package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TranscriptEntry(
    val id: Long = System.nanoTime(),
    val speakerNumber: Int,
    val text: String
)

class ListenerViewModel : ViewModel() {

    private val _entries = MutableStateFlow<List<TranscriptEntry>>(emptyList())
    val entries: StateFlow<List<TranscriptEntry>> = _entries.asStateFlow()

    private val _currentSpeaker = MutableStateFlow(1)
    val currentSpeaker: StateFlow<Int> = _currentSpeaker.asStateFlow()

    private val _soundAlertsEnabled = MutableStateFlow(true)
    val soundAlertsEnabled: StateFlow<Boolean> = _soundAlertsEnabled.asStateFlow()

    private val _loudSoundBannerVisible = MutableStateFlow(false)
    val loudSoundBannerVisible: StateFlow<Boolean> = _loudSoundBannerVisible.asStateFlow()

    private val _captionFontSizeSp = MutableStateFlow(26f)
    val captionFontSizeSp: StateFlow<Float> = _captionFontSizeSp.asStateFlow()

    fun appendRecognizedSpeech(text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        val speaker = _currentSpeaker.value
        val current = _entries.value
        val last = current.lastOrNull()
        if (last != null && last.speakerNumber == speaker && last.text.length < 180) {
            _entries.value = current.dropLast(1) + last.copy(text = "${last.text} $clean")
        } else {
            _entries.value = current + TranscriptEntry(speakerNumber = speaker, text = clean)
        }
    }

    fun switchToNextSpeaker() {
        _currentSpeaker.value = if (_currentSpeaker.value >= 4) 1 else _currentSpeaker.value + 1
    }

    fun setSoundAlertsEnabled(enabled: Boolean) {
        _soundAlertsEnabled.value = enabled
        if (!enabled) _loudSoundBannerVisible.value = false
    }

    fun triggerLoudSoundAlert() {
        if (_soundAlertsEnabled.value) {
            _loudSoundBannerVisible.value = true
        }
    }

    fun dismissLoudSoundAlert() {
        _loudSoundBannerVisible.value = false
    }

    fun setCaptionFontSize(sizeSp: Float) {
        _captionFontSizeSp.value = sizeSp.coerceIn(18f, 40f)
    }

    fun clearTranscript() {
        _entries.value = emptyList()
        _currentSpeaker.value = 1
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ListenerViewModel() as T
    }
}
