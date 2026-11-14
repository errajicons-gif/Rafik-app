package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.hardware.DocumentReaderHelper
import com.example.core.hardware.SpeechRecognizerManager
import com.example.core.i18n.stringsFor
import com.example.data.local.AppLanguage
import com.example.data.local.UserPreferences
import com.example.ui.components.AdaptiveCenteredBox
import com.example.ui.components.PermissionGateCard
import com.example.ui.components.RafiqTopBar
import com.example.ui.viewmodel.ListenerViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListenerScreen(
    preferences: UserPreferences,
    isOnline: Boolean,
    isSpeaking: Boolean,
    speechManager: SpeechRecognizerManager,
    viewModel: ListenerViewModel,
    onStopSpeaking: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val strings = stringsFor(preferences.language)

    var recognitionLanguage by remember(preferences.language) {
        mutableStateOf(
            if (preferences.language == AppLanguage.FRENCH) AppLanguage.FRENCH else AppLanguage.ARABIC
        )
    }

    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val currentSpeaker by viewModel.currentSpeaker.collectAsStateWithLifecycle()
    val soundAlertsEnabled by viewModel.soundAlertsEnabled.collectAsStateWithLifecycle()
    val loudSoundBannerVisible by viewModel.loudSoundBannerVisible.collectAsStateWithLifecycle()
    val captionFontSizeSp by viewModel.captionFontSizeSp.collectAsStateWithLifecycle()

    val isListening by speechManager.isListening.collectAsStateWithLifecycle()
    val partialText by speechManager.partialText.collectAsStateWithLifecycle()
    val rmsDb by speechManager.rmsDb.collectAsStateWithLifecycle()
    val speechError by speechManager.errorMessage.collectAsStateWithLifecycle()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (granted) {
            speechManager.startListening(recognitionLanguage, continuous = true)
        }
    }

    LaunchedEffect(speechManager) {
        speechManager.finalResults.collect { text ->
            viewModel.appendRecognizedSpeech(text)
        }
    }

    LaunchedEffect(speechManager, soundAlertsEnabled, preferences.visualNotifications) {
        speechManager.loudSoundAlert.collect {
            if (soundAlertsEnabled) {
                viewModel.triggerLoudSoundAlert()
                if (preferences.visualNotifications) {
                    DocumentReaderHelper.triggerHapticPulse(context)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechManager.stopListening()
        }
    }

    Scaffold(
        topBar = {
            RafiqTopBar(
                title = strings.listenerTitle,
                strings = strings,
                isOnline = isOnline,
                isSpeaking = isSpeaking,
                onBack = onBack,
                onSpeakScreen = null,
                onStopSpeaking = onStopSpeaking,
                actions = {
                    if (entries.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.clearTranscript() },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("listener_clear_button")
                        ) {
                            Icon(Icons.Filled.ClearAll, contentDescription = strings.clearAll)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        AdaptiveCenteredBox(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Visual Loud Sound Alert Banner
                AnimatedVisibility(visible = loudSoundBannerVisible) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("listener_sound_alert_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.NotificationsActive, contentDescription = null)
                            Text(
                                text = strings.loudSoundAlert,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { viewModel.dismissLoudSoundAlert() }) {
                                Text(strings.cancel)
                            }
                        }
                    }
                }

                // Recognition Language & Controls
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppLanguage.entries.forEach { lang ->
                        FilterChip(
                            selected = recognitionLanguage == lang,
                            onClick = {
                                recognitionLanguage = lang
                                if (isListening) {
                                    speechManager.stopListening()
                                    speechManager.startListening(lang, continuous = true)
                                }
                            },
                            label = { Text(lang.displayLabel) },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("listener_lang_${lang.code}")
                        )
                    }
                }

                // Sound Alert Toggle & Caption Font Size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(strings.soundAlertsToggle, style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = soundAlertsEnabled,
                        onCheckedChange = { viewModel.setSoundAlertsEnabled(it) },
                        modifier = Modifier.testTag("listener_alerts_switch")
                    )
                }

                Column {
                    Text(
                        text = "${strings.textSizeLabel}: ${captionFontSizeSp.toInt()}sp",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Slider(
                        value = captionFontSizeSp,
                        onValueChange = { viewModel.setCaptionFontSize(it) },
                        valueRange = 18f..40f,
                        modifier = Modifier.testTag("listener_font_slider")
                    )
                }

                if (isListening) {
                    val normalizedLevel = ((rmsDb + 2f) / 14f).coerceIn(0.05f, 1f)
                    LinearProgressIndicator(
                        progress = { normalizedLevel },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )
                }

                if (!hasMicPermission) {
                    PermissionGateCard(
                        icon = Icons.Filled.Hearing,
                        title = strings.micPermissionTitle,
                        rationale = strings.micPermissionRationale,
                        grantLabel = strings.grantPermission,
                        deniedHint = strings.permissionDeniedHint,
                        onGrantClick = {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    )
                }

                // Live Captions Transcript Area
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (entries.isEmpty() && partialText.isBlank()) {
                            item {
                                Text(
                                    text = strings.listenerEmptyHint,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        items(entries, key = { it.id }) { entry ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = when (entry.speakerNumber) {
                                        1 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        2 -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                        else -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                                    }
                                ),
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${strings.speakerLabel} ${entry.speakerNumber}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = entry.text,
                                        fontSize = captionFontSizeSp.sp,
                                        lineHeight = (captionFontSizeSp * 1.35f).sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        if (partialText.isNotBlank()) {
                            item {
                                Text(
                                    text = "${strings.speakerLabel} $currentSpeaker: $partialText",
                                    fontSize = captionFontSizeSp.sp,
                                    lineHeight = (captionFontSizeSp * 1.35f).sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                if (speechError != null) {
                    Text(
                        text = speechError.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Bottom Controls: Start/Stop + Switch Speaker Turn
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (isListening) {
                                speechManager.stopListening()
                            } else if (hasMicPermission) {
                                speechManager.startListening(recognitionLanguage, continuous = true)
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isListening) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 58.dp)
                            .testTag("listener_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isListening) strings.stopListening else strings.startListening,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.switchToNextSpeaker() },
                        modifier = Modifier
                            .heightIn(min = 58.dp)
                            .testTag("listener_speaker_turn_button")
                    ) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("${strings.newSpeakerTurn} ($currentSpeaker)")
                    }
                }
            }
        }
    }
}
