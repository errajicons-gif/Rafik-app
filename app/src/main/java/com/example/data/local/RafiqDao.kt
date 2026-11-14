package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RafiqDao {

    @Query("SELECT * FROM aac_pictograms ORDER BY isCustom DESC, id ASC")
    fun getAllPictograms(): Flow<List<AacPictogramEntity>>

    @Query("SELECT COUNT(*) FROM aac_pictograms")
    suspend fun countPictograms(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPictograms(items: List<AacPictogramEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPictogram(item: AacPictogramEntity)

    @Query("DELETE FROM aac_pictograms WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustomPictogram(id: Int)

    @Query("SELECT * FROM saved_expressions ORDER BY createdAt DESC")
    fun getSavedExpressions(): Flow<List<SavedExpressionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpression(expression: SavedExpressionEntity)

    @Query("DELETE FROM saved_expressions WHERE id = :id")
    suspend fun deleteExpression(id: Int)

    @Query("SELECT * FROM learning_activities ORDER BY id ASC")
    fun getAllLearningActivities(): Flow<List<LearningActivityEntity>>

    @Query("SELECT COUNT(*) FROM learning_activities")
    suspend fun countLearningActivities(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningActivities(items: List<LearningActivityEntity>)

    @Query("UPDATE learning_activities SET isCompleted = :completed WHERE id = :id")
    suspend fun markActivityCompleted(id: String, completed: Boolean)

    @Query("DELETE FROM saved_expressions")
    suspend fun clearAllExpressions()

    @Query("DELETE FROM aac_pictograms WHERE isCustom = 1")
    suspend fun clearCustomPictograms()

    @Query("UPDATE learning_activities SET isCompleted = 0")
    suspend fun resetLearningProgress()
}
