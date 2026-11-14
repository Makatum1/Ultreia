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

        val hMatch = Regex("""(\d+)\s*h""", RegexOption.IGNORE_CASE).find(clean)
        val mMatch = Regex("""(\d+)\s*m(?:in)?""", RegexOption.IGNORE_CASE).find(clean)
        val sMatch = Regex("""(\d+)\s*s(?:eg)?""", RegexOption.IGNORE_CASE).find(clean)
        if (hMatch != null || mMatch != null || sMatch != null) {
            val h = hMatch?.groupValues?.get(1)?.toLongOrNull() ?: 0L
            val m = mMatch?.groupValues?.get(1)?.toLongOrNull() ?: 0L
            val s = sMatch?.groupValues?.get(1)?.toLongOrNull() ?: 0L
            val total = h * 3600L + m * 60L + s
            if (total > 0L) return total
        }

        if (clean.startsWith("PT", ignoreCase = true)) {
            try {
                return java.time.Duration.parse(clean).seconds
            } catch (_: Exception) {}
        }
        return null
    }

    private fun extractMetricsFromText(
        text: String,
        onDistanceKm: (Double) -> Unit,
        onGainM: (Int) -> Unit,
        onLossM: (Int) -> Unit,
        onMovingTimeSec: (Long) -> Unit
    ) {
        if (text.isBlank()) return

        // 1. Distancia
        val distRegex = Regex("""(?:distancia|distance|longitud|length)\s*[:=]?\s*([0-9]+[.,]?[0-9]*)\s*(km|m)?\b""", RegexOption.IGNORE_CASE)
        distRegex.find(text)?.let { m ->
            val rawNum = m.groupValues[1].replace(',', '.')
            val unit = m.groupValues.getOrNull(2)?.lowercase() ?: "km"
            val num = rawNum.toDoubleOrNull()
            if (num != null && num > 0.0) {
                val km = if (unit == "m" && num > 500.0) num / 1000.0 else num
                onDistanceKm(km)
            }
        }

        // 2. Desnivel positivo
        val gainRegex = Regex("""(?:desnivel\s*(?:\+|positivo|acumulado\s*(?:subiendo|positivo)?)|elevation\s*gain|ascent|subida|ganancia\s*(?:de\s*elevaci[oó]n)?)\s*[:=]?\s*\+?\s*([0-9]+[.,]?[0-9]*)\s*(?:m|metros)?\b""", RegexOption.IGNORE_CASE)
        gainRegex.find(text)?.let { m ->
            val rawNum = m.groupValues[1].replace(',', '.')
            rawNum.toDoubleOrNull()?.let {
                if (it > 0.0) onGainM(it.roundToInt())
            }
        }

        // 3. Desnivel negativo
        val lossRegex = Regex("""(?:desnivel\s*(?:\-|negativo|acumulado\s*(?:bajando|negativo)?)|elevation\s*loss|descent|bajada|p[eé]rdida\s*(?:de\s*elevaci[oó]n)?)\s*[:=]?\s*\-?\s*([0-9]+[.,]?[0-9]*)\s*(?:m|metros)?\b""", RegexOption.IGNORE_CASE)
        lossRegex.find(text)?.let { m ->
            val rawNum = m.groupValues[1].replace(',', '.')
            rawNum.toDoubleOrNull()?.let {
                if (it > 0.0) onLossM(it.roundToInt())
            }
        }

        // 4. Tiempo en movimiento
        val timeRegex = Regex("""(?:tiempo\s*(?:en\s*movimiento|movimiento|activo)?|moving\s*time|time\s*moving)\s*[:=]?\s*([0-9]+(?::[0-9]+(?::[0-9]+)?|\s*h(?:\s*[0-9]+\s*m)?(?:\s*[0-9]+\s*s)?|[0-9]+\s*min))""", RegexOption.IGNORE_CASE)
        timeRegex.find(text)?.let { m ->
            val timeStr = m.groupValues[1].trim()
            parseDurationToSeconds(timeStr)?.let {
                if (it > 0L) onMovingTimeSec(it)
            }
        }
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
                        if (text.isNotEmpty()) {
                            if (insideTrkpt) {
                                when (currentTag) {
                                    "ele" -> {
                                        currentEle = text.toDoubleOrNull() ?: 0.0
                                        hasEle = true
                                    }
                                    "time" -> currentTimeMillis = parseIsoTimestamp(text)
                                }
                            } else {
                                when (currentTag) {
                                    "name" -> {
                                        if (originName == null) originName = text
                                        else destName = text
                                    }
                                    "desc", "cmt" -> {
                                        extractMetricsFromText(
                                            text = text,
                                            onDistanceKm = { if (explicitDistanceMeters == null) explicitDistanceMeters = it },
                                            onGainM = { if (explicitAscentMeters == null) explicitAscentMeters = it.toDouble() },
                                            onLossM = { if (explicitDescentMeters == null) explicitDescentMeters = it.toDouble() },
                                            onMovingTimeSec = { if (explicitMovingTimeSec == null) explicitMovingTimeSec = it }
                                        )
                                    }
                                    "distance", "length", "totaldistance", "total_distance", "dist", "totaldist", "total_dist" -> {
                                        text.toDoubleOrNull()?.let { if (it > 0) explicitDistanceMeters = it }
                                    }
                                    "ascent", "totalascent", "total_ascent", "elevationgain", "elevation_gain", "gain", "elegain", "ele_gain", "climb", "totalclimb" -> {
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
                                internalPoints.add(InternalGpxPoint(currentLat, currentLon, currentEle, hasEle, currentTimeMillis))
                            }
                            insideTrkpt = false
                            currentLat = null
                            currentLon = null
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

        if (points.isEmpty()) {
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
                    points.add(GpxPoint(lat, lon, ele))
                    internalPoints.add(InternalGpxPoint(lat, lon, ele, hasEle, timeMillis))
                }
            }
            if (points.isEmpty()) {
                val simpleTrkpt = """<trkpt[^>]*lat=["']([-0-9.]+)["'][^>]*lon=["']([-0-9.]+)["']""".toRegex()
                for (m in simpleTrkpt.findAll(gpxContent)) {
                    val lat = m.groupValues[1].toDoubleOrNull()
                    val lon = m.groupValues[2].toDoubleOrNull()
                    if (lat != null && lon != null) {
                        points.add(GpxPoint(lat, lon, 0.0))
                        internalPoints.add(InternalGpxPoint(lat, lon, 0.0, false, null))
                    }
                }
            }
            val nameMatch = """<name>(.*?)</name>""".toRegex().find(gpxContent)
            if (nameMatch != null) {
                val nameText = nameMatch.groupValues[1].trim()
                if (originName == null) originName = nameText
            }
        }

        // Escaneo global de descripciones si los campos explícitos aún no se han encontrado
        if (explicitDistanceMeters == null || explicitAscentMeters == null || explicitDescentMeters == null || explicitMovingTimeSec == null) {
            extractMetricsFromText(
                text = gpxContent,
                onDistanceKm = { if (explicitDistanceMeters == null) explicitDistanceMeters = it },
                onGainM = { if (explicitAscentMeters == null) explicitAscentMeters = it.toDouble() },
                onLossM = { if (explicitDescentMeters == null) explicitDescentMeters = it.toDouble() },
                onMovingTimeSec = { if (explicitMovingTimeSec == null) explicitMovingTimeSec = it }
            )
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
        for (i in 0 until internalPoints.size - 1) {
            val p1 = internalPoints[i]
            val p2 = internalPoints[i + 1]
            totalDistanceKm += haversineDistanceKm(p1.lat, p1.lon, p2.lat, p2.lon)
        }

        val finalDistanceKm = if (explicitDistanceMeters != null && explicitDistanceMeters > 0.0) {
            if (explicitDistanceMeters > 500.0) explicitDistanceMeters / 1000.0 else explicitDistanceMeters
        } else {
            // Eliminar ruido de coma flotante IEEE manteniendo los decimales reales sin redondeo forzado
            round(totalDistanceKm * 10000.0) / 10000.0
        }
        val distanceExact = finalDistanceKm

        // 2. Desnivel positivo y negativo acumulado en TODO el GPX (calculado sobre los puntos con altimetría)
        var gainM = 0.0
        var lossM = 0.0
        val elevationPoints = internalPoints.filter { it.hasEle }
        for (i in 0 until elevationPoints.size - 1) {
            val e1 = elevationPoints[i].ele
            val e2 = elevationPoints[i + 1].ele
            val dEle = e2 - e1
            if (dEle > 0.0) {
                gainM += dEle
            } else if (dEle < 0.0) {
                lossM += abs(dEle)
            }
        }

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

        // 3. Tiempo en movimiento (moving time) sin segundos
        var movingSeconds = 0.0
        if (explicitMovingTimeSec != null && explicitMovingTimeSec > 0) {
            movingSeconds = explicitMovingTimeSec.toDouble()
        } else {
            val timedPoints = internalPoints.filter { it.timeMillis != null }
            for (i in 0 until timedPoints.size - 1) {
                val p1 = timedPoints[i]
                val p2 = timedPoints[i + 1]
                val dtSec = (p2.timeMillis!! - p1.timeMillis!!) / 1000.0
                if (dtSec in 0.5..900.0) {
                    val distKm = haversineDistanceKm(p1.lat, p1.lon, p2.lat, p2.lon)
                    val speedKmh = (distKm / dtSec) * 3600.0
                    // Umbral senderista de movimiento efectivo (descartando paradas)
                    if (speedKmh >= 0.5) {
                        movingSeconds += dtSec
                    }
                }
            }

            if (movingSeconds <= 0.0 && timedPoints.size >= 2) {
                val firstTime = timedPoints.first().timeMillis!!
                val lastTime = timedPoints.last().timeMillis!!
                if (lastTime > firstTime) {
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

        // Tiempo en movimiento en formato "Xh Ym" sin incluir segundos
        val movingTimeStr = if (hours > 0) {
            "${hours}h ${String.format(Locale.US, "%02d", minutes)}m"
        } else if (minutes > 0) {
            "${minutes}m"
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
