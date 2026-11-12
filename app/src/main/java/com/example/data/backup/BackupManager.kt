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
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.Normalizer
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class ParsedCaminoData(
    val camino: CaminoEntity,
    val stages: List<StageEntity>,
    val accommodationsByStageIndex: Map<Int, List<AccommodationEntity>>
)

data class ImportConflict(
    val importedTitle: String,
    val existingCamino: CaminoEntity,
    val parsedData: ParsedCaminoData
)

object BackupManager {

    fun normalizeName(s: String): String {
        return Normalizer.normalize(s.trim(), Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
    }

    fun capitalizeFirst(s: String): String {
        val trimmed = s.trim()
        return if (trimmed.isNotEmpty()) {
            trimmed.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        } else trimmed
    }

    /**
     * Serializes caminos to JSON string
     */
    suspend fun serializeCaminosToJson(
        caminos: List<CaminoEntity>,
        repository: CaminoRepository
    ): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 2)
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
        root.toString(2)
    }

    /**
     * Generates a ZIP file containing the backup JSON and shares it
     */
    suspend fun exportToZip(
        context: Context,
        caminos: List<CaminoEntity>,
        zipFileName: String,
        shareSubject: String,
        repository: CaminoRepository
    ): File? = withContext(Dispatchers.IO) {
        try {
            val jsonString = serializeCaminosToJson(caminos, repository)
            val outputDir = File(context.cacheDir, "backups")
            if (!outputDir.exists()) outputDir.mkdirs()

            val zipFile = File(outputDir, if (zipFileName.endsWith(".zip", ignoreCase = true)) zipFileName else "$zipFileName.zip")
            FileOutputStream(zipFile).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    val entry = ZipEntry("ultreia_backup.json")
                    zos.putNextEntry(entry)
                    zos.write(jsonString.toByteArray(Charsets.UTF_8))
                    zos.closeEntry()
                }
            }

            withContext(Dispatchers.Main) {
                shareFile(context, zipFile, "application/zip", shareSubject)
            }
            zipFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Single Camino export as JSON (replaces Exportar CSV)
     */
    suspend fun exportSingleCaminoJson(
        context: Context,
        caminoId: Long,
        repository: CaminoRepository
    ): File? = withContext(Dispatchers.IO) {
        try {
            val camino = repository.getCaminoByIdOnce(caminoId) ?: return@withContext null
            val jsonString = serializeCaminosToJson(listOf(camino), repository)

            val outputDir = File(context.cacheDir, "backups")
            if (!outputDir.exists()) outputDir.mkdirs()

            val sanitized = camino.title.trim()
                .replace("\\s+".toRegex(), "_")
                .replace("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ_-]".toRegex(), "_")
                .ifBlank { "Camino_${camino.id}" }
            val jsonFile = File(outputDir, "${sanitized}_export.json")
            FileOutputStream(jsonFile).use { fos ->
                fos.write(jsonString.toByteArray(Charsets.UTF_8))
            }

            withContext(Dispatchers.Main) {
                shareFile(context, jsonFile, "application/json", "Exportación: ${camino.title}")
            }
            jsonFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Complete backup: all caminos and all travels across both profiles in a single ZIP
     */
    suspend fun exportAllBackupZip(
        context: Context,
        repository: CaminoRepository
    ): File? = withContext(Dispatchers.IO) {
        val allCaminos = repository.getAllCaminosOnce()
        exportToZip(
            context = context,
            caminos = allCaminos,
            zipFileName = "Ultreia_Backup_Completo.zip",
            shareSubject = "Copia de Seguridad Completa - Ultreia (ZIP)",
            repository = repository
        )
    }

    /**
     * Reads file from URI, which can be either a .zip or a .json file
     */
    suspend fun readJsonFromUri(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val headerBytes = ByteArray(4)
            val bufferedStream = BufferedInputStream(inputStream)
            bufferedStream.mark(4)
            val bytesRead = bufferedStream.read(headerBytes, 0, 4)
            bufferedStream.reset()

            // Check if ZIP magic number: 0x50 0x4B 0x03 0x04 ("PK..")
            val isZip = bytesRead == 4 &&
                    headerBytes[0] == 0x50.toByte() &&
                    headerBytes[1] == 0x4B.toByte() &&
                    headerBytes[2] == 0x03.toByte() &&
                    headerBytes[3] == 0x04.toByte()

            if (isZip) {
                ZipInputStream(bufferedStream).use { zis ->
                    var entry: ZipEntry? = zis.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory && entry.name.endsWith(".json", ignoreCase = true)) {
                            return@withContext zis.bufferedReader(Charsets.UTF_8).readText()
                        }
                        entry = zis.nextEntry
                    }
                }
                null
            } else {
                bufferedStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Parses the JSON into structured ParsedCaminoData
     */
    fun parseJsonContent(jsonString: String): List<ParsedCaminoData> {
        val list = mutableListOf<ParsedCaminoData>()
        val root = JSONObject(jsonString)
        val caminosArray = root.optJSONArray("caminos") ?: return emptyList()

        for (i in 0 until caminosArray.length()) {
            val caminoObj = caminosArray.getJSONObject(i)
            val rawTitle = caminoObj.optString("title", "Camino Importado")
            val rawCapTitle = capitalizeFirst(rawTitle)

            val parsedCamino = CaminoEntity(
                id = 0,
                title = rawCapTitle,
                subtitle = caminoObj.optString("subtitle", ""),
                coverPhotoResName = caminoObj.optString("coverPhotoResName", "img_camino_hero"),
                coverPhotoUri = caminoObj.optString("coverPhotoUri").takeIf { it.isNotBlank() },
                pilgrimName = caminoObj.optString("pilgrimName", ""),
                startDate = caminoObj.optString("startDate", ""),
                totalEstimatedKm = caminoObj.optDouble("totalEstimatedKm", 0.0),
                isTravelGuide = caminoObj.optBoolean("isTravelGuide", false),
                attachedFilesJson = caminoObj.optString("attachedFilesJson", "[]")
            )

            val stagesArray = caminoObj.optJSONArray("stages") ?: JSONArray()
            val stagesList = mutableListOf<StageEntity>()
            val accMap = mutableMapOf<Int, MutableList<AccommodationEntity>>()

            for (j in 0 until stagesArray.length()) {
                val stageObj = stagesArray.getJSONObject(j)
                val stage = StageEntity(
                    id = 0,
                    caminoId = 0,
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
                stagesList.add(stage)

                val accArray = stageObj.optJSONArray("accommodations") ?: JSONArray()
                val accList = mutableListOf<AccommodationEntity>()
                for (k in 0 until accArray.length()) {
                    val accObj = accArray.getJSONObject(k)
                    val acc = AccommodationEntity(
                        id = 0,
                        stageId = 0,
                        name = accObj.optString("name", "Alojamiento"),
                        address = accObj.optString("address", ""),
                        phone = accObj.optString("phone", ""),
                        roomOptionsJson = accObj.optString("roomOptionsJson", "[]"),
                        notes = accObj.optString("notes", ""),
                        isBooked = accObj.optBoolean("isBooked", false)
                    )
                    accList.add(acc)
                }
                accMap[j] = accList
            }

            list.add(ParsedCaminoData(parsedCamino, stagesList, accMap))
        }

        return list
    }

    /**
     * Checks if any parsed camino matches existing ones ignoring case and accents
     */
    suspend fun findConflicts(
        parsedList: List<ParsedCaminoData>,
        repository: CaminoRepository
    ): List<ImportConflict> = withContext(Dispatchers.IO) {
        val existing = repository.getAllCaminosOnce()
        val conflicts = mutableListOf<ImportConflict>()

        for (parsed in parsedList) {
            val normParsed = normalizeName(parsed.camino.title)
            val match = existing.firstOrNull { normalizeName(it.title) == normParsed }
            if (match != null) {
                conflicts.add(ImportConflict(parsed.camino.title, match, parsed))
            }
        }
        conflicts
    }

    /**
     * Inserts or overwrites the caminos into repository
     */
    suspend fun applyImport(
        parsedList: List<ParsedCaminoData>,
        overwrittenCaminoIds: Set<Long>,
        repository: CaminoRepository
    ): Int = withContext(Dispatchers.IO) {
        val existing = repository.getAllCaminosOnce()
        var importedCount = 0

        for (parsed in parsedList) {
            val normParsed = normalizeName(parsed.camino.title)
            val match = existing.firstOrNull { normalizeName(it.title) == normParsed }

            val targetCaminoId: Long
            if (match != null && overwrittenCaminoIds.contains(match.id)) {
                // Overwrite existing: delete old stages and update camino
                val oldStages = repository.getStagesForCaminoOnce(match.id)
                for (oldStage in oldStages) {
                    repository.deleteStage(oldStage)
                }
                val updatedCamino = parsed.camino.copy(id = match.id)
                repository.updateCamino(updatedCamino)
                targetCaminoId = match.id
            } else if (match != null) {
                // If conflict but user declined overwrite, skip this item
                continue
            } else {
                // Create new with first letter capitalized
                val capTitle = capitalizeFirst(parsed.camino.title)
                val newCamino = parsed.camino.copy(id = 0, title = capTitle)
                targetCaminoId = repository.insertCamino(newCamino)
            }

            // Insert stages & accommodations
            for ((index, stage) in parsed.stages.withIndex()) {
                val newStage = stage.copy(id = 0, caminoId = targetCaminoId)
                val newStageId = repository.insertStage(newStage)

                val accommodations = parsed.accommodationsByStageIndex[index] ?: emptyList()
                for (acc in accommodations) {
                    val newAcc = acc.copy(id = 0, stageId = newStageId)
                    repository.insertAccommodation(newAcc)
                }
            }
            importedCount++
        }
        importedCount
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

    private fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "Archivo generado con Ultreia.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir con"))
    }
}
