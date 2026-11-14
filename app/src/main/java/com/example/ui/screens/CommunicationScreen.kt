package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MoodBad
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.i18n.stringsFor
import com.example.data.local.AacCategory
import com.example.data.local.AacPictogramEntity
import com.example.data.local.UserPreferences
import com.example.ui.components.AdaptiveCenteredBox
import com.example.ui.components.ModuleStatusBadge
import com.example.ui.components.RafiqTopBar
import com.example.ui.theme.CommAccent
import com.example.ui.viewmodel.CommTab
import com.example.ui.viewmodel.CommunicationViewModel
import com.example.ui.viewmodel.labelFor
import com.example.ui.viewmodel.textFor
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommunicationScreen(
    preferences: UserPreferences,
    isOnline: Boolean,
    isSpeaking: Boolean,
    viewModel: CommunicationViewModel,
    onStopSpeaking: () -> Unit,
    onBack: () -> Unit
) {
    val strings = stringsFor(preferences.language)
    val pictograms by viewModel.pictograms.collectAsStateWithLifecycle()
    val savedExpressions by viewModel.savedExpressions.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val sentenceTokens by viewModel.sentenceTokens.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showAddCardDialog by remember { mutableStateOf(false) }
    var customCardInput by remember { mutableStateOf("") }

    val filteredPictograms = remember(pictograms, selectedTab) {
        when (selectedTab) {
            CommTab.ALL -> pictograms
            CommTab.DAILY_NEEDS -> pictograms.filter {
                it.category == AacCategory.DAILY_NEEDS.name || it.isCustom
            }
            CommTab.FOOD -> pictograms.filter { it.category == AacCategory.FOOD.name }
            CommTab.EMOTIONS -> pictograms.filter { it.category == AacCategory.EMOTIONS.name }
            CommTab.HEALTH -> pictograms.filter { it.category == AacCategory.HEALTH.name }
            CommTab.MOVEMENT -> pictograms.filter { it.category == AacCategory.MOVEMENT.name }
            CommTab.SAVED -> emptyList()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            RafiqTopBar(
                title = strings.commTitle,
                strings = strings,
                isOnline = isOnline,
                isSpeaking = isSpeaking,
                onBack = onBack,
                onSpeakScreen = {
                    viewModel.speakFullSentence(preferences.language, preferences.voiceSpeed)
                },
                onStopSpeaking = onStopSpeaking
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCardDialog = true },
                modifier = Modifier.testTag("comm_add_card_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = strings.addCustomCard)
            }
        }
    ) { innerPadding ->
        AdaptiveCenteredBox(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ModuleStatusBadge(
                        isOfflineReady = true,
                        strings = strings,
                        accentColor = CommAccent
                    )
                }

                // Sentence Builder Strip
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (sentenceTokens.isEmpty()) {
                            Text(
                                text = strings.sentenceStripEmpty,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                sentenceTokens.forEach { token ->
                                    AssistChip(
                                        onClick = {
                                            viewModel.speakText(
                                                token.labelFor(preferences.language),
                                                preferences.language,
                                                preferences.voiceSpeed
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = pictogramIcon(token.iconKey),
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = token.labelFor(preferences.language),
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.speakFullSentence(preferences.language, preferences.voiceSpeed)
                                },
                                enabled = sentenceTokens.isNotEmpty(),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 50.dp)
                                    .testTag("comm_speak_sentence_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text(strings.speakSentence)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.saveCurrentSentence()
                                    scope.launch {
                                        snackbarHostState.showSnackbar(strings.savedSuccess)
                                    }
                                },
                                enabled = sentenceTokens.isNotEmpty(),
                                modifier = Modifier
                                    .heightIn(min = 50.dp)
                                    .testTag("comm_save_sentence_button")
                            ) {
                                Icon(Icons.Filled.BookmarkAdd, contentDescription = strings.saveExpression)
                            }

                            OutlinedButton(
                                onClick = { viewModel.removeLastToken() },
                                enabled = sentenceTokens.isNotEmpty(),
                                modifier = Modifier
                                    .heightIn(min = 50.dp)
                                    .testTag("comm_backspace_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = strings.backspace)
                            }

                            OutlinedButton(
                                onClick = { viewModel.clearSentence() },
                                enabled = sentenceTokens.isNotEmpty(),
                                modifier = Modifier
                                    .heightIn(min = 50.dp)
                                    .testTag("comm_clear_button")
                            ) {
                                Icon(Icons.Filled.ClearAll, contentDescription = strings.clearAll)
                            }
                        }
                    }
                }

                // Category Filter Tabs
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CommTab.entries) { tab ->
                        val label = when (tab) {
                            CommTab.ALL -> strings.catAll
                            CommTab.DAILY_NEEDS -> strings.catDaily
                            CommTab.FOOD -> strings.catFood
                            CommTab.EMOTIONS -> strings.catEmotions
                            CommTab.HEALTH -> strings.catHealth
                            CommTab.MOVEMENT -> strings.catMovement
                            CommTab.SAVED -> strings.catSaved
                        }
                        FilterChip(
                            selected = selectedTab == tab,
                            onClick = { viewModel.selectTab(tab) },
                            label = { Text(label) },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("comm_tab_${tab.name.lowercase()}")
                        )
                    }
                }

                // Board Content
                if (selectedTab == CommTab.SAVED) {
                    if (savedExpressions.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = strings.noSavedExpressions,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(18.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(savedExpressions, key = { it.id }) { expr ->
                                val exprText = expr.textFor(preferences.language)
                                Card(
                                    onClick = {
                                        viewModel.speakText(exprText, preferences.language, preferences.voiceSpeed)
                                    },
                                    shape = MaterialTheme.shapes.large,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = strings.readAloud,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = exprText,
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { viewModel.deleteSavedExpression(expr.id) },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = strings.clearAll,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    BoxWithConstraints(modifier = Modifier.weight(1f)) {
                        val columns = when {
                            preferences.simplifiedInterface -> 2
                            maxWidth >= 600.dp -> 4
                            else -> 2
                        }
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(filteredPictograms, key = { it.id }) { item ->
                                PictogramCard(
                                    item = item,
                                    label = item.labelFor(preferences.language),
                                    onClick = {
                                        viewModel.appendPictogram(
                                            item,
                                            preferences.language,
                                            preferences.voiceSpeed
                                        )
                                    },
                                    onDeleteCustom = if (item.isCustom) {
                                        { viewModel.deleteCustomCard(item.id) }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddCardDialog) {
        AlertDialog(
            onDismissRequest = { showAddCardDialog = false },
            title = { Text(strings.addCustomCard) },
            text = {
                OutlinedTextField(
                    value = customCardInput,
                    onValueChange = { customCardInput = it },
                    placeholder = { Text(strings.customCardLabelHint) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("comm_custom_card_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addCustomCard(customCardInput, preferences.language)
                        customCardInput = ""
                        showAddCardDialog = false
                    },
                    modifier = Modifier.testTag("comm_custom_card_save")
                ) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCardDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
private fun PictogramCard(
    item: AacPictogramEntity,
    label: String,
    onClick: () -> Unit,
    onDeleteCustom: (() -> Unit)?
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 122.dp)
            .testTag("aac_card_${item.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (onDeleteCustom != null) {
                IconButton(
                    onClick = onDeleteCustom,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .minimumInteractiveComponentSize()
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = pictogramIcon(item.iconKey),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun pictogramIcon(key: String): ImageVector = when (key) {
    "help" -> Icons.Filled.Help
    "yes" -> Icons.Filled.CheckCircle
    "no" -> Icons.Filled.Cancel
    "thanks" -> Icons.Filled.ThumbUp
    "rest" -> Icons.Filled.Bedtime
    "bathroom" -> Icons.Filled.Wc
    "water" -> Icons.Filled.WaterDrop
    "food" -> Icons.Filled.Restaurant
    "fruit" -> Icons.Filled.Eco
    "drink" -> Icons.Filled.LocalCafe
    "happy" -> Icons.Filled.SentimentSatisfiedAlt
    "tired" -> Icons.Filled.MoodBad
    "worried" -> Icons.Filled.SentimentVeryDissatisfied
    "love" -> Icons.Filled.Favorite
    "pain" -> Icons.Filled.Healing
    "medicine" -> Icons.Filled.Medication
    "doctor" -> Icons.Filled.LocalHospital
    "better" -> Icons.Filled.ThumbUp
    "go_out" -> Icons.Filled.DirectionsWalk
    "home" -> Icons.Filled.Home
    "stop" -> Icons.Filled.PanTool
    "slow" -> Icons.Filled.DirectionsWalk
    else -> Icons.Filled.Star
}
