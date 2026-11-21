package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.gpx.GpxParser
import com.example.data.model.AccommodationEntity
import com.example.data.model.CaminoEntity
import com.example.data.model.RoomPriceOption
import com.example.data.model.StageEntity
import com.example.data.sample.SampleData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class CaminoStats(
    val totalStages: Int,
    val completedStages: Int,
    val totalKm: Double,
    val completedKm: Double,
    val totalElevationGainM: Int,
    val completedElevationGainM: Int,
    val progressPercent: Int
)

class CaminoRepository(val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.caminoDao()

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        dao.deleteAllAccommodations()
        dao.deleteAllStages()
        dao.deleteAllCaminos()
    }

    suspend fun initializeSampleData() = withContext(Dispatchers.IO) {
        // Do not prepopulate sample data: user requested clean slate on start
    }

    val allCaminos: Flow<List<CaminoEntity>> = dao.getAllCaminos()
    val allStages: Flow<List<StageEntity>> = dao.getAllStages()
    val allAccommodations: Flow<List<AccommodationEntity>> = dao.getAllAccommodations()

    suspend fun getAllCaminosOnce(): List<CaminoEntity> = withContext(Dispatchers.IO) {
        dao.getAllCaminosOnce()
    }

    suspend fun getAllStagesOnce(): List<StageEntity> = withContext(Dispatchers.IO) {
        dao.getAllStagesOnce()
    }

    suspend fun getAllAccommodationsOnce(): List<AccommodationEntity> = withContext(Dispatchers.IO) {
        dao.getAllAccommodationsOnce()
    }

    fun getCaminoById(id: Long): Flow<CaminoEntity?> = dao.getCaminoById(id)

    suspend fun getCaminoByIdOnce(id: Long): CaminoEntity? = withContext(Dispatchers.IO) {
        dao.getCaminoByIdOnce(id)
    }

    suspend fun insertCamino(camino: CaminoEntity): Long = withContext(Dispatchers.IO) {
        dao.insertCamino(camino)
    }

    suspend fun updateCamino(camino: CaminoEntity) = withContext(Dispatchers.IO) {
        dao.updateCamino(camino)
    }

    suspend fun deleteCamino(camino: CaminoEntity) = withContext(Dispatchers.IO) {
        dao.deleteCamino(camino)
    }

    fun getStagesForCamino(caminoId: Long): Flow<List<StageEntity>> =
        dao.getStagesForCamino(caminoId)

    suspend fun getStagesForCaminoOnce(caminoId: Long): List<StageEntity> = withContext(Dispatchers.IO) {
        dao.getStagesForCaminoOnce(caminoId)
    }

    fun getStageById(stageId: Long): Flow<StageEntity?> = dao.getStageById(stageId)

    suspend fun getStageByIdOnce(stageId: Long): StageEntity? = withContext(Dispatchers.IO) {
        dao.getStageByIdOnce(stageId)
    }

    suspend fun insertStage(stage: StageEntity): Long = withContext(Dispatchers.IO) {
        dao.insertStage(stage)
    }

    suspend fun updateStage(stage: StageEntity) = withContext(Dispatchers.IO) {
        dao.updateStage(stage)
    }

    suspend fun deleteStage(stage: StageEntity) = withContext(Dispatchers.IO) {
        dao.deleteStage(stage)
    }

    fun getAccommodationsForStage(stageId: Long): Flow<List<AccommodationEntity>> =
        dao.getAccommodationsForStage(stageId)

    fun getAccommodationsForCamino(caminoId: Long): Flow<List<AccommodationEntity>> =
        dao.getAccommodationsForCamino(caminoId)

    suspend fun getAccommodationsForStageOnce(stageId: Long): List<AccommodationEntity> = withContext(Dispatchers.IO) {
        dao.getAccommodationsForStageOnce(stageId)
    }

    suspend fun getAllAccommodationsForCaminoOnce(caminoId: Long): List<AccommodationEntity> = withContext(Dispatchers.IO) {
        dao.getAllAccommodationsForCaminoOnce(caminoId)
    }

    suspend fun insertAccommodation(acc: AccommodationEntity): Long = withContext(Dispatchers.IO) {
        dao.insertAccommodation(acc)
    }

    suspend fun updateAccommodation(acc: AccommodationEntity) = withContext(Dispatchers.IO) {
        dao.updateAccommodation(acc)
    }

    suspend fun deleteAccommodation(acc: AccommodationEntity) = withContext(Dispatchers.IO) {
        dao.deleteAccommodation(acc)
    }

    /**
     * Import GPX content into a Stage: calculates distance, elevation gain/loss,
     * updates origin/destination if available, and stores track points JSON.
     */
    suspend fun importGpxIntoStage(stageId: Long, gpxXmlContent: String, markAsCompleted: Boolean = false): StageEntity? = withContext(Dispatchers.IO) {
        val stage = dao.getStageByIdOnce(stageId) ?: return@withContext null
        val parseResult = GpxParser.parse(gpxXmlContent)

        val updatedOrigin = if (!parseResult.origin.isNullOrBlank()) parseResult.origin else stage.origin
        val updatedDest = if (!parseResult.destination.isNullOrBlank()) parseResult.destination else stage.destination

        val timeStr = if (parseResult.formattedTime.isNotBlank()) parseResult.formattedTime else stage.estimatedTime
        val dist = if (parseResult.distanceKm > 0) parseResult.distanceKm else stage.distanceKm
        val gain = if (parseResult.elevationGainM > 0) parseResult.elevationGainM else stage.elevationGainM
        val loss = if (parseResult.elevationLossM > 0) parseResult.elevationLossM else stage.elevationLossM
        val hours = if (parseResult.estimatedHours > 0) parseResult.estimatedHours else parseHours(timeStr)

        val autoDifficulty = calculateAutoDifficulty(dist, gain, loss, hours)

        val serializedPoints = GpxParser.serializePoints(parseResult.points)

        // No se modifica origen ni destino; se actualizan distancia, desniveles acumulados, tiempo y dificultad calculada
        val updatedStage = stage.copy(
            origin = stage.origin,
            destination = stage.destination,
            distanceKm = dist,
            elevationGainM = gain,
            elevationLossM = loss,
            estimatedTime = timeStr,
            difficulty = autoDifficulty,
            gpxTrackJson = serializedPoints,
            isCompleted = if (markAsCompleted) true else stage.isCompleted
        )

        dao.updateStage(updatedStage)
        updatedStage
    }

    fun parseRoomOptions(json: String?): List<RoomPriceOption> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<RoomPriceOption>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    RoomPriceOption(
                        roomType = obj.optString("roomType", ""),
                        priceEur = obj.optDouble("priceEur", 0.0)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun serializeRoomOptions(options: List<RoomPriceOption>): String {
        val array = JSONArray()
        for (opt in options) {
            val obj = JSONObject()
            obj.put("roomType", opt.roomType)
            obj.put("priceEur", opt.priceEur)
            array.put(obj)
        }
        return array.toString()
    }

    fun calculateStats(stages: List<StageEntity>): CaminoStats {
        val countableStages = stages.filter { it.distanceKm > 0.0 }
        val totalStages = countableStages.size
        val completedStages = countableStages.count { it.isCompleted }
        val totalKm = countableStages.sumOf { it.distanceKm }
        val completedKm = countableStages.filter { it.isCompleted }.sumOf { it.distanceKm }
        val totalElevationGain = countableStages.sumOf { it.elevationGainM }
        val completedElevationGain = countableStages.filter { it.isCompleted }.sumOf { it.elevationGainM }
        val percent = if (totalKm > 0) ((completedKm / totalKm) * 100).toInt() else 0

        return CaminoStats(
            totalStages = totalStages,
            completedStages = completedStages,
            totalKm = (Math.round(totalKm * 10.0) / 10.0),
            completedKm = (Math.round(completedKm * 10.0) / 10.0),
            totalElevationGainM = totalElevationGain,
            completedElevationGainM = completedElevationGain,
            progressPercent = percent
        )
    }

    companion object {
        fun parseHours(timeStr: String): Double {
            try {
                val hMatch = Regex("(\\d+)\\s*h", RegexOption.IGNORE_CASE).find(timeStr)
                val mMatch = Regex("(\\d+)\\s*m", RegexOption.IGNORE_CASE).find(timeStr)
                if (hMatch != null || mMatch != null) {
                    val h = hMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                    val m = mMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                    return h + (m / 60.0)
                }
                val colonParts = timeStr.split(":")
                if (colonParts.size >= 2) {
                    val h = colonParts[0].trim().toDoubleOrNull() ?: 0.0
                    val m = colonParts[1].trim().toDoubleOrNull() ?: 0.0
                    return h + (m / 60.0)
                }
            } catch (_: Exception) {}
            return 0.0
        }

        /**
         * Puntuación para el cálculo de dificultad:
         * Distancia: <15km (0 pts), 15-25km (1 pt), >25km (2 pts)
         * Desnivel +: <300m (0 pts), 300-700m (1 pt), >700m (2 pts)
         * Desnivel -: <300m (0 pts), 300-700m (1 pt), >700m (2 pts)
         * Tiempo: <4h (0 pts), 4-6h (1 pt), >6h (2 pts)
         * Suma de puntos: 0-2 (Fácil), 3-4 (Moderado), 5-6 (Exigente), 7+ (Muy Difícil)
         */
        fun calculateAutoDifficulty(distanceKm: Double, elevationGainM: Int, elevationLossM: Int, hours: Double): String {
            var points = 0
            points += when {
                distanceKm < 15.0 -> 0
                distanceKm <= 25.0 -> 1
                else -> 2
            }
            points += when {
                elevationGainM < 300 -> 0
                elevationGainM <= 700 -> 1
                else -> 2
            }
            points += when {
                elevationLossM < 300 -> 0
                elevationLossM <= 700 -> 1
                else -> 2
            }
            points += when {
                hours < 4.0 -> 0
                hours <= 6.0 -> 1
                else -> 2
            }

            return when {
                points <= 2 -> "Fácil"
                points <= 4 -> "Moderado"
                points <= 6 -> "Exigente"
                else -> "Muy Difícil"
            }
        }
    }
}
