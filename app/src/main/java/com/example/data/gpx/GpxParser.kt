package com.example.data.gpx

import com.example.data.model.GpxPoint
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.*

private data class RawGpxPoint(
    val lat: Double,
    val lon: Double,
    val ele: Double,
    val hasEle: Boolean,
    val timeMillis: Long?
)

data class GpxParseResult(
    val points: List<GpxPoint>,
    val distanceKm: Double,
    val elevationGainM: Int,
    val elevationLossM: Int,
    val origin: String?,
    val destination: String?,
    val estimatedHours: Double,
    val formattedTime: String = ""
)

object GpxParser {

    private fun parseIsoTimestamp(timeStr: String): Long? {
        val clean = timeStr.trim()
        if (clean.isBlank()) return null
        try {
            return java.time.Instant.parse(clean).toEpochMilli()
        } catch (_: Exception) {}
        try {
            return java.time.OffsetDateTime.parse(clean).toInstant().toEpochMilli()
        } catch (_: Exception) {}

        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss"
        )
        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val date = sdf.parse(clean)
                if (date != null) return date.time
            } catch (_: Exception) {}
        }
        return null
    }

    /**
     * Parses standard GPX 1.0 / 1.1 XML string and computes the 4 key track metrics:
     * 1. Distance (Km): Haversine distance with filter ignoring jumps < 2 meters (anti-noise). Max 2 decimals.
     * 2. Time: Difference between last point and first point timestamp, truncated to minutes ("X h Y min").
     * 3. Elevation Gain & Loss (Double Filter): Moving average smoothing (5-point window) + Hysteresis threshold (3.5m).
     */
    fun parse(gpxContent: String): GpxParseResult {
        val rawPoints = mutableListOf<RawGpxPoint>()
        var originName: String? = null
        var destName: String? = null

        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(StringReader(gpxContent))

            var eventType = parser.eventType
            var currentLat: Double? = null
            var currentLon: Double? = null
            var currentEle = 0.0
            var hasEle = false
            var currentTimeMillis: Long? = null
            var insideTrkpt = false
            var currentTag = ""

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        val tagName = parser.name?.substringAfter(":")?.lowercase() ?: ""
                        currentTag = tagName
                        if (tagName == "trkpt" || tagName == "wpt") {
                            var latStr: String? = null
                            var lonStr: String? = null
                            for (i in 0 until parser.attributeCount) {
                                val attr = parser.getAttributeName(i).lowercase()
                                if (attr == "lat") latStr = parser.getAttributeValue(i)
                                if (attr == "lon") lonStr = parser.getAttributeValue(i)
                            }
                            currentLat = latStr?.toDoubleOrNull()
                            currentLon = lonStr?.toDoubleOrNull()
                            currentEle = 0.0
                            hasEle = false
                            currentTimeMillis = null
                            insideTrkpt = true
                        }
                    }
                    XmlPullParser.TEXT -> {
                        val text = parser.text?.trim() ?: ""
                        if (insideTrkpt && text.isNotEmpty()) {
                            when (currentTag) {
                                "ele" -> {
                                    currentEle = text.toDoubleOrNull() ?: 0.0
                                    hasEle = true
                                }
                                "time" -> {
                                    currentTimeMillis = parseIsoTimestamp(text)
                                }
                            }
                        } else if (currentTag == "name" && text.isNotEmpty()) {
                            if (originName == null) originName = text
                            else destName = text
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        val endTag = parser.name?.substringAfter(":")?.lowercase() ?: ""
                        if (endTag == "trkpt" || endTag == "wpt") {
                            if (currentLat != null && currentLon != null) {
                                rawPoints.add(RawGpxPoint(currentLat, currentLon, currentEle, hasEle, currentTimeMillis))
                            }
                            insideTrkpt = false
                            currentLat = null
                            currentLon = null
                            currentEle = 0.0
                            hasEle = false
                            currentTimeMillis = null
                        }
                        currentTag = ""
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (rawPoints.isEmpty()) {
            val trkptFullRegex = """<trkpt[^>]*lat=["']([-0-9.]+)["'][^>]*lon=["']([-0-9.]+)["'][^>]*>(.*?)</trkpt>""".toRegex(RegexOption.DOT_MATCHES_ALL)
            for (match in trkptFullRegex.findAll(gpxContent)) {
                val lat = match.groupValues[1].toDoubleOrNull()
                val lon = match.groupValues[2].toDoubleOrNull()
                val inner = match.groupValues[3]
                val eleMatch = """<ele>([-0-9.]+)</ele>""".toRegex().find(inner)
                val ele = eleMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                val hasEle = eleMatch != null
                val timeStr = """<time>(.*?)</time>""".toRegex().find(inner)?.groupValues?.get(1)?.trim()
                val timeMillis = if (!timeStr.isNullOrBlank()) parseIsoTimestamp(timeStr) else null
                if (lat != null && lon != null) {
                    rawPoints.add(RawGpxPoint(lat, lon, ele, hasEle, timeMillis))
                }
            }
            if (rawPoints.isEmpty()) {
                val simpleTrkpt = """<trkpt[^>]*lat=["']([-0-9.]+)["'][^>]*lon=["']([-0-9.]+)["']""".toRegex()
                for (m in simpleTrkpt.findAll(gpxContent)) {
                    val lat = m.groupValues[1].toDoubleOrNull()
                    val lon = m.groupValues[2].toDoubleOrNull()
                    if (lat != null && lon != null) {
                        rawPoints.add(RawGpxPoint(lat, lon, 0.0, false, null))
                    }
                }
            }
            val nameMatch = """<name>(.*?)</name>""".toRegex().find(gpxContent)
            if (nameMatch != null) {
                val nameText = nameMatch.groupValues[1].trim()
                if (originName == null) originName = nameText
            }
        }

        // 1. DISTANCIA (KM) CON FILTRO ANTI-RUIDO (< 2 metros)
        var totalDistanceMeters = 0.0
        for (i in 0 until rawPoints.size - 1) {
            val p1 = rawPoints[i]
            val p2 = rawPoints[i + 1]
            val stepMeters = haversineDistanceMeters(p1.lat, p1.lon, p2.lat, p2.lon)
            if (stepMeters >= 2.0) {
                totalDistanceMeters += stepMeters
            }
        }
        val distanceKm = round((totalDistanceMeters / 1000.0) * 100.0) / 100.0

        // 2. DESNIVEL POSITIVO Y NEGATIVO ACUMULADO CON FILTRO DOBLE (Media Móvil + Histéresis 3.5m)
        var gainM = 0.0
        var lossM = 0.0
        val elevationPoints = rawPoints.filter { it.hasEle }
        if (elevationPoints.size >= 2) {
            // Paso 1 (Suavizado): Media móvil con ventana de 5 puntos para eliminar picos de altitud irreales
            val rawEle = elevationPoints.map { it.ele }
            val smoothed = DoubleArray(rawEle.size)
            val radius = 2
            for (i in rawEle.indices) {
                val start = maxOf(0, i - radius)
                val end = minOf(rawEle.lastIndex, i + radius)
                var sum = 0.0
                var count = 0
                for (j in start..end) {
                    sum += rawEle[j]
                    count++
                }
                smoothed[i] = sum / count
            }

            // Paso 2 (Histéresis): Umbral de 3.5 metros. Solo suma cuando supera el umbral respecto al último punto validado
            val threshold = 3.5
            var lastValidatedEle = smoothed[0]
            for (i in 1 until smoothed.size) {
                val current = smoothed[i]
                val dEle = current - lastValidatedEle
                if (dEle >= threshold) {
                    gainM += dEle
                    lastValidatedEle = current
                } else if (dEle <= -threshold) {
                    lossM += abs(dEle)
                    lastValidatedEle = current
                }
            }
        }
        val finalElevationGainM = gainM.roundToInt()
        val finalElevationLossM = lossM.roundToInt()

        // 3. TIEMPO: Diferencia entre último punto y primer punto, truncado a minutos ("X h Y min")
        val firstTime = rawPoints.firstOrNull { it.timeMillis != null }?.timeMillis
        val lastTime = rawPoints.lastOrNull { it.timeMillis != null }?.timeMillis
        val durationMinutes = if (firstTime != null && lastTime != null && lastTime >= firstTime) {
            (lastTime - firstTime) / 60000L
        } else {
            if (distanceKm > 0) {
                (((distanceKm / 4.2) + (finalElevationGainM / 400.0)) * 60).toLong()
            } else 0L
        }

        val hours = durationMinutes / 60L
        val mins = durationMinutes % 60L
        val formattedTime = if (hours > 0) {
            "${hours} h ${mins} min"
        } else {
            "${mins} min"
        }
        val estimatedHours = durationMinutes / 60.0

        // Puntos simplificados para renderizado en mapa / elevación (máximo 250)
        val sampledPoints = if (rawPoints.size > 250) {
            val step = rawPoints.size / 250
            rawPoints.filterIndexed { index, _ -> index % step == 0 || index == rawPoints.lastIndex }
                .map { GpxPoint(it.lat, it.lon, it.ele) }
        } else {
            rawPoints.map { GpxPoint(it.lat, it.lon, it.ele) }
        }

        return GpxParseResult(
            points = sampledPoints,
            distanceKm = distanceKm,
            elevationGainM = finalElevationGainM,
            elevationLossM = finalElevationLossM,
            origin = originName,
            destination = destName,
            estimatedHours = estimatedHours,
            formattedTime = formattedTime
        )
    }

    private fun haversineDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun haversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        return haversineDistanceMeters(lat1, lon1, lat2, lon2) / 1000.0
    }

    fun serializePoints(points: List<GpxPoint>): String {
        val array = JSONArray()
        for (p in points) {
            val obj = JSONObject()
            obj.put("lat", p.lat)
            obj.put("lon", p.lon)
            obj.put("ele", p.ele)
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializePoints(json: String?): List<GpxPoint> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<GpxPoint>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    GpxPoint(
                        lat = obj.optDouble("lat", 0.0),
                        lon = obj.optDouble("lon", 0.0),
                        ele = obj.optDouble("ele", 0.0)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
