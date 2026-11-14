package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.hardware.SpeechRecognizerManager
import com.example.core.i18n.stringsFor
import com.example.data.local.ModuleId
import com.example.data.local.UserPreferences
import com.example.ui.components.AdaptiveCenteredBox
import com.example.ui.components.AiAccuracyDisclaimer
import com.example.ui.components.FirebaseNoticeBanner
import com.example.ui.components.PermissionGateCard
import com.example.ui.components.RafiqTopBar
import com.example.ui.viewmodel.VoiceMessage
import com.example.ui.viewmodel.VoiceViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceScreen(
    preferences: UserPreferences,
    isOnline: Boolean,
    isSpeaking: Boolean,
    speechManager: SpeechRecognizerManager,
    viewModel: VoiceViewModel,
    onSpeakText: (String) -> Unit,
    onStopSpeaking: () -> Unit,
    onNavigateToModule: (ModuleId) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val strings = stringsFor(preferences.language)

    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val autoSpeak by viewModel.autoSpeak.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val isListening by speechManager.isListening.collectAsStateWithLifecycle()
    val partialText by speechManager.partialText.collectAsStateWithLifecycle()
    val speechError by speechManager.errorMessage.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
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
            speechManager.startListening(preferences.language, continuous = false)
        }
    }

    LaunchedEffect(speechManager) {
        speechManager.finalResults.collect { recognized ->
            viewModel.sendQuery(recognized, preferences.language, preferences.voiceSpeed)
        }
    }

    Scaffold(
        topBar = {
            RafiqTopBar(
                title = strings.voiceTitle,
                strings = strings,
                isOnline = isOnline,
                isSpeaking = isSpeaking,
                onBack = onBack,
                onSpeakScreen = {
                    val lastReply = messages.lastOrNull { !it.isUser }?.text ?: strings.voiceWelcomeMsg
                    onSpeakText(lastReply)
                },
                onStopSpeaking = onStopSpeaking,
                actions = {
                    if (messages.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.clearHistory() },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("voice_clear_button")
                        ) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = strings.clearAll)
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
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FirebaseNoticeBanner(strings = strings)

                // Auto-speak + Quick Command Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(strings.autoSpeakResult, style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = autoSpeak,
                        onCheckedChange = { viewModel.setAutoSpeak(it) },
                        modifier = Modifier.testTag("voice_auto_speak_switch")
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AssistChip(
                        onClick = { onNavigateToModule(ModuleId.VISION) },
                        label = { Text(strings.quickCmdVision) },
                        modifier = Modifier
                            .heightIn(min = 44.dp)
                            .testTag("voice_quick_vision")
                    )
                    AssistChip(
                        onClick = { onNavigateToModule(ModuleId.READER) },
                        label = { Text(strings.quickCmdReader) },
                        modifier = Modifier
                            .heightIn(min = 44.dp)
                            .testTag("voice_quick_reader")
                    )
                    AssistChip(
                        onClick = {
                            viewModel.sendQuery(
                                strings.quickCmdDailyTip,
                                preferences.language,
                                preferences.voiceSpeed
                            )
                        },
                        label = { Text(strings.quickCmdDailyTip) },
                        modifier = Modifier
                            .heightIn(min = 44.dp)
                            .testTag("voice_quick_tip")
                    )
                }

                if (!hasMicPermission) {
                    PermissionGateCard(
                        icon = Icons.Filled.Mic,
                        title = strings.micPermissionTitle,
                        rationale = strings.micPermissionRationale,
                        grantLabel = strings.grantPermission,
                        deniedHint = strings.permissionDeniedHint,
                        onGrantClick = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                    )
                }

                // Conversation List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (messages.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                ),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Text(
                                    text = strings.voiceWelcomeMsg,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }

                    items(messages, key = { it.id }) { msg ->
                        VoiceChatBubble(
                            message = msg,
                            readAloudLabel = strings.readAloud,
                            onSpeak = {
                                viewModel.speakMessage(msg.text, preferences.language, preferences.voiceSpeed)
                            }
                        )
                    }

                    if (isLoading) {
                        item {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp))
                                Text("...", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }

                if (partialText.isNotBlank()) {
                    Text(
                        text = partialText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                val shownError = speechError ?: errorMessage
                if (shownError != null) {
                    Text(
                        text = shownError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Large Voice Button
                Button(
                    onClick = {
                        if (isListening) {
                            speechManager.stopListening()
                        } else if (hasMicPermission) {
                            speechManager.startListening(preferences.language, continuous = false)
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isListening) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 58.dp)
                        .testTag("voice_mic_button"),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isListening) strings.voiceListeningNow else strings.voiceTapToSpeak,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Optional Text Fallback Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text(strings.voiceInputPlaceholder) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("voice_text_input")
                    )
                    Button(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendQuery(inputText, preferences.language, preferences.voiceSpeed)
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .heightIn(min = 54.dp)
                            .testTag("voice_send_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = strings.send)
                    }
                }

                AiAccuracyDisclaimer(strings = strings)
            }
        }
    }
}

@Composable
private fun VoiceChatBubble(
    message: VoiceMessage,
    readAloudLabel: String,
    onSpeak: () -> Unit
) {
    val containerColor = if (message.isUser) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onSpeak,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = readAloudLabel,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
