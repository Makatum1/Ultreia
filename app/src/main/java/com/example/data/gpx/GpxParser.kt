package com.example.data.gpx

import com.example.data.model.GpxPoint
import org.json.JSONArray
import org.json.JSONObject
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.xml.parsers.SAXParserFactory
import kotlin.math.*

data class RawGpxPoint(
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
    val formattedTime: String = "",
    val totalTrkptCount: Int = 0,
    val ignoredWptCount: Int = 0,
    val ignoredRteptCount: Int = 0,
    val minElevationM: Double = 0.0,
    val maxElevationM: Double = 0.0,
    val totalDurationMs: Long = 0L,
    val movingDurationMs: Long = 0L,
    val debugLog: String = ""
)

object GpxParser {

    const val EARTH_RADIUS_METERS = 6371008.8
    const val ELEVATION_THRESHOLD_M = 5.0
    const val ELEVATION_THRESHOLD_BAROMETRIC_M = 2.0
    const val MOVING_SPEED_MS = 0.5

    /**
     * Determina el umbral de histéresis adecuado según el ruido de elevación del segmento:
     * - Trazas suaves / barométricas (1 Hz, baja inversión de pendiente): 2.0 m.
     * - Trazas GPS estándar con ruido decimal: 5.0 m.
     */
    private fun effectiveElevationThreshold(elePoints: List<RawGpxPoint>): Double {
        if (elePoints.size < 100) return ELEVATION_THRESHOLD_M
        var sumAbsDiff = 0.0
        var reversals = 0
        var prevDir = 0
        for (i in 1 until elePoints.size) {
            val d = elePoints[i].ele - elePoints[i - 1].ele
            sumAbsDiff += abs(d)
            if (d != 0.0) {
                val dir = if (d > 0) 1 else -1
                if (prevDir != 0 && dir != prevDir) reversals++
                prevDir = dir
            }
        }
        val n = elePoints.size - 1
        val meanAbsDiff = sumAbsDiff / n
        val reversalRatio = reversals.toDouble() / n
        return if (meanAbsDiff < 0.15 && reversalRatio < 0.05) {
            ELEVATION_THRESHOLD_BAROMETRIC_M
        } else {
            ELEVATION_THRESHOLD_M
        }
    }

    private fun safeLog(tag: String, msg: String) {
        try {
            android.util.Log.d(tag, msg)
        } catch (_: Throwable) {
            println("[$tag] $msg")
        }
    }

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
     * Calcula la distancia Haversine 2D en metros entre dos coordenadas geográficas.
     * Radio medio terrestre: 6,371,008.8 m.
     */
    fun haversineDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)

        val a = sin(dLat / 2.0).pow(2.0) +
                cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2.0).pow(2.0)
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        return EARTH_RADIUS_METERS * c
    }

    fun haversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        return haversineDistanceMeters(lat1, lon1, lat2, lon2) / 1000.0
    }

    /**
     * Simplificación geométrica iterativa (Ramer-Douglas-Peucker) en metros.
     * Conserva todas las curvas de calles, giros y puentes eliminando solo puntos redundantes en rectas.
     */
    private fun simplifyDouglasPeucker(
        points: List<RawGpxPoint>,
        epsilonMeters: Double
    ): List<GpxPoint> {
        if (points.size <= 2) return points.map { GpxPoint(it.lat, it.lon, it.ele) }

        val lat0 = points[0].lat
        val lon0 = points[0].lon
        val cosLat = cos(Math.toRadians(lat0))
        val mPerDegLat = 110540.0
        val mPerDegLon = 111320.0 * cosLat

        val xs = DoubleArray(points.size) { (points[it].lon - lon0) * mPerDegLon }
        val ys = DoubleArray(points.size) { (points[it].lat - lat0) * mPerDegLat }

        val keep = BooleanArray(points.size)
        keep[0] = true
        keep[points.lastIndex] = true

        val stack = ArrayDeque<Pair<Int, Int>>()
        stack.addLast(0 to points.lastIndex)

        while (stack.isNotEmpty()) {
            val (start, end) = stack.removeLast()
            if (end - start <= 1) continue

            val dx = xs[end] - xs[start]
            val dy = ys[end] - ys[start]
            val lenSq = dx * dx + dy * dy

            var maxDist = -1.0
            var maxIdx = -1

            for (i in (start + 1) until end) {
                val dist = if (lenSq < 1e-9) {
                    hypot(xs[i] - xs[start], ys[i] - ys[start])
                } else {
                    val t = ((xs[i] - xs[start]) * dx + (ys[i] - ys[start]) * dy) / lenSq
                    val clampedT = t.coerceIn(0.0, 1.0)
                    val projX = xs[start] + clampedT * dx
                    val projY = ys[start] + clampedT * dy
                    hypot(xs[i] - projX, ys[i] - projY)
                }
                if (dist > maxDist) {
                    maxDist = dist
                    maxIdx = i
                }
            }

            if (maxDist > epsilonMeters && maxIdx != -1) {
                keep[maxIdx] = true
                stack.addLast(start to maxIdx)
                stack.addLast(maxIdx to end)
            }
        }

        val result = ArrayList<GpxPoint>()
        for (i in points.indices) {
            if (keep[i]) {
                val p = points[i]
                result.add(GpxPoint(p.lat, p.lon, p.ele))
            }
        }
        return result
    }

    private fun simplifyTrackForMap(points: List<RawGpxPoint>, maxPoints: Int = 2500): List<GpxPoint> {
        if (points.size <= maxPoints) return points.map { GpxPoint(it.lat, it.lon, it.ele) }
        val tolerances = doubleArrayOf(3.0, 5.0, 8.0, 12.0, 18.0)
        var best = simplifyDouglasPeucker(points, tolerances[0])
        for (tol in tolerances) {
            best = simplifyDouglasPeucker(points, tol)
            if (best.size <= maxPoints) return best
        }
        val step = best.size / maxPoints
        return best.filterIndexed { idx, _ -> idx % step == 0 || idx == best.lastIndex }
    }

    /**
     * Procesa el archivo GPX con parser XML nativo:
     * - Lee ÚNICAMENTE <trk><trkseg><trkpt>. Ignora <wpt> y <rtept>.
     * - Si hay varios <trkseg>, los concatena sin sumar el salto inter-segmento a la distancia.
     * - Distancia: Fórmula Haversine 2D (R = 6371008.8 m). Formato 2 decimales.
     * - Desnivel (+ y -): Altitudes originales sin alisar con histéresis de 5 metros (ELEVATION_THRESHOLD_M = 5.0).
     * - Tiempo: Tiempo total (primer a último trkpt) y tiempo en movimiento (velocidad > 0.5 m/s). Si no hay timestamps -> "n/d".
     */
    fun parse(gpxContent: String, fileName: String = "track.gpx"): GpxParseResult {
        val segments = mutableListOf<MutableList<RawGpxPoint>>()
        var currentSegment = mutableListOf<RawGpxPoint>()
        var originName: String? = null
        var destName: String? = null

        var totalTrkptCount = 0
        var ignoredWptCount = 0
        var ignoredRteptCount = 0

        try {
            val saxFactory = SAXParserFactory.newInstance()
            saxFactory.isNamespaceAware = true
            val saxParser = saxFactory.newSAXParser()

            var insideTrk = false
            var insideTrkseg = false
            var insideTrkpt = false

            var currentLat: Double? = null
            var currentLon: Double? = null
            var currentEle = 0.0
            var hasEle = false
            var currentTimeMillis: Long? = null
            val textBuffer = StringBuilder()

            val handler = object : DefaultHandler() {
                override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
                    val tag = (localName?.takeIf { it.isNotEmpty() } ?: qName?.substringAfter(":") ?: "").lowercase()
                    textBuffer.setLength(0)

                    when (tag) {
                        "trk" -> insideTrk = true
                        "trkseg" -> {
                            if (insideTrk) {
                                insideTrkseg = true
                                if (currentSegment.isNotEmpty()) {
                                    segments.add(currentSegment)
                                    currentSegment = mutableListOf()
                                }
                            }
                        }
                        "trkpt" -> {
                            if (insideTrk && insideTrkseg) {
                                insideTrkpt = true
                                var latStr: String? = null
                                var lonStr: String? = null
                                if (attributes != null) {
                                    for (i in 0 until attributes.length) {
                                        val attrName = attributes.getLocalName(i).ifEmpty { attributes.getQName(i) }.lowercase()
                                        if (attrName == "lat") latStr = attributes.getValue(i)
                                        if (attrName == "lon") lonStr = attributes.getValue(i)
                                    }
                                }
                                currentLat = latStr?.toDoubleOrNull()
                                currentLon = lonStr?.toDoubleOrNull()
                                currentEle = 0.0
                                hasEle = false
                                currentTimeMillis = null
                            }
                        }
                        "wpt" -> ignoredWptCount++
                        "rtept" -> ignoredRteptCount++
                    }
                }

                override fun characters(ch: CharArray?, start: Int, length: Int) {
                    if (ch != null && length > 0) {
                        textBuffer.append(ch, start, length)
                    }
                }

                override fun endElement(uri: String?, localName: String?, qName: String?) {
                    val tag = (localName?.takeIf { it.isNotEmpty() } ?: qName?.substringAfter(":") ?: "").lowercase()
                    val text = textBuffer.toString().trim()

                    if (insideTrkpt) {
                        when (tag) {
                            "ele" -> {
                                if (text.isNotEmpty()) {
                                    currentEle = text.toDoubleOrNull() ?: 0.0
                                    hasEle = true
                                }
                            }
                            "time" -> {
                                if (text.isNotEmpty()) {
                                    currentTimeMillis = parseIsoTimestamp(text)
                                }
                            }
                            "trkpt" -> {
                                if (currentLat != null && currentLon != null) {
                                    val pt = RawGpxPoint(currentLat!!, currentLon!!, currentEle, hasEle, currentTimeMillis)
                                    currentSegment.add(pt)
                                    totalTrkptCount++
                                }
                                insideTrkpt = false
                                currentLat = null
                                currentLon = null
                                currentEle = 0.0
                                hasEle = false
                                currentTimeMillis = null
                            }
                        }
                    } else {
                        when (tag) {
                            "name" -> {
                                if (text.isNotEmpty() && !insideTrk) {
                                    if (originName == null) originName = text
                                    else destName = text
                                }
                            }
                            "trkseg" -> {
                                if (insideTrkseg) {
                                    if (currentSegment.isNotEmpty()) {
                                        segments.add(currentSegment)
                                        currentSegment = mutableListOf()
                                    }
                                    insideTrkseg = false
                                }
                            }
                            "trk" -> insideTrk = false
                        }
                    }
                    textBuffer.setLength(0)
                }
            }

            saxParser.parse(InputSource(StringReader(gpxContent)), handler)

            if (currentSegment.isNotEmpty()) {
                segments.add(currentSegment)
                currentSegment = mutableListOf()
            }
        } catch (e: Exception) {
            safeLog("GpxParser", "Error al procesar XML GPX: ${e.message}")
        }

        // 1. DISTANCIA (Haversine 2D por segmentos, sin sumar saltos inter-segmento)
        var totalDistanceMeters = 0.0
        for (segment in segments) {
            for (i in 0 until segment.size - 1) {
                val p1 = segment[i]
                val p2 = segment[i + 1]
                totalDistanceMeters += haversineDistanceMeters(p1.lat, p1.lon, p2.lat, p2.lon)
            }
        }
        val distanceKm = Math.round((totalDistanceMeters / 1000.0) * 100.0) / 100.0

        // 2. DESNIVEL (+ y -) CON ALTITUDES ORIGINALES E HISTÉRESIS ADAPTATIVA (2m barométrico / 5m GPS)
        var accumGain = 0.0
        var accumLoss = 0.0
        var minElevation = Double.MAX_VALUE
        var maxElevation = -Double.MAX_VALUE

        for (segment in segments) {
            val elePoints = segment.filter { it.hasEle }
            if (elePoints.isEmpty()) continue

            val threshold = effectiveElevationThreshold(elePoints)
            var refEle = elePoints[0].ele
            if (refEle < minElevation) minElevation = refEle
            if (refEle > maxElevation) maxElevation = refEle

            for (i in 1 until elePoints.size) {
                val currentEle = elePoints[i].ele
                if (currentEle < minElevation) minElevation = currentEle
                if (currentEle > maxElevation) maxElevation = currentEle

                val diff = currentEle - refEle
                if (abs(diff) >= threshold) {
                    if (diff > 0) {
                        accumGain += diff
                    } else {
                        accumLoss += abs(diff)
                    }
                    refEle = currentEle
                }
            }
        }

        val elevationGainM = Math.round(accumGain).toInt()
        val elevationLossM = Math.round(accumLoss).toInt()
        if (minElevation == Double.MAX_VALUE) {
            minElevation = 0.0
            maxElevation = 0.0
        }

        // 3. TIEMPOS (Tiempo Total y Tiempo en Movimiento con velocidad > 0.5 m/s)
        val allTrackPoints = segments.flatten()
        val timedPoints = allTrackPoints.filter { it.timeMillis != null }
        val firstTime = timedPoints.firstOrNull()?.timeMillis
        val lastTime = timedPoints.lastOrNull()?.timeMillis

        val totalDurationMs = if (firstTime != null && lastTime != null && lastTime > firstTime) {
            lastTime - firstTime
        } else 0L

        var movingDurationMs = 0L
        for (segment in segments) {
            for (i in 0 until segment.size - 1) {
                val p1 = segment[i]
                val p2 = segment[i + 1]
                if (p1.timeMillis != null && p2.timeMillis != null) {
                    val dtMillis = p2.timeMillis - p1.timeMillis
                    if (dtMillis > 0L) {
                        val dtSec = dtMillis / 1000.0
                        val distMeters = haversineDistanceMeters(p1.lat, p1.lon, p2.lat, p2.lon)
                        val speedMs = distMeters / dtSec
                        if (speedMs > MOVING_SPEED_MS) {
                            movingDurationMs += dtMillis
                        }
                    }
                }
            }
        }

        val hasTimestamps = timedPoints.isNotEmpty() && (totalDurationMs > 0L || movingDurationMs > 0L)
        val formattedTime: String
        val estimatedHours: Double

        if (!hasTimestamps) {
            formattedTime = "n/d"
            estimatedHours = 0.0
        } else {
            val timeToFormat = if (movingDurationMs > 0L) movingDurationMs else totalDurationMs
            val hours = timeToFormat / (1000 * 3600)
            val mins = (timeToFormat % (1000 * 3600)) / (1000 * 60)
            formattedTime = if (hours > 0) "${hours}h ${mins.toString().padStart(2, '0')}m" else "${mins}m"
            estimatedHours = timeToFormat / (1000.0 * 3600.0)
        }

        // Resumen de depuración
        val totalSec = totalDurationMs / 1000
        val totalH = totalSec / 3600
        val totalM = (totalSec % 3600) / 60
        val movingSec = movingDurationMs / 1000
        val movingH = movingSec / 3600
        val movingM = (movingSec % 3600) / 60

        val debugLog = buildString {
            appendLine("=== [GPX DEBUG LOG] ===")
            appendLine("Archivo: $fileName")
            appendLine("trkpt leídos (<trk><trkseg><trkpt>): $totalTrkptCount")
            appendLine("wpt ignorados: $ignoredWptCount")
            appendLine("rtept ignorados: $ignoredRteptCount")
            appendLine("Distancia (Haversine 2D): $distanceKm km")
            appendLine("Desnivel positivo (+): +$elevationGainM m")
            appendLine("Desnivel negativo (-): -$elevationLossM m")
            appendLine("Altitud mínima: ${minElevation} m | Altitud máxima: ${maxElevation} m")
            appendLine("Tiempo total (inicio a fin): ${totalH}h ${totalM}m")
            appendLine("Tiempo en movimiento (> 0.5 m/s): ${movingH}h ${movingM}m")
            appendLine("Tiempo formateado: $formattedTime")
            appendLine("========================")
        }
        safeLog("GpxParser", debugLog)

        // Simplificación geométrica (Douglas-Peucker, 3 m) para conservar calles, giros y puentes sin saltos rectos
        val sampledPoints = simplifyTrackForMap(allTrackPoints, maxPoints = 2500)

        return GpxParseResult(
            points = sampledPoints,
            distanceKm = distanceKm,
            elevationGainM = elevationGainM,
            elevationLossM = elevationLossM,
            origin = originName,
            destination = destName,
            estimatedHours = estimatedHours,
            formattedTime = formattedTime,
            totalTrkptCount = totalTrkptCount,
            ignoredWptCount = ignoredWptCount,
            ignoredRteptCount = ignoredRteptCount,
            minElevationM = minElevation,
            maxElevationM = maxElevation,
            totalDurationMs = totalDurationMs,
            movingDurationMs = movingDurationMs,
            debugLog = debugLog
        )
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
