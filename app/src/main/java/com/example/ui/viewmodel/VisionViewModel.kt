package com.example.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.AppContainer
import com.example.data.ai.AiResult
import com.example.data.ai.VisionAnalysis
import com.example.data.ai.VisionAnalysisMode
import com.example.data.local.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface VisionUiState {
    data object Idle : VisionUiState
    data object Analyzing : VisionUiState
    data class Success(val analysis: VisionAnalysis, val isPlaceholder: Boolean) : VisionUiState
    data class Error(val message: String) : VisionUiState
}

class VisionViewModel(private val container: AppContainer) : ViewModel() {

    private val _selectedMode = MutableStateFlow(VisionAnalysisMode.OBJECTS)
    val selectedMode: StateFlow<VisionAnalysisMode> = _selectedMode.asStateFlow()

    private val _capturedBitmap = MutableStateFlow<Bitmap?>(null)
    val capturedBitmap: StateFlow<Bitmap?> = _capturedBitmap.asStateFlow()

    private val _autoSpeak = MutableStateFlow(true)
    val autoSpeak: StateFlow<Boolean> = _autoSpeak.asStateFlow()

    private val _uiState = MutableStateFlow<VisionUiState>(VisionUiState.Idle)
    val uiState: StateFlow<VisionUiState> = _uiState.asStateFlow()

    fun setMode(mode: VisionAnalysisMode) {
        _selectedMode.value = mode
    }

    fun setAutoSpeak(enabled: Boolean) {
        _autoSpeak.value = enabled
    }

    fun onImageSelected(bitmap: Bitmap, language: AppLanguage, voiceSpeed: Float) {
        _capturedBitmap.value = bitmap
        analyzeCurrentImage(language, voiceSpeed)
    }

    fun analyzeCurrentImage(language: AppLanguage, voiceSpeed: Float) {
        val bitmap = _capturedBitmap.value ?: return
        val mode = _selectedMode.value
        viewModelScope.launch {
            _uiState.value = VisionUiState.Analyzing
            when (val res = container.aiRepository.analyzeImage(bitmap, mode, language)) {
                is AiResult.Success -> {
                    _uiState.value = VisionUiState.Success(res.data, res.isPlaceholder)
                    if (_autoSpeak.value) {
                        container.ttsManager.speak(res.data.summary, language, voiceSpeed)
                    }
                }
                is AiResult.Error -> {
                    _uiState.value = VisionUiState.Error(res.message)
                }
            }
        }
    }

    fun clearImage() {
        _capturedBitmap.value = null
        _uiState.value = VisionUiState.Idle
        container.ttsManager.stop()
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            VisionViewModel(container) as T
    }
}
