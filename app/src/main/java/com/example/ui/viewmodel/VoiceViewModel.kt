package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.AppContainer
import com.example.data.ai.AiResult
import com.example.data.ai.ChatTurn
import com.example.data.local.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VoiceMessage(
    val id: Long = System.nanoTime(),
    val isUser: Boolean,
    val text: String,
    val isPlaceholderAi: Boolean = false
)

class VoiceViewModel(private val container: AppContainer) : ViewModel() {

    private val _messages = MutableStateFlow<List<VoiceMessage>>(emptyList())
    val messages: StateFlow<List<VoiceMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _autoSpeak = MutableStateFlow(true)
    val autoSpeak: StateFlow<Boolean> = _autoSpeak.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun setAutoSpeak(enabled: Boolean) {
        _autoSpeak.value = enabled
    }

    fun sendQuery(text: String, language: AppLanguage, voiceSpeed: Float) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _isLoading.value) return

        _errorMessage.value = null
        val userTurn = VoiceMessage(isUser = true, text = trimmed)
        _messages.value = _messages.value + userTurn

        viewModelScope.launch {
            _isLoading.value = true
            val history = _messages.value.map { ChatTurn(it.isUser, it.text) }
            when (val res = container.aiRepository.sendVoiceQuery(trimmed, history, language)) {
                is AiResult.Success -> {
                    val reply = VoiceMessage(
                        isUser = false,
                        text = res.data,
                        isPlaceholderAi = res.isPlaceholder
                    )
                    _messages.value = _messages.value + reply
                    if (_autoSpeak.value) {
                        container.ttsManager.speak(res.data, language, voiceSpeed)
                    }
                }
                is AiResult.Error -> {
                    _errorMessage.value = res.message
                }
            }
            _isLoading.value = false
        }
    }

    fun speakMessage(text: String, language: AppLanguage, voiceSpeed: Float) {
        container.ttsManager.speak(text, language, voiceSpeed)
    }

    fun clearHistory() {
        _messages.value = emptyList()
        _errorMessage.value = null
        container.ttsManager.stop()
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            VoiceViewModel(container) as T
    }
}
