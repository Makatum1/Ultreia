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
    const val MOVING_SPEED_MS = 0.5

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

        // 2. DESNIVEL (+ y -) CON ALTITUDES ORIGINALES E HISTÉRESIS DE 5 METROS
        var accumGain = 0.0
        var accumLoss = 0.0
        var minElevation = Double.MAX_VALUE
        var maxElevation = -Double.MAX_VALUE

        for (segment in segments) {
            val elePoints = segment.filter { it.hasEle }
            if (elePoints.isEmpty()) continue

            var refEle = elePoints[0].ele
            if (refEle < minElevation) minElevation = refEle
            if (refEle > maxElevation) maxElevation = refEle

            for (i in 1 until elePoints.size) {
                val currentEle = elePoints[i].ele
                if (currentEle < minElevation) minElevation = currentEle
                if (currentEle > maxElevation) maxElevation = currentEle

                val diff = currentEle - refEle
                if (abs(diff) >= ELEVATION_THRESHOLD_M) {
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

        // Puntos simplificados para mapa y perfil de elevación (máximo 300)
        val sampledPoints = if (allTrackPoints.size > 300) {
            val step = allTrackPoints.size / 300
            allTrackPoints.filterIndexed { index, _ -> index % step == 0 || index == allTrackPoints.lastIndex }
                .map { GpxPoint(it.lat, it.lon, it.ele) }
        } else {
            allTrackPoints.map { GpxPoint(it.lat, it.lon, it.ele) }
        }

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
