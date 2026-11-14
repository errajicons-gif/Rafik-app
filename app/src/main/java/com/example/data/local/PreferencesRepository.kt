package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rafiq_preferences")

enum class AppLanguage(val code: String, val displayLabel: String) {
    ARABIC("ar", "العربية"),
    FRENCH("fr", "Français"),
    ENGLISH("en", "English")
}

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class FontScaleOption(val multiplier: Float) {
    NORMAL(1.0f),
    LARGE(1.18f),
    EXTRA_LARGE(1.36f)
}

enum class AccessibilityNeed {
    VISION,
    HEARING,
    MOTOR,
    SPEECH,
    LEARNING
}

enum class ModuleId {
    VISION,
    VOICE,
    COMMUNICATION,
    LISTENER,
    READER,
    LEARNING
}

data class UserPreferences(
    val onboardingCompleted: Boolean = false,
    val selectedNeeds: Set<AccessibilityNeed> = emptySet(),
    val language: AppLanguage = AppLanguage.ARABIC,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val highContrast: Boolean = false,
    val fontScale: FontScaleOption = FontScaleOption.NORMAL,
    val voiceSpeed: Float = 1.0f,
    val simplifiedInterface: Boolean = false,
    val soundNotifications: Boolean = true,
    val visualNotifications: Boolean = true,
    val enabledModules: Set<ModuleId> = ModuleId.entries.toSet()
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val SELECTED_NEEDS = stringSetPreferencesKey("selected_needs")
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val FONT_SCALE = stringPreferencesKey("font_scale")
        val VOICE_SPEED = floatPreferencesKey("voice_speed")
        val SIMPLIFIED_INTERFACE = booleanPreferencesKey("simplified_interface")
        val SOUND_NOTIFICATIONS = booleanPreferencesKey("sound_notifications")
        val VISUAL_NOTIFICATIONS = booleanPreferencesKey("visual_notifications")
        val ENABLED_MODULES = stringSetPreferencesKey("enabled_modules")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val needs = prefs[Keys.SELECTED_NEEDS]?.mapNotNull {
            runCatching { AccessibilityNeed.valueOf(it) }.getOrNull()
        }?.toSet() ?: emptySet()

        val enabledModules = prefs[Keys.ENABLED_MODULES]?.mapNotNull {
            runCatching { ModuleId.valueOf(it) }.getOrNull()
        }?.toSet()?.ifEmpty { ModuleId.entries.toSet() } ?: ModuleId.entries.toSet()

        UserPreferences(
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            selectedNeeds = needs,
            language = prefs[Keys.LANGUAGE]?.let {
                runCatching { AppLanguage.valueOf(it) }.getOrNull()
            } ?: AppLanguage.ARABIC,
            themeMode = prefs[Keys.THEME_MODE]?.let {
                runCatching { ThemeMode.valueOf(it) }.getOrNull()
            } ?: ThemeMode.SYSTEM,
            highContrast = prefs[Keys.HIGH_CONTRAST] ?: false,
            fontScale = prefs[Keys.FONT_SCALE]?.let {
                runCatching { FontScaleOption.valueOf(it) }.getOrNull()
            } ?: FontScaleOption.NORMAL,
            voiceSpeed = (prefs[Keys.VOICE_SPEED] ?: 1.0f).coerceIn(0.6f, 1.6f),
            simplifiedInterface = prefs[Keys.SIMPLIFIED_INTERFACE] ?: false,
            soundNotifications = prefs[Keys.SOUND_NOTIFICATIONS] ?: true,
            visualNotifications = prefs[Keys.VISUAL_NOTIFICATIONS] ?: true,
            enabledModules = enabledModules
        )
    }

    suspend fun completeOnboarding(
        language: AppLanguage,
        needs: Set<AccessibilityNeed>,
        highContrast: Boolean,
        fontScale: FontScaleOption,
        simplifiedInterface: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = true
            prefs[Keys.LANGUAGE] = language.name
            prefs[Keys.SELECTED_NEEDS] = needs.map { it.name }.toSet()
            prefs[Keys.HIGH_CONTRAST] = highContrast
            prefs[Keys.FONT_SCALE] = fontScale.name
            prefs[Keys.SIMPLIFIED_INTERFACE] = simplifiedInterface
            prefs[Keys.ENABLED_MODULES] = ModuleId.entries.map { it.name }.toSet()
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setHighContrast(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HIGH_CONTRAST] = enabled }
    }

    suspend fun setFontScale(option: FontScaleOption) {
        context.dataStore.edit { it[Keys.FONT_SCALE] = option.name }
    }

    suspend fun setVoiceSpeed(speed: Float) {
        context.dataStore.edit { it[Keys.VOICE_SPEED] = speed.coerceIn(0.6f, 1.6f) }
    }

    suspend fun setSimplifiedInterface(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SIMPLIFIED_INTERFACE] = enabled }
    }

    suspend fun setSoundNotifications(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_NOTIFICATIONS] = enabled }
    }

    suspend fun setVisualNotifications(enabled: Boolean) {
        context.dataStore.edit { it[Keys.VISUAL_NOTIFICATIONS] = enabled }
    }

    suspend fun toggleModule(moduleId: ModuleId, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.ENABLED_MODULES]?.mapNotNull {
                runCatching { ModuleId.valueOf(it) }.getOrNull()
            }?.toMutableSet() ?: ModuleId.entries.toMutableSet()
            if (enabled) current.add(moduleId) else if (current.size > 1) current.remove(moduleId)
            prefs[Keys.ENABLED_MODULES] = current.map { it.name }.toSet()
        }
    }

    suspend fun clearAllPreferences() {
        context.dataStore.edit { it.clear() }
    }
}
