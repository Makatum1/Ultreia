package com.example.data.gpx

import android.location.Location
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
     * 1. Distance (Km): Native Android Location.distanceBetween per <trkseg> with unreal speed filter (> 30 km/h). Max 2 decimals.
     * 2. Time: Recorded duration summing point-to-point intervals <= 10s ("X h Y min").
     * 3. Elevation Gain & Loss (Double Filter): Moving average smoothing (11-point window) + Hysteresis threshold (0.80m).
     */
    fun parse(gpxContent: String): GpxParseResult {
        val rawPoints = mutableListOf<RawGpxPoint>()
        val segments = mutableListOf<MutableList<RawGpxPoint>>()
        var currentSegment = mutableListOf<RawGpxPoint>()
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
                        if (tagName == "trkseg") {
                            if (currentSegment.isNotEmpty()) {
                                segments.add(currentSegment)
                                currentSegment = mutableListOf()
                            }
                        } else if (tagName == "trkpt" || tagName == "wpt") {
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
                                val pt = RawGpxPoint(currentLat, currentLon, currentEle, hasEle, currentTimeMillis)
                                rawPoints.add(pt)
                                currentSegment.add(pt)
                            }
                            insideTrkpt = false
                            currentLat = null
                            currentLon = null
                            currentEle = 0.0
                            hasEle = false
                            currentTimeMillis = null
                        } else if (endTag == "trkseg") {
                            if (currentSegment.isNotEmpty()) {
                                segments.add(currentSegment)
                                currentSegment = mutableListOf()
                            }
                        }
                        currentTag = ""
                    }
                }
                eventType = parser.next()
            }
            if (currentSegment.isNotEmpty()) {
                segments.add(currentSegment)
                currentSegment = mutableListOf()
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
                    val pt = RawGpxPoint(lat, lon, ele, hasEle, timeMillis)
                    rawPoints.add(pt)
                    currentSegment.add(pt)
                }
            }
            if (rawPoints.isEmpty()) {
                val simpleTrkpt = """<trkpt[^>]*lat=["']([-0-9.]+)["'][^>]*lon=["']([-0-9.]+)["']""".toRegex()
                for (m in simpleTrkpt.findAll(gpxContent)) {
                    val lat = m.groupValues[1].toDoubleOrNull()
                    val lon = m.groupValues[2].toDoubleOrNull()
                    if (lat != null && lon != null) {
                        val pt = RawGpxPoint(lat, lon, 0.0, false, null)
                        rawPoints.add(pt)
                        currentSegment.add(pt)
                    }
                }
            }
            if (currentSegment.isNotEmpty()) {
                segments.add(currentSegment)
                currentSegment = mutableListOf()
            }
            val nameMatch = """<name>(.*?)</name>""".toRegex().find(gpxContent)
            if (nameMatch != null) {
                val nameText = nameMatch.groupValues[1].trim()
                if (originName == null) originName = nameText
            }
        }

        if (segments.isEmpty() && rawPoints.isNotEmpty()) {
            segments.add(rawPoints)
        }

        // 1. DISTANCIA (KM) CON FORMULA NATIVA, SEGMENTOS Y FILTRO DE VELOCIDAD (< 30 km/h)
        var totalDistanceMeters = 0.0
        val results = FloatArray(1)
        for (segment in segments) {
            for (i in 0 until segment.size - 1) {
                val p1 = segment[i]
                val p2 = segment[i + 1]
                Location.distanceBetween(p1.lat, p1.lon, p2.lat, p2.lon, results)
                val stepMeters = results[0].toDouble()

                // Filtro de velocidad irreal: si la velocidad supera 30 km/h se ignora el tramo
                var isUnrealSpeed = false
                if (p1.timeMillis != null && p2.timeMillis != null) {
                    val dtSec = (p2.timeMillis - p1.timeMillis) / 1000.0
                    if (dtSec > 0.0) {
                        val speedKmh = (stepMeters / dtSec) * 3.6
                        if (speedKmh > 30.0) {
                            isUnrealSpeed = true
                        }
                    }
                }

                if (!isUnrealSpeed) {
                    totalDistanceMeters += stepMeters
                }
            }
        }
        val distanceKm = round((totalDistanceMeters / 1000.0) * 100.0) / 100.0

        // 2. DESNIVEL POSITIVO Y NEGATIVO ACUMULADO (Suavizado 11 puntos + Histéresis 0.80m)
        var gainM = 0.0
        var lossM = 0.0
        val elevationPoints = rawPoints.filter { it.hasEle }
        if (elevationPoints.size >= 2) {
            // Paso 1 (Suavizado): Media móvil con ventana de 11 puntos (radio = 5 puntos a cada lado)
            val rawEle = elevationPoints.map { it.ele }
            val smoothed = DoubleArray(rawEle.size)
            val radius = 5
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

            // Paso 2 (Histéresis): Umbral de 0.80 metros. Solo suma cuando el cambio respecto al último punto validado supera 0.80m
            val threshold = 0.80
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

        // 3. TIEMPO GRABADO: Suma de los intervalos entre puntos hasta 10 segundos (sin huecos)
        var recordedDurationSec = 0.0
        val timedPoints = rawPoints.filter { it.timeMillis != null }
        for (i in 0 until timedPoints.size - 1) {
            val p1 = timedPoints[i]
            val p2 = timedPoints[i + 1]
            val dtSec = (p2.timeMillis!! - p1.timeMillis!!) / 1000.0
            if (dtSec > 0.0 && dtSec <= 10.0) {
                recordedDurationSec += dtSec
            }
        }

        val durationMinutes = if (recordedDurationSec > 0.0) {
            (recordedDurationSec / 60.0).roundToLong()
        } else {
            // Fallback si el GPX no contiene marcas de tiempo consecutivas de muestreo
            val firstTime = timedPoints.firstOrNull()?.timeMillis
            val lastTime = timedPoints.lastOrNull()?.timeMillis
            if (firstTime != null && lastTime != null && lastTime >= firstTime) {
                (lastTime - firstTime) / 60000L
            } else if (distanceKm > 0) {
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
