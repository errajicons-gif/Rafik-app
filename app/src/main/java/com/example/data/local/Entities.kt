package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AacCategory {
    DAILY_NEEDS,
    FOOD,
    EMOTIONS,
    HEALTH,
    MOVEMENT,
    CUSTOM
}

enum class LearningDifficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

@Entity(tableName = "aac_pictograms")
data class AacPictogramEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val labelAr: String,
    val labelFr: String,
    val labelEn: String,
    val iconKey: String,
    val isCustom: Boolean = false
)

@Entity(tableName = "saved_expressions")
data class SavedExpressionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val textAr: String,
    val textFr: String,
    val textEn: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "learning_activities")
data class LearningActivityEntity(
    @PrimaryKey val id: String,
    val difficulty: String,
    val categoryIcon: String,
    val titleAr: String,
    val titleFr: String,
    val titleEn: String,
    val lessonAr: String,
    val lessonFr: String,
    val lessonEn: String,
    val questionAr: String,
    val questionFr: String,
    val questionEn: String,
    val optionsAr: String,
    val optionsFr: String,
    val optionsEn: String,
    val correctIndex: Int,
    val isCompleted: Boolean = false,
    val downloadedOffline: Boolean = true
)
