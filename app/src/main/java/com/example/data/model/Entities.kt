package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "caminos")
data class CaminoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val subtitle: String,
    val coverPhotoResName: String = "img_camino_hero",
    val coverPhotoUri: String? = null,
    val pilgrimName: String = "",
    val startDate: String = "",
    val totalEstimatedKm: Double = 0.0,
    val isTravelGuide: Boolean = false,
    val attachedFilesJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis()
)

data class AttachedFile(
    val id: String,
    val name: String,
    val uri: String,
    val mimeType: String = "",
    val sizeBytes: Long = 0,
    val addedAt: Long = System.currentTimeMillis()
)

data class VivenciaItem(
    val id: String,
    val note: String,
    val photoUri: String? = null,
    val dateStr: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "stages",
    foreignKeys = [
        ForeignKey(
            entity = CaminoEntity::class,
            parentColumns = ["id"],
            childColumns = ["caminoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("caminoId")]
)
data class StageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val caminoId: Long,
    val dayNumber: Int,
    val date: String = "",
    val title: String,
    val origin: String,
    val destination: String,
    val distanceKm: Double,
    val elevationGainM: Int,
    val elevationLossM: Int,
    val estimatedTime: String,
    val difficulty: String, // Fácil, Moderada, Difícil, Muy exigente
    val isCompleted: Boolean = false,
    val completedDate: String = "",
    val stageNotes: String = "",
    val diaryNotes: String = "",
    val diaryPhotoUri: String? = null,
    val vivenciasJson: String = "[]",
    val attachedFilesJson: String = "[]",
    val gpxTrackJson: String? = null // JSON array of GpxPoint
)

data class RoomPriceOption(
    val roomType: String,
    val priceEur: Double
)

@Entity(
    tableName = "accommodations",
    foreignKeys = [
        ForeignKey(
            entity = StageEntity::class,
            parentColumns = ["id"],
            childColumns = ["stageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("stageId")]
)
data class AccommodationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val stageId: Long,
    val name: String,
    val address: String,
    val phone: String,
    val roomOptionsJson: String = "[]", // Serialized List<RoomPriceOption>
    val notes: String = "",
    val isBooked: Boolean = false
)

data class GpxPoint(
    val lat: Double,
    val lon: Double,
    val ele: Double = 0.0
)
