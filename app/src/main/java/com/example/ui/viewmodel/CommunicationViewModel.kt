package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.AppContainer
import com.example.data.local.AacPictogramEntity
import com.example.data.local.AppLanguage
import com.example.data.local.SavedExpressionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CommTab {
    ALL,
    DAILY_NEEDS,
    FOOD,
    EMOTIONS,
    HEALTH,
    MOVEMENT,
    SAVED
}

class CommunicationViewModel(private val container: AppContainer) : ViewModel() {

    val pictograms: StateFlow<List<AacPictogramEntity>> = container.localDataRepository.pictograms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedExpressions: StateFlow<List<SavedExpressionEntity>> = container.localDataRepository.savedExpressions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTab = MutableStateFlow(CommTab.ALL)
    val selectedTab: StateFlow<CommTab> = _selectedTab.asStateFlow()

    private val _sentenceTokens = MutableStateFlow<List<AacPictogramEntity>>(emptyList())
    val sentenceTokens: StateFlow<List<AacPictogramEntity>> = _sentenceTokens.asStateFlow()

    fun selectTab(tab: CommTab) {
        _selectedTab.value = tab
    }

    fun appendPictogram(item: AacPictogramEntity, language: AppLanguage, voiceSpeed: Float) {
        _sentenceTokens.value = _sentenceTokens.value + item
        val word = item.labelFor(language)
        container.ttsManager.speak(word, language, voiceSpeed)
    }

    fun removeLastToken() {
        val current = _sentenceTokens.value
        if (current.isNotEmpty()) {
            _sentenceTokens.value = current.dropLast(1)
        }
    }

    fun clearSentence() {
        _sentenceTokens.value = emptyList()
    }

    fun speakFullSentence(language: AppLanguage, voiceSpeed: Float) {
        val text = _sentenceTokens.value.joinToString(" ") { it.labelFor(language) }
        if (text.isNotBlank()) {
            container.ttsManager.speak(text, language, voiceSpeed)
        }
    }

    fun speakText(text: String, language: AppLanguage, voiceSpeed: Float) {
        if (text.isNotBlank()) {
            container.ttsManager.speak(text, language, voiceSpeed)
        }
    }

    fun saveCurrentSentence() {
        val tokens = _sentenceTokens.value
        if (tokens.isEmpty()) return
        val ar = tokens.joinToString(" ") { it.labelAr }
        val fr = tokens.joinToString(" ") { it.labelFr }
        val en = tokens.joinToString(" ") { it.labelEn }
        viewModelScope.launch {
            container.localDataRepository.saveExpression(ar, fr, en)
        }
    }

    fun deleteSavedExpression(id: Int) {
        viewModelScope.launch {
            container.localDataRepository.deleteExpression(id)
        }
    }

    fun addCustomCard(label: String, language: AppLanguage) {
        val trimmed = label.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            container.localDataRepository.addCustomPictogram(trimmed, language)
        }
    }

    fun deleteCustomCard(id: Int) {
        viewModelScope.launch {
            container.localDataRepository.deleteCustomPictogram(id)
        }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CommunicationViewModel(container) as T
    }
}

fun AacPictogramEntity.labelFor(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> labelAr
    AppLanguage.FRENCH -> labelFr
    AppLanguage.ENGLISH -> labelEn
}

fun SavedExpressionEntity.textFor(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> textAr
    AppLanguage.FRENCH -> textFr
    AppLanguage.ENGLISH -> textEn
}
