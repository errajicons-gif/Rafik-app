package com.example.data.ai

import android.graphics.Bitmap
import com.example.data.local.AppLanguage
import com.example.data.local.LearningDifficulty

enum class VisionAnalysisMode {
    OBJECTS,
    SCENE,
    OCR_TEXT,
    COLORS
}

data class ChatTurn(
    val isUser: Boolean,
    val text: String
)

data class VisionAnalysis(
    val summary: String,
    val details: List<String>,
    val isPlaceholder: Boolean = true
)

data class LearningSuggestion(
    val title: String,
    val explanation: String,
    val practiceQuestion: String,
    val options: List<String>,
    val correctIndex: Int,
    val isPlaceholder: Boolean = true
)

sealed interface AiResult<out T> {
    data class Success<T>(val data: T, val isPlaceholder: Boolean = true) : AiResult<T>
    data class Error(val message: String) : AiResult<Nothing>
}

interface AiRepository {
    val requiresFirebaseSetup: Boolean
    suspend fun analyzeImage(
        bitmap: Bitmap,
        mode: VisionAnalysisMode,
        language: AppLanguage
    ): AiResult<VisionAnalysis>

    suspend fun sendVoiceQuery(
        query: String,
        history: List<ChatTurn>,
        language: AppLanguage
    ): AiResult<String>

    suspend fun simplifyText(
        text: String,
        language: AppLanguage
    ): AiResult<String>

    suspend fun generateLearningHint(
        difficulty: LearningDifficulty,
        language: AppLanguage
    ): AiResult<LearningSuggestion>
}
