package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.i18n.stringsFor
import com.example.data.local.AppLanguage
import com.example.data.local.FontScaleOption
import com.example.data.local.ModuleId
import com.example.data.local.ThemeMode
import com.example.data.local.UserPreferences
import com.example.ui.components.AdaptiveCenteredBox
import com.example.ui.components.RafiqTopBar
import com.example.ui.viewmodel.RafiqMainViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    isOnline: Boolean,
    isSpeaking: Boolean,
    mainViewModel: RafiqMainViewModel,
    onBack: () -> Unit
) {
    val strings = stringsFor(preferences.language)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            RafiqTopBar(
                title = strings.settings,
                strings = strings,
                isOnline = isOnline,
                isSpeaking = isSpeaking,
                onBack = onBack,
                onSpeakScreen = {
                    mainViewModel.speak("${strings.settings}. ${strings.privacyDescription}")
                },
                onStopSpeaking = { mainViewModel.stopSpeaking() }
            )
        }
    ) { innerPadding ->
        AdaptiveCenteredBox(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Language & Appearance
                SettingsSectionCard(title = strings.appearanceSection) {
                    Text(text = strings.selectLanguageLabel, style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = preferences.language == lang,
                                onClick = { mainViewModel.setLanguage(lang) },
                                label = { Text(lang.displayLabel) },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("settings_lang_${lang.code}")
                            )
                        }
                    }

                    HorizontalDivider()

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            val label = when (mode) {
                                ThemeMode.SYSTEM -> strings.themeSystem
                                ThemeMode.LIGHT -> strings.themeLight
                                ThemeMode.DARK -> strings.themeDark
                            }
                            FilterChip(
                                selected = preferences.themeMode == mode,
                                onClick = { mainViewModel.setThemeMode(mode) },
                                label = { Text(label) },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("settings_theme_${mode.name.lowercase()}")
                            )
                        }
                    }

                    HorizontalDivider()

                    Text(text = strings.fontSizeLabel, style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FontScaleOption.entries.forEach { option ->
                            val label = when (option) {
                                FontScaleOption.NORMAL -> strings.fontNormal
                                FontScaleOption.LARGE -> strings.fontLarge
                                FontScaleOption.EXTRA_LARGE -> strings.fontExtraLarge
                            }
                            FilterChip(
                                selected = preferences.fontScale == option,
                                onClick = { mainViewModel.setFontScale(option) },
                                label = { Text(label) },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("settings_font_${option.name.lowercase()}")
                            )
                        }
                    }

                    HorizontalDivider()

                    SettingsSwitchRow(
                        label = strings.highContrastLabel,
                        checked = preferences.highContrast,
                        testTag = "settings_high_contrast",
                        onCheckedChange = { mainViewModel.setHighContrast(it) }
                    )

                    SettingsSwitchRow(
                        label = strings.simpleLayoutLabel,
                        checked = preferences.simplifiedInterface,
                        testTag = "settings_simplified_interface",
                        onCheckedChange = { mainViewModel.setSimplifiedInterface(it) }
                    )

                    HorizontalDivider()

                    Column {
                        val speedDisplay = ((preferences.voiceSpeed * 10).roundToInt() / 10f)
                        Text(
                            text = "${strings.readingSpeedLabel}: ${speedDisplay}x",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Slider(
                            value = preferences.voiceSpeed,
                            onValueChange = { mainViewModel.setVoiceSpeed(it) },
                            valueRange = 0.6f..1.6f,
                            modifier = Modifier.testTag("settings_voice_speed_slider")
                        )
                    }
                }

                // Modules Visibility
                SettingsSectionCard(title = strings.modulesVisibilitySection) {
                    ModuleId.entries.forEach { moduleId ->
                        val enabled = moduleId in preferences.enabledModules
                        SettingsSwitchRow(
                            label = strings.moduleTitle(moduleId),
                            checked = enabled,
                            testTag = "settings_toggle_${moduleId.name.lowercase()}",
                            onCheckedChange = { mainViewModel.toggleModule(moduleId, it) }
                        )
                    }
                }

                // Notifications
                SettingsSectionCard(title = strings.notificationsSection) {
                    SettingsSwitchRow(
                        label = strings.soundNotificationsLabel,
                        checked = preferences.soundNotifications,
                        testTag = "settings_sound_notifications",
                        onCheckedChange = { mainViewModel.setSoundNotifications(it) }
                    )
                    SettingsSwitchRow(
                        label = strings.visualNotificationsLabel,
                        checked = preferences.visualNotifications,
                        testTag = "settings_visual_notifications",
                        onCheckedChange = { mainViewModel.setVisualNotifications(it) }
                    )
                }

                // Privacy & Local Data Management
                SettingsSectionCard(title = strings.privacySection) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = strings.privacyDescription,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    OutlinedButton(
                        onClick = { mainViewModel.resetOnboarding() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .testTag("settings_rerun_onboarding")
                    ) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.rerunOnboarding)
                    }

                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .testTag("settings_delete_data_button")
                    ) {
                        Icon(imageVector = Icons.Filled.DeleteForever, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.deleteAllDataButton)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(strings.deleteConfirmTitle) },
            text = { Text(strings.deleteConfirmBody) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        mainViewModel.deleteAllLocalData {
                            scope.launch {
                                snackbarHostState.showSnackbar(strings.dataDeletedToast)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_data_button")
                ) {
                    Text(strings.confirmDelete)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    modifier = Modifier.testTag("cancel_delete_data_button")
                ) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    label: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}
