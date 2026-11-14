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

private data class InternalGpxPoint(
    val lat: Double,
    val lon: Double,
    val ele: Double,
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
    val movingTimeStr: String = ""
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

    private fun parseDurationToSeconds(text: String): Long? {
        val clean = text.trim()
        if (clean.isBlank()) return null
        clean.toLongOrNull()?.let { return it }
        clean.toDoubleOrNull()?.let { return it.toLong() }
        if (clean.contains(":")) {
            val parts = clean.split(":")
            if (parts.size == 3) {
                val h = parts[0].trim().toLongOrNull() ?: 0L
                val m = parts[1].trim().toLongOrNull() ?: 0L
                val s = parts[2].trim().toDoubleOrNull()?.toLong() ?: 0L
                return h * 3600L + m * 60L + s
            } else if (parts.size == 2) {
                val m = parts[0].trim().toLongOrNull() ?: 0L
                val s = parts[1].trim().toDoubleOrNull()?.toLong() ?: 0L
                return m * 60L + s
            }
        }
        if (clean.startsWith("PT", ignoreCase = true)) {
            try {
                return java.time.Duration.parse(clean).seconds
            } catch (_: Exception) {}
        }
        return null
    }

    /**
     * Parses standard GPX 1.0 / 1.1 XML string and computes track metrics without arbitrary rounding
     */
    fun parse(gpxContent: String): GpxParseResult {
        val points = mutableListOf<GpxPoint>()
        val internalPoints = mutableListOf<InternalGpxPoint>()
        var originName: String? = null
        var destName: String? = null

        var explicitDistanceMeters: Double? = null
        var explicitAscentMeters: Double? = null
        var explicitDescentMeters: Double? = null
        var explicitMovingTimeSec: Long? = null
        var explicitTotalTimeSec: Long? = null

        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(StringReader(gpxContent))

            var eventType = parser.eventType
            var currentLat: Double? = null
            var currentLon: Double? = null
            var currentEle = 0.0
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
                            currentTimeMillis = null
                            insideTrkpt = true
                        }
                    }
                    XmlPullParser.TEXT -> {
                        val text = parser.text?.trim() ?: ""
                        if (text.isNotEmpty()) {
                            if (insideTrkpt) {
                                when (currentTag) {
                                    "ele" -> currentEle = text.toDoubleOrNull() ?: 0.0
                                    "time" -> currentTimeMillis = parseIsoTimestamp(text)
                                }
                            } else {
                                when (currentTag) {
                                    "name" -> {
                                        if (originName == null) originName = text
                                        else destName = text
                                    }
                                    "distance", "length", "totaldistance", "total_distance", "dist" -> {
                                        text.toDoubleOrNull()?.let { if (it > 0) explicitDistanceMeters = it }
                                    }
                                    "ascent", "totalascent", "total_ascent", "elevationgain", "elevation_gain", "gain", "elegain", "ele_gain", "climb" -> {
                                        text.toDoubleOrNull()?.let { if (it > 0) explicitAscentMeters = it }
                                    }
                                    "descent", "totaldescent", "total_descent", "elevationloss", "elevation_loss", "loss", "eleloss", "ele_loss" -> {
                                        text.toDoubleOrNull()?.let { if (it > 0) explicitDescentMeters = it }
                                    }
                                    "movingtime", "moving_time", "timemoving", "time_moving", "totalmovingtime", "total_moving_time" -> {
                                        parseDurationToSeconds(text)?.let { if (it > 0) explicitMovingTimeSec = it }
                                    }
                                    "totaltime", "total_time", "duration", "elapsedtime", "elapsed_time" -> {
                                        parseDurationToSeconds(text)?.let { if (it > 0) explicitTotalTimeSec = it }
                                    }
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        val endTag = parser.name?.substringAfter(":")?.lowercase() ?: ""
                        if (endTag == "trkpt" || endTag == "wpt") {
                            if (currentLat != null && currentLon != null) {
                                val pt = GpxPoint(currentLat, currentLon, currentEle)
                                points.add(pt)
                                internalPoints.add(InternalGpxPoint(currentLat, currentLon, currentEle, currentTimeMillis))
                            }
                            insideTrkpt = false
                            currentLat = null
                            currentLon = null
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

        if (points.isEmpty()) {
            val trkptFullRegex = """<trkpt[^>]*lat=["']([-0-9.]+)["'][^>]*lon=["']([-0-9.]+)["'][^>]*>(.*?)</trkpt>""".toRegex(RegexOption.DOT_MATCHES_ALL)
            for (match in trkptFullRegex.findAll(gpxContent)) {
                val lat = match.groupValues[1].toDoubleOrNull()
                val lon = match.groupValues[2].toDoubleOrNull()
                val inner = match.groupValues[3]
                val ele = """<ele>([-0-9.]+)</ele>""".toRegex().find(inner)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                val timeStr = """<time>(.*?)</time>""".toRegex().find(inner)?.groupValues?.get(1)?.trim()
                val timeMillis = if (!timeStr.isNullOrBlank()) parseIsoTimestamp(timeStr) else null
                if (lat != null && lon != null) {
                    points.add(GpxPoint(lat, lon, ele))
                    internalPoints.add(InternalGpxPoint(lat, lon, ele, timeMillis))
                }
            }
            if (points.isEmpty()) {
                val simpleTrkpt = """<trkpt[^>]*lat=["']([-0-9.]+)["'][^>]*lon=["']([-0-9.]+)["']""".toRegex()
                for (m in simpleTrkpt.findAll(gpxContent)) {
                    val lat = m.groupValues[1].toDoubleOrNull()
                    val lon = m.groupValues[2].toDoubleOrNull()
                    if (lat != null && lon != null) {
                        points.add(GpxPoint(lat, lon, 0.0))
                        internalPoints.add(InternalGpxPoint(lat, lon, 0.0, null))
                    }
                }
            }
            val nameMatch = """<name>(.*?)</name>""".toRegex().find(gpxContent)
            if (nameMatch != null) {
                val nameText = nameMatch.groupValues[1].trim()
                if (originName == null) originName = nameText
            }
        }

        // Downsample points if too dense (limit to max ~250 points for smooth canvas rendering)
        val sampledPoints = if (points.size > 250) {
            val step = points.size / 250
            points.filterIndexed { index, _ -> index % step == 0 || index == points.lastIndex }
        } else {
            points
        }

        // 1. Distancia acumulada exacta de todos los puntos consecutivos (o metadato explícito del GPX)
        var totalDistanceKm = 0.0
        var gainM = 0.0
        var lossM = 0.0

        for (i in 0 until internalPoints.size - 1) {
            val p1 = internalPoints[i]
            val p2 = internalPoints[i + 1]
            totalDistanceKm += haversineDistanceKm(p1.lat, p1.lon, p2.lat, p2.lon)

            val dEle = p2.ele - p1.ele
            if (dEle > 0.0) gainM += dEle
            else if (dEle < 0.0) lossM += abs(dEle)
        }

        val finalDistanceKm = if (explicitDistanceMeters != null && explicitDistanceMeters > 0.0) {
            if (explicitDistanceMeters > 500.0) explicitDistanceMeters / 1000.0 else explicitDistanceMeters
        } else {
            totalDistanceKm
        }
        val distanceExact = (round(finalDistanceKm * 100.0) / 100.0)

        // 2. Desnivel + y Desnivel - (acumulada exacta)
        val finalElevationGainM = if (explicitAscentMeters != null && explicitAscentMeters > 0.0) {
            explicitAscentMeters.roundToInt()
        } else {
            gainM.roundToInt()
        }

        val finalElevationLossM = if (explicitDescentMeters != null && explicitDescentMeters > 0.0) {
            explicitDescentMeters.roundToInt()
        } else {
            lossM.roundToInt()
        }

        // 3. Tiempo en movimiento (moving time)
        var movingSeconds = 0.0
        if (explicitMovingTimeSec != null && explicitMovingTimeSec > 0) {
            movingSeconds = explicitMovingTimeSec.toDouble()
        } else {
            for (i in 0 until internalPoints.size - 1) {
                val p1 = internalPoints[i]
                val p2 = internalPoints[i + 1]
                if (p1.timeMillis != null && p2.timeMillis != null) {
                    val dtSec = (p2.timeMillis - p1.timeMillis) / 1000.0
                    if (dtSec in 0.5..900.0) {
                        val distKm = haversineDistanceKm(p1.lat, p1.lon, p2.lat, p2.lon)
                        val speedKmh = (distKm / dtSec) * 3600.0
                        if (speedKmh >= 0.5) {
                            movingSeconds += dtSec
                        }
                    }
                }
            }

            if (movingSeconds <= 0.0) {
                val firstTime = internalPoints.firstOrNull { it.timeMillis != null }?.timeMillis
                val lastTime = internalPoints.lastOrNull { it.timeMillis != null }?.timeMillis
                if (firstTime != null && lastTime != null && lastTime > firstTime) {
                    movingSeconds = (lastTime - firstTime) / 1000.0
                }
            }

            if (movingSeconds <= 0.0 && explicitTotalTimeSec != null && explicitTotalTimeSec > 0) {
                movingSeconds = explicitTotalTimeSec.toDouble()
            }
        }

        // Si no existen tiempos en el archivo GPX (ruta dibujada a mano sin timestamps), estimar ritmo senderista
        if (movingSeconds <= 0.0) {
            val estimatedHours = if (distanceExact > 0) {
                (distanceExact / 4.2) + (finalElevationGainM / 400.0)
            } else 0.0
            movingSeconds = estimatedHours * 3600.0
        }

        val totalSec = movingSeconds.roundToLong()
        val hours = totalSec / 3600
        val minutes = (totalSec % 3600) / 60
        val seconds = totalSec % 60

        val movingTimeStr = if (hours > 0) {
            if (seconds > 0) {
                "${hours}h ${String.format(Locale.US, "%02d", minutes)}m ${String.format(Locale.US, "%02d", seconds)}s"
            } else {
                "${hours}h ${String.format(Locale.US, "%02d", minutes)}m"
            }
        } else if (minutes > 0) {
            if (seconds > 0) {
                "${minutes}m ${String.format(Locale.US, "%02d", seconds)}s"
            } else {
                "${minutes}m"
            }
        } else if (seconds > 0) {
            "${seconds}s"
        } else {
            "0h 00m"
        }

        val estimatedHoursResult = movingSeconds / 3600.0

        return GpxParseResult(
            points = sampledPoints,
            distanceKm = distanceExact,
            elevationGainM = finalElevationGainM,
            elevationLossM = finalElevationLossM,
            origin = originName,
            destination = destName,
            estimatedHours = estimatedHoursResult,
            movingTimeStr = movingTimeStr
        )
    }

    private fun haversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
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
