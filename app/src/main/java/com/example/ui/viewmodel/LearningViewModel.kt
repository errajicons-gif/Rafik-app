package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.AppContainer
import com.example.data.ai.AiResult
import com.example.data.ai.LearningSuggestion
import com.example.data.local.AppLanguage
import com.example.data.local.LearningActivityEntity
import com.example.data.local.LearningDifficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AiLearningState {
    data object Idle : AiLearningState
    data object Loading : AiLearningState
    data class Success(val suggestion: LearningSuggestion, val isPlaceholder: Boolean) : AiLearningState
    data class Error(val message: String) : AiLearningState
}

class LearningViewModel(private val container: AppContainer) : ViewModel() {

    val activities: StateFlow<List<LearningActivityEntity>> = container.localDataRepository.learningActivities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDifficulty = MutableStateFlow(LearningDifficulty.BEGINNER)
    val selectedDifficulty: StateFlow<LearningDifficulty> = _selectedDifficulty.asStateFlow()

    private val _activeIndex = MutableStateFlow(0)
    val activeIndex: StateFlow<Int> = _activeIndex.asStateFlow()

    private val _selectedOptionIndex = MutableStateFlow<Int?>(null)
    val selectedOptionIndex: StateFlow<Int?> = _selectedOptionIndex.asStateFlow()

    private val _answerResult = MutableStateFlow<Boolean?>(null)
    val answerResult: StateFlow<Boolean?> = _answerResult.asStateFlow()

    private val _aiState = MutableStateFlow<AiLearningState>(AiLearningState.Idle)
    val aiState: StateFlow<AiLearningState> = _aiState.asStateFlow()

    fun selectDifficulty(difficulty: LearningDifficulty) {
        _selectedDifficulty.value = difficulty
        _activeIndex.value = 0
        _selectedOptionIndex.value = null
        _answerResult.value = null
    }

    fun selectOption(index: Int) {
        _selectedOptionIndex.value = index
        _answerResult.value = null
    }

    fun checkAnswer(
        activity: LearningActivityEntity,
        language: AppLanguage,
        voiceSpeed: Float,
        correctFeedback: String,
        tryAgainFeedback: String
    ) {
        val chosen = _selectedOptionIndex.value ?: return
        val isCorrect = chosen == activity.correctIndex
        _answerResult.value = isCorrect
        if (isCorrect) {
            viewModelScope.launch {
                container.localDataRepository.markActivityCompleted(activity.id, true)
            }
            container.ttsManager.speak(correctFeedback, language, voiceSpeed)
        } else {
            container.ttsManager.speak(tryAgainFeedback, language, voiceSpeed)
        }
    }

    fun nextActivity(totalInDifficulty: Int) {
        if (totalInDifficulty <= 0) return
        _activeIndex.value = (_activeIndex.value + 1) % totalInDifficulty
        _selectedOptionIndex.value = null
        _answerResult.value = null
    }

    fun speakLesson(text: String, language: AppLanguage, voiceSpeed: Float) {
        container.ttsManager.speak(text, language, voiceSpeed)
    }

    fun generateAiPractice(language: AppLanguage, voiceSpeed: Float) {
        viewModelScope.launch {
            _aiState.value = AiLearningState.Loading
            when (val res = container.aiRepository.generateLearningHint(_selectedDifficulty.value, language)) {
                is AiResult.Success -> {
                    _aiState.value = AiLearningState.Success(res.data, res.isPlaceholder)
                    container.ttsManager.speak(res.data.explanation, language, voiceSpeed)
                }
                is AiResult.Error -> {
                    _aiState.value = AiLearningState.Error(res.message)
                }
            }
        }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LearningViewModel(container) as T
    }
}
