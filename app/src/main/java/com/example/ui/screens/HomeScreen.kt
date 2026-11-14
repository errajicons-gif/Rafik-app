package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.hardware.SpeechRecognizerManager
import com.example.core.i18n.AppStrings
import com.example.core.i18n.stringsFor
import com.example.data.local.ModuleId
import com.example.data.local.UserPreferences
import com.example.ui.components.AdaptiveCenteredBox
import com.example.ui.components.ModuleStatusBadge
import com.example.ui.components.RafiqTopBar
import com.example.ui.theme.CommAccent
import com.example.ui.theme.LearningAccent
import com.example.ui.theme.ListenerAccent
import com.example.ui.theme.ReaderAccent
import com.example.ui.theme.VisionAccent
import com.example.ui.theme.VoiceAccent
import com.example.ui.viewmodel.VoiceNavEvent
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun HomeScreen(
    preferences: UserPreferences,
    isOnline: Boolean,
    isSpeaking: Boolean,
    speechManager: SpeechRecognizerManager,
    voiceNavEvents: SharedFlow<VoiceNavEvent>,
    onSpeakText: (String) -> Unit,
    onStopSpeaking: () -> Unit,
    onVoiceCommandRecognized: (String) -> Unit,
    onOpenModule: (ModuleId) -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val strings = stringsFor(preferences.language)
    val isListening by speechManager.isListening.collectAsStateWithLifecycle()
    val speechError by speechManager.errorMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            speechManager.startListening(preferences.language, continuous = false)
        }
    }

    LaunchedEffect(speechManager) {
        speechManager.finalResults.collect { recognized ->
            if (!isListening || recognized.isNotBlank()) {
                onVoiceCommandRecognized(recognized)
            }
        }
    }

    LaunchedEffect(voiceNavEvents) {
        voiceNavEvents.collect { event ->
            when (event) {
                is VoiceNavEvent.NavigateToModule -> onOpenModule(event.moduleId)
                VoiceNavEvent.OpenSettings -> onOpenSettings()
                VoiceNavEvent.Unrecognized -> snackbarHostState.showSnackbar(strings.voiceNavNotRecognized)
            }
        }
    }

    LaunchedEffect(speechError) {
        speechError?.let {
            snackbarHostState.showSnackbar(it)
            speechManager.clearError()
        }
    }

    val visibleModules = remember(preferences.enabledModules) {
        ModuleId.entries.filter { it in preferences.enabledModules }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            RafiqTopBar(
                title = strings.appTitle,
                strings = strings,
                isOnline = isOnline,
                isSpeaking = isSpeaking,
                onSpeakScreen = {
                    val moduleList = visibleModules.joinToString("، ") { strings.moduleTitle(it) }
                    onSpeakText("${strings.appTitle}. ${strings.homeGreeting}. $moduleList")
                },
                onStopSpeaking = onStopSpeaking,
                actions = {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = strings.settings
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        AdaptiveCenteredBox(modifier = Modifier.padding(innerPadding)) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val columns = when {
                    preferences.simplifiedInterface -> 1
                    maxWidth >= 600.dp -> 2
                    maxWidth >= 340.dp -> 2
                    else -> 1
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("home_modules_grid")
                ) {
                    // Hero Welcome & Voice Navigation Card
                    item(span = { GridItemSpan(columns) }) {
                        HomeHeroCard(
                            strings = strings,
                            isListening = isListening,
                            onVoiceNavClick = {
                                if (isListening) {
                                    speechManager.stopListening()
                                } else {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (granted) {
                                        speechManager.startListening(preferences.language, continuous = false)
                                    } else {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            onPersonalizeClick = onOpenSettings
                        )
                    }

                    // Section Title
                    item(span = { GridItemSpan(columns) }) {
                        Text(
                            text = strings.activeModulesTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // 6 Core Accessibility Modules
                    items(visibleModules, key = { it.name }) { moduleId ->
                        ModuleDashboardCard(
                            moduleId = moduleId,
                            strings = strings,
                            simplifiedMode = preferences.simplifiedInterface,
                            onClick = { onOpenModule(moduleId) }
                        )
                    }

                    item(span = { GridItemSpan(columns) }) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeroCard(
    strings: AppStrings,
    isListening: Boolean,
    onVoiceNavClick: () -> Unit,
    onPersonalizeClick: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .padding(0.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                shape = MaterialTheme.shapes.extraLarge,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_rafiq_emblem_1794686217381),
                            contentDescription = strings.appTitle,
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.appSubtitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = strings.homeGreeting,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onVoiceNavClick,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 52.dp)
                                .testTag("home_voice_nav_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isListening) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isListening) strings.voiceNavListening else strings.voiceNavButton,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        OutlinedButton(
                            onClick = onPersonalizeClick,
                            modifier = Modifier
                                .heightIn(min = 52.dp)
                                .testTag("home_personalize_button")
                        ) {
                            Icon(imageVector = Icons.Filled.Settings, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = strings.personalizeCta, style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    Text(
                        text = strings.voiceNavHint,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ModuleDashboardCard(
    moduleId: ModuleId,
    strings: AppStrings,
    simplifiedMode: Boolean,
    onClick: () -> Unit
) {
    val meta = remember(moduleId) { moduleVisualMeta(moduleId) }
    val minHeight = if (simplifiedMode) 116.dp else 168.dp

    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.5.dp, meta.accent.copy(alpha = 0.28f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .testTag("module_card_${moduleId.name.lowercase()}")
    ) {
        if (simplifiedMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ModuleIconBubble(icon = meta.icon, accent = meta.accent)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = strings.moduleTitle(moduleId),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.moduleSubtitle(moduleId),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ModuleStatusBadge(
                        isOfflineReady = meta.offlineReady,
                        strings = strings,
                        accentColor = meta.accent
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    ModuleIconBubble(icon = meta.icon, accent = meta.accent)
                    ModuleStatusBadge(
                        isOfflineReady = meta.offlineReady,
                        strings = strings,
                        accentColor = meta.accent
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = strings.moduleTitle(moduleId),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.moduleSubtitle(moduleId),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3
                    )
                }
            }
        }
    }
}

@Composable
private fun ModuleIconBubble(icon: ImageVector, accent: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = accent.copy(alpha = 0.14f),
        modifier = Modifier.size(54.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

private data class ModuleVisualMeta(
    val icon: ImageVector,
    val accent: Color,
    val offlineReady: Boolean
)

private fun moduleVisualMeta(moduleId: ModuleId): ModuleVisualMeta = when (moduleId) {
    ModuleId.VISION -> ModuleVisualMeta(Icons.Filled.RemoveRedEye, VisionAccent, offlineReady = false)
    ModuleId.VOICE -> ModuleVisualMeta(Icons.Filled.GraphicEq, VoiceAccent, offlineReady = false)
    ModuleId.COMMUNICATION -> ModuleVisualMeta(Icons.Filled.RecordVoiceOver, CommAccent, offlineReady = true)
    ModuleId.LISTENER -> ModuleVisualMeta(Icons.Filled.Hearing, ListenerAccent, offlineReady = false)
    ModuleId.READER -> ModuleVisualMeta(Icons.AutoMirrored.Filled.MenuBook, ReaderAccent, offlineReady = true)
    ModuleId.LEARNING -> ModuleVisualMeta(Icons.Filled.School, LearningAccent, offlineReady = true)
}
