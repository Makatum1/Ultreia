package com.example.data.sample

import com.example.data.gpx.GpxParser
import com.example.data.model.CaminoEntity
import com.example.data.model.GpxPoint
import com.example.data.model.StageEntity
import com.example.data.repository.CaminoRepository
import com.example.ui.screens.TownCoordinates

data class PredefinedRouteInfo(
    val title: String,
    val subtitle: String,
    val totalKm: Double,
    val note: String? = null,
    val stages: List<PredefinedStageInfo>
)

data class PredefinedStageInfo(
    val dayNumber: Int,
    val title: String,
    val origin: String,
    val destination: String,
    val distanceKm: Double,
    val elevationGainM: Int,
    val elevationLossM: Int,
    val estimatedTime: String,
    val difficulty: String,
    val notes: String = ""
)

object SampleData {

    fun generateTrackPoints(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double,
        startEle: Double,
        endEle: Double,
        numPoints: Int = 25
    ): List<GpxPoint> {
        val points = mutableListOf<GpxPoint>()
        for (i in 0..numPoints) {
            val t = i.toDouble() / numPoints
            val curveFactor = Math.sin(t * Math.PI) * 0.02
            val lat = startLat + (endLat - startLat) * t + curveFactor * 0.5
            val lon = startLon + (endLon - startLon) * t + curveFactor
            val ele = startEle + (endEle - startEle) * t + Math.sin(t * Math.PI) * 120.0
            points.add(GpxPoint(lat = lat, lon = lon, ele = ele))
        }
        return points
    }

    val PREDEFINED_ROUTES = PredefinedRoutesData.ROUTES

    suspend fun createPredefinedCamino(
        routeInfo: PredefinedRouteInfo,
        repository: CaminoRepository,
        startDate: String = "15/05/2026"
    ): Long {
        val isCaminoIngles = routeInfo.title.contains("Inglés", ignoreCase = true) || routeInfo.title.contains("Ingles", ignoreCase = true)
        val initialAttachedFilesJson = if (isCaminoIngles) {
            try {
                val attached = com.example.data.gpx.CaminoInglesGpxProvider.createAttachedFile(repository.context)
                org.json.JSONArray().apply {
                    val obj = org.json.JSONObject().apply {
                        put("id", attached.id)
                        put("name", attached.name)
                        put("uri", attached.uri)
                        put("mimeType", attached.mimeType)
                        put("sizeBytes", attached.sizeBytes)
                        put("addedAt", attached.addedAt)
                    }
                    put(obj)
                }.toString()
            } catch (_: Exception) {
                "[]"
            }
        } else {
            "[]"
        }

        val camino = CaminoEntity(
            title = routeInfo.title,
            subtitle = routeInfo.subtitle,
            coverPhotoResName = "img_camino_hero",
            pilgrimName = "Peregrino Ultreia",
            startDate = startDate,
            totalEstimatedKm = routeInfo.totalKm,
            isTravelGuide = false,
            attachedFilesJson = initialAttachedFilesJson
        )
        val caminoId = repository.insertCamino(camino)

        for (stage in routeInfo.stages) {
            // Generate test GPX track points between start and end coordinates
            val startCoord = TownCoordinates.findCoordinates(stage.origin)
            val endCoord = TownCoordinates.findCoordinates(stage.destination)

            val (sLat, sLon) = when {
                startCoord != null -> startCoord
                endCoord != null -> Pair(endCoord.first + 0.10, endCoord.second + 0.10)
                else -> Pair(42.88 - (stage.dayNumber * 0.05), -8.54 - (stage.dayNumber * 0.08))
            }
            val (eLat, eLon) = when {
                endCoord != null -> endCoord
                startCoord != null -> Pair(startCoord.first - 0.10, startCoord.second - 0.10)
                else -> Pair(sLat - 0.08, sLon - 0.10)
            }

            val testPoints = generateTrackPoints(
                startLat = sLat,
                startLon = sLon,
                endLat = eLat,
                endLon = eLon,
                startEle = 250.0 + (stage.elevationGainM * 0.25),
                endEle = 250.0 + (stage.elevationLossM * 0.20),
                numPoints = 25
            )
            val gpxJson = GpxParser.serializePoints(testPoints)

            val stageEntity = StageEntity(
                caminoId = caminoId,
                dayNumber = stage.dayNumber,
                title = stage.title,
                origin = stage.origin,
                destination = stage.destination,
                distanceKm = stage.distanceKm,
                elevationGainM = stage.elevationGainM,
                elevationLossM = stage.elevationLossM,
                estimatedTime = stage.estimatedTime,
                difficulty = stage.difficulty,
                isCompleted = false,
                stageNotes = stage.notes,
                attachedFilesJson = initialAttachedFilesJson,
                gpxTrackJson = gpxJson
            )
            repository.insertStage(stageEntity)
        }
        return caminoId
    }
}
