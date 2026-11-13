package com.example.data.db

import androidx.room.*
import com.example.data.model.AccommodationEntity
import com.example.data.model.CaminoEntity
import com.example.data.model.StageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CaminoDao {

    // Caminos
    @Query("SELECT * FROM caminos ORDER BY createdAt DESC")
    fun getAllCaminos(): Flow<List<CaminoEntity>>

    @Query("SELECT * FROM caminos WHERE id = :id")
    fun getCaminoById(id: Long): Flow<CaminoEntity?>

    @Query("SELECT * FROM caminos WHERE id = :id")
    suspend fun getCaminoByIdOnce(id: Long): CaminoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCamino(camino: CaminoEntity): Long

    @Update
    suspend fun updateCamino(camino: CaminoEntity)

    @Delete
    suspend fun deleteCamino(camino: CaminoEntity)

    // Stages
    @Query("SELECT * FROM stages WHERE caminoId = :caminoId ORDER BY dayNumber ASC")
    fun getStagesForCamino(caminoId: Long): Flow<List<StageEntity>>

    @Query("SELECT * FROM stages WHERE caminoId = :caminoId ORDER BY dayNumber ASC")
    suspend fun getStagesForCaminoOnce(caminoId: Long): List<StageEntity>

    @Query("SELECT * FROM stages WHERE id = :id")
    fun getStageById(id: Long): Flow<StageEntity?>

    @Query("SELECT * FROM stages WHERE id = :id")
    suspend fun getStageByIdOnce(id: Long): StageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStage(stage: StageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStages(stages: List<StageEntity>): List<Long>

    @Update
    suspend fun updateStage(stage: StageEntity)

    @Delete
    suspend fun deleteStage(stage: StageEntity)

    // Accommodations
    @Query("SELECT * FROM accommodations WHERE stageId = :stageId")
    fun getAccommodationsForStage(stageId: Long): Flow<List<AccommodationEntity>>

    @Query("SELECT * FROM accommodations WHERE stageId = :stageId")
    suspend fun getAccommodationsForStageOnce(stageId: Long): List<AccommodationEntity>

    @Query("""
        SELECT a.* FROM accommodations a
        INNER JOIN stages s ON a.stageId = s.id
        WHERE s.caminoId = :caminoId
    """)
    fun getAccommodationsForCamino(caminoId: Long): Flow<List<AccommodationEntity>>

    @Query("""
        SELECT a.* FROM accommodations a
        INNER JOIN stages s ON a.stageId = s.id
        WHERE s.caminoId = :caminoId
    """)
    suspend fun getAllAccommodationsForCaminoOnce(caminoId: Long): List<AccommodationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccommodation(accommodation: AccommodationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccommodations(accommodations: List<AccommodationEntity>): List<Long>

    @Update
    suspend fun updateAccommodation(accommodation: AccommodationEntity)

    @Delete
    suspend fun deleteAccommodation(accommodation: AccommodationEntity)

    @Query("SELECT COUNT(*) FROM caminos")
    suspend fun getCaminosCount(): Int

    @Query("SELECT * FROM caminos ORDER BY createdAt DESC")
    suspend fun getAllCaminosOnce(): List<CaminoEntity>

    @Query("SELECT * FROM stages")
    fun getAllStages(): Flow<List<StageEntity>>

    @Query("SELECT * FROM stages")
    suspend fun getAllStagesOnce(): List<StageEntity>

    @Query("SELECT * FROM accommodations")
    suspend fun getAllAccommodationsOnce(): List<AccommodationEntity>

    @Query("DELETE FROM caminos")
    suspend fun deleteAllCaminos()

    @Query("DELETE FROM stages")
    suspend fun deleteAllStages()

    @Query("DELETE FROM accommodations")
    suspend fun deleteAllAccommodations()
}
