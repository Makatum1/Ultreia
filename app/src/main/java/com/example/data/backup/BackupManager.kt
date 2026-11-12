package com.example.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.AccommodationEntity
import com.example.data.model.CaminoEntity
import com.example.data.model.StageEntity
import com.example.data.repository.CaminoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object BackupManager {

    suspend fun exportAllCaminosJson(context: Context, repository: CaminoRepository): File? = withContext(Dispatchers.IO) {
        try {
            val caminos = repository.getAllCaminosOnce()
            val root = JSONObject()
            root.put("version", 1)
            root.put("appName", "Ultreia")
            root.put("exportedAt", System.currentTimeMillis())

            val caminosArray = JSONArray()
            for (camino in caminos) {
                val stages = repository.getStagesForCaminoOnce(camino.id)
                val allAccommodations = repository.getAllAccommodationsForCaminoOnce(camino.id)
                val accMap = allAccommodations.groupBy { it.stageId }

                val caminoObj = serializeCamino(camino, stages, accMap)
                caminosArray.put(caminoObj)
            }
            root.put("caminos", caminosArray)

            val outputDir = File(context.cacheDir, "backups")
            if (!outputDir.exists()) outputDir.mkdirs()
            val backupFile = File(outputDir, "Ultreia_Todos_los_Caminos_Backup.json")
            FileOutputStream(backupFile).use { fos ->
                fos.write(root.toString(2).toByteArray(Charsets.UTF_8))
            }

            withContext(Dispatchers.Main) {
                shareBackupFile(context, backupFile, "Copia de Seguridad Completa - Ultreia")
            }
            backupFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun exportSingleCaminoJson(context: Context, caminoId: Long, repository: CaminoRepository): File? = withContext(Dispatchers.IO) {
        try {
            val camino = repository.getCaminoByIdOnce(caminoId) ?: return@withContext null
            val stages = repository.getStagesForCaminoOnce(camino.id)
            val allAccommodations = repository.getAllAccommodationsForCaminoOnce(camino.id)
            val accMap = allAccommodations.groupBy { it.stageId }

            val root = JSONObject()
            root.put("version", 1)
            root.put("appName", "Ultreia")
            root.put("exportedAt", System.currentTimeMillis())

            val caminosArray = JSONArray()
            caminosArray.put(serializeCamino(camino, stages, accMap))
            root.put("caminos", caminosArray)

            val outputDir = File(context.cacheDir, "backups")
            if (!outputDir.exists()) outputDir.mkdirs()
            val sanitized = camino.title.trim()
                .replace("\\s+".toRegex(), "_")
                .replace("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ_-]".toRegex(), "_")
                .ifBlank { "Camino_${camino.id}" }
            val backupFile = File(outputDir, "${sanitized}_Backup.json")
            FileOutputStream(backupFile).use { fos ->
                fos.write(root.toString(2).toByteArray(Charsets.UTF_8))
            }

            withContext(Dispatchers.Main) {
                shareBackupFile(context, backupFile, "Copia de Seguridad: ${camino.title}")
            }
            backupFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun importFromJson(jsonString: String, repository: CaminoRepository): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            val caminosArray = root.optJSONArray("caminos") ?: return@withContext Result.failure(Exception("Formato no válido: no se encontraron caminos"))
            var importedCount = 0

            for (i in 0 until caminosArray.length()) {
                val caminoObj = caminosArray.getJSONObject(i)
                val newCamino = CaminoEntity(
                    id = 0,
                    title = caminoObj.optString("title", "Camino Importado"),
                    subtitle = caminoObj.optString("subtitle", ""),
                    coverPhotoResName = caminoObj.optString("coverPhotoResName", "img_camino_hero"),
                    coverPhotoUri = caminoObj.optString("coverPhotoUri").takeIf { it.isNotBlank() },
                    pilgrimName = caminoObj.optString("pilgrimName", ""),
                    startDate = caminoObj.optString("startDate", ""),
                    totalEstimatedKm = caminoObj.optDouble("totalEstimatedKm", 0.0),
                    isTravelGuide = caminoObj.optBoolean("isTravelGuide", false),
                    attachedFilesJson = caminoObj.optString("attachedFilesJson", "[]")
                )
                val newCaminoId = repository.insertCamino(newCamino)

                val stagesArray = caminoObj.optJSONArray("stages") ?: JSONArray()
                for (j in 0 until stagesArray.length()) {
                    val stageObj = stagesArray.getJSONObject(j)
                    val newStage = StageEntity(
                        id = 0,
                        caminoId = newCaminoId,
                        dayNumber = stageObj.optInt("dayNumber", j + 1),
                        date = stageObj.optString("date", ""),
                        title = stageObj.optString("title", "Etapa ${j + 1}"),
                        origin = stageObj.optString("origin", ""),
                        destination = stageObj.optString("destination", ""),
                        distanceKm = stageObj.optDouble("distanceKm", 0.0),
                        elevationGainM = stageObj.optInt("elevationGainM", 0),
                        elevationLossM = stageObj.optInt("elevationLossM", 0),
                        estimatedTime = stageObj.optString("estimatedTime", "5h 00m"),
                        difficulty = stageObj.optString("difficulty", "Moderada"),
                        isCompleted = stageObj.optBoolean("isCompleted", false),
                        completedDate = stageObj.optString("completedDate", ""),
                        stageNotes = stageObj.optString("stageNotes", ""),
                        diaryNotes = stageObj.optString("diaryNotes", ""),
                        diaryPhotoUri = stageObj.optString("diaryPhotoUri").takeIf { it.isNotBlank() },
                        vivenciasJson = stageObj.optString("vivenciasJson", "[]"),
                        attachedFilesJson = stageObj.optString("attachedFilesJson", "[]"),
                        gpxTrackJson = stageObj.optString("gpxTrackJson").takeIf { it.isNotBlank() }
                    )
                    val newStageId = repository.insertStage(newStage)

                    val accArray = stageObj.optJSONArray("accommodations") ?: JSONArray()
                    for (k in 0 until accArray.length()) {
                        val accObj = accArray.getJSONObject(k)
                        val newAcc = AccommodationEntity(
                            id = 0,
                            stageId = newStageId,
                            name = accObj.optString("name", "Alojamiento"),
                            address = accObj.optString("address", ""),
                            phone = accObj.optString("phone", ""),
                            roomOptionsJson = accObj.optString("roomOptionsJson", "[]"),
                            notes = accObj.optString("notes", ""),
                            isBooked = accObj.optBoolean("isBooked", false)
                        )
                        repository.insertAccommodation(newAcc)
                    }
                }
                importedCount++
            }
            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun serializeCamino(
        camino: CaminoEntity,
        stages: List<StageEntity>,
        accMap: Map<Long, List<AccommodationEntity>>
    ): JSONObject {
        val obj = JSONObject()
        obj.put("title", camino.title)
        obj.put("subtitle", camino.subtitle)
        obj.put("coverPhotoResName", camino.coverPhotoResName)
        obj.put("coverPhotoUri", camino.coverPhotoUri ?: "")
        obj.put("pilgrimName", camino.pilgrimName)
        obj.put("startDate", camino.startDate)
        obj.put("totalEstimatedKm", camino.totalEstimatedKm)
        obj.put("isTravelGuide", camino.isTravelGuide)
        obj.put("attachedFilesJson", camino.attachedFilesJson)

        val stagesArray = JSONArray()
        for (stage in stages) {
            val stageObj = JSONObject()
            stageObj.put("dayNumber", stage.dayNumber)
            stageObj.put("date", stage.date)
            stageObj.put("title", stage.title)
            stageObj.put("origin", stage.origin)
            stageObj.put("destination", stage.destination)
            stageObj.put("distanceKm", stage.distanceKm)
            stageObj.put("elevationGainM", stage.elevationGainM)
            stageObj.put("elevationLossM", stage.elevationLossM)
            stageObj.put("estimatedTime", stage.estimatedTime)
            stageObj.put("difficulty", stage.difficulty)
            stageObj.put("isCompleted", stage.isCompleted)
            stageObj.put("completedDate", stage.completedDate)
            stageObj.put("stageNotes", stage.stageNotes)
            stageObj.put("diaryNotes", stage.diaryNotes)
            stageObj.put("diaryPhotoUri", stage.diaryPhotoUri ?: "")
            stageObj.put("vivenciasJson", stage.vivenciasJson)
            stageObj.put("attachedFilesJson", stage.attachedFilesJson)
            stageObj.put("gpxTrackJson", stage.gpxTrackJson ?: "")

            val accArray = JSONArray()
            val accommodations = accMap[stage.id] ?: emptyList()
            for (acc in accommodations) {
                val accObj = JSONObject()
                accObj.put("name", acc.name)
                accObj.put("address", acc.address)
                accObj.put("phone", acc.phone)
                accObj.put("roomOptionsJson", acc.roomOptionsJson)
                accObj.put("notes", acc.notes)
                accObj.put("isBooked", acc.isBooked)
                accArray.put(accObj)
            }
            stageObj.put("accommodations", accArray)
            stagesArray.put(stageObj)
        }
        obj.put("stages", stagesArray)
        return obj
    }

    private fun shareBackupFile(context: Context, file: File, title: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "Copia de seguridad en formato JSON generada con Ultreia.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir Copia de Seguridad"))
    }
}
