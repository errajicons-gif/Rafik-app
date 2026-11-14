package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.AppContainer
import com.example.data.local.AccessibilityNeed
import com.example.data.local.AppLanguage
import com.example.data.local.FontScaleOption
import com.example.data.local.ModuleId
import com.example.data.local.ThemeMode
import com.example.data.local.UserPreferences
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface VoiceNavEvent {
    data class NavigateToModule(val moduleId: ModuleId) : VoiceNavEvent
    data object OpenSettings : VoiceNavEvent
    data object Unrecognized : VoiceNavEvent
}

class RafiqMainViewModel(
    private val container: AppContainer
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = container.preferencesRepository.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    val isOnline: StateFlow<Boolean> = container.networkMonitor.isOnline
    val isSpeaking: StateFlow<Boolean> = container.ttsManager.isSpeaking

    private val _voiceNavEvents = MutableSharedFlow<VoiceNavEvent>(extraBufferCapacity = 4)
    val voiceNavEvents = _voiceNavEvents.asSharedFlow()

    fun completeOnboarding(
        language: AppLanguage,
        needs: Set<AccessibilityNeed>,
        highContrast: Boolean,
        fontScale: FontScaleOption,
        simplifiedInterface: Boolean
    ) {
        viewModelScope.launch {
            container.preferencesRepository.completeOnboarding(
                language = language,
                needs = needs,
                highContrast = highContrast,
                fontScale = fontScale,
                simplifiedInterface = simplifiedInterface
            )
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { container.preferencesRepository.setLanguage(language) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { container.preferencesRepository.setThemeMode(mode) }
    }

    fun setHighContrast(enabled: Boolean) {
        viewModelScope.launch { container.preferencesRepository.setHighContrast(enabled) }
    }

    fun setFontScale(option: FontScaleOption) {
        viewModelScope.launch { container.preferencesRepository.setFontScale(option) }
    }

    fun setVoiceSpeed(speed: Float) {
        viewModelScope.launch { container.preferencesRepository.setVoiceSpeed(speed) }
    }

    fun setSimplifiedInterface(enabled: Boolean) {
        viewModelScope.launch { container.preferencesRepository.setSimplifiedInterface(enabled) }
    }

    fun setSoundNotifications(enabled: Boolean) {
        viewModelScope.launch { container.preferencesRepository.setSoundNotifications(enabled) }
    }

    fun setVisualNotifications(enabled: Boolean) {
        viewModelScope.launch { container.preferencesRepository.setVisualNotifications(enabled) }
    }

    fun toggleModule(moduleId: ModuleId, enabled: Boolean) {
        viewModelScope.launch { container.preferencesRepository.toggleModule(moduleId, enabled) }
    }

    fun speak(text: String) {
        val prefs = preferences.value
        container.ttsManager.speak(text, prefs.language, prefs.voiceSpeed)
    }

    fun stopSpeaking() {
        container.ttsManager.stop()
    }

    fun handleVoiceNavigationCommand(spokenText: String) {
        val lower = spokenText.lowercase()
        val event = when {
            listOf("عين", "رؤية", "كاميرا", "vision", "camera", "see", "voir").any { it in lower } ->
                VoiceNavEvent.NavigateToModule(ModuleId.VISION)
            listOf("صوت", "مساعد", "محادثة", "voice", "assistant", "voix", "parler").any { it in lower } ->
                VoiceNavEvent.NavigateToModule(ModuleId.VOICE)
            listOf("تواصل", "رموز", "بطاقات", "comm", "communication", "pictog", "speak").any { it in lower } ->
                VoiceNavEvent.NavigateToModule(ModuleId.COMMUNICATION)
            listOf("مستمع", "استماع", "سمع", "listen", "caption", "écoute", "ecoute").any { it in lower } ->
                VoiceNavEvent.NavigateToModule(ModuleId.LISTENER)
            listOf("قارئ", "قراءة", "مستند", "pdf", "read", "lecteur", "lire").any { it in lower } ->
                VoiceNavEvent.NavigateToModule(ModuleId.READER)
            listOf("تعلم", "درس", "تدريب", "learn", "study", "apprend", "apprentissage").any { it in lower } ->
                VoiceNavEvent.NavigateToModule(ModuleId.LEARNING)
            listOf("إعداد", "ضبط", "تخصيص", "setting", "param", "config").any { it in lower } ->
                VoiceNavEvent.OpenSettings
            else -> VoiceNavEvent.Unrecognized
        }
        _voiceNavEvents.tryEmit(event)
    }

    fun deleteAllLocalData(onComplete: () -> Unit) {
        viewModelScope.launch {
            container.ttsManager.stop()
            container.localDataRepository.deleteAllUserData()
            container.preferencesRepository.clearAllPreferences()
            onComplete()
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            val current = preferences.value
            container.preferencesRepository.clearAllPreferences()
            container.preferencesRepository.setLanguage(current.language)
        }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RafiqMainViewModel(container) as T
        }
    }
}
