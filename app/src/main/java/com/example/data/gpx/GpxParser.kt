package com.example.data.gpx

import com.example.data.model.GpxPoint
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.io.StringReader
import kotlin.math.*

data class GpxParseResult(
    val points: List<GpxPoint>,
    val distanceKm: Double,
    val elevationGainM: Int,
    val elevationLossM: Int,
    val origin: String?,
    val destination: String?,
    val estimatedHours: Double
)

object GpxParser {

    /**
     * Parses standard GPX 1.0 / 1.1 XML string and computes track metrics
     */
    fun parse(gpxContent: String): GpxParseResult {
        val points = mutableListOf<GpxPoint>()
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
                            insideTrkpt = true
                        }
                    }
                    XmlPullParser.TEXT -> {
                        val text = parser.text?.trim() ?: ""
                        if (insideTrkpt && currentTag == "ele" && text.isNotEmpty()) {
                            currentEle = text.toDoubleOrNull() ?: 0.0
                        } else if (currentTag == "name" && text.isNotEmpty()) {
                            if (originName == null) originName = text
                            else destName = text
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        val endTag = parser.name?.substringAfter(":")?.lowercase() ?: ""
                        if (endTag == "trkpt" || endTag == "wpt") {
                            if (currentLat != null && currentLon != null) {
                                points.add(GpxPoint(currentLat, currentLon, currentEle))
                            }
                            insideTrkpt = false
                            currentLat = null
                            currentLon = null
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
                if (lat != null && lon != null) {
                    points.add(GpxPoint(lat, lon, ele))
                }
            }
            if (points.isEmpty()) {
                val simpleTrkpt = """<trkpt[^>]*lat=["']([-0-9.]+)["'][^>]*lon=["']([-0-9.]+)["']""".toRegex()
                for (m in simpleTrkpt.findAll(gpxContent)) {
                    val lat = m.groupValues[1].toDoubleOrNull()
                    val lon = m.groupValues[2].toDoubleOrNull()
                    if (lat != null && lon != null) {
                        points.add(GpxPoint(lat, lon, 0.0))
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

        // Calculate total distance & elevation gain/loss
        var totalDistanceKm = 0.0
        var gainM = 0.0
        var lossM = 0.0

        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            totalDistanceKm += haversineDistanceKm(p1.lat, p1.lon, p2.lat, p2.lon)

            val dEle = p2.ele - p1.ele
            if (dEle > 1.0) gainM += dEle
            else if (dEle < -1.0) lossM += abs(dEle)
        }

        // Estimated hiking time: ~4.2 km/h + 1h per 400m elevation gain
        val estimatedHours = if (totalDistanceKm > 0) {
            (totalDistanceKm / 4.2) + (gainM / 400.0)
        } else {
            0.0
        }

        return GpxParseResult(
            points = sampledPoints,
            distanceKm = (round(totalDistanceKm * 10.0) / 10.0),
            elevationGainM = gainM.roundToInt(),
            elevationLossM = lossM.roundToInt(),
            origin = originName,
            destination = destName,
            estimatedHours = estimatedHours
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
