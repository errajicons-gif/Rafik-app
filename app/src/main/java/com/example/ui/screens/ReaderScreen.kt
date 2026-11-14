package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.i18n.stringsFor
import com.example.data.local.UserPreferences
import com.example.ui.components.AdaptiveCenteredBox
import com.example.ui.components.AiAccuracyDisclaimer
import com.example.ui.components.FirebaseNoticeBanner
import com.example.ui.components.ModuleStatusBadge
import com.example.ui.components.RafiqTopBar
import com.example.ui.theme.ReaderAccent
import com.example.ui.viewmodel.RafiqMainViewModel
import com.example.ui.viewmodel.ReaderViewModel
import com.example.ui.viewmodel.SimplifyState
import kotlin.math.roundToInt

@Composable
fun ReaderScreen(
    preferences: UserPreferences,
    isOnline: Boolean,
    mainViewModel: RafiqMainViewModel,
    viewModel: ReaderViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val strings = stringsFor(preferences.language)

    val documentText by viewModel.documentText.collectAsStateWithLifecycle()
    val sentences by viewModel.sentences.collectAsStateWithLifecycle()
    val fileName by viewModel.fileName.collectAsStateWithLifecycle()
    val pdfPageCount by viewModel.pdfPageCount.collectAsStateWithLifecycle()
    val currentPdfPage by viewModel.currentPdfPage.collectAsStateWithLifecycle()
    val pdfPageBitmap by viewModel.pdfPageBitmap.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val activeSentenceIndex by viewModel.activeSentenceIndex.collectAsStateWithLifecycle()
    val simplifyState by viewModel.simplifyState.collectAsStateWithLifecycle()

    val openDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.openDocument(context, uri, preferences.language)
        }
    }

    Scaffold(
        topBar = {
            RafiqTopBar(
                title = strings.readerTitle,
                strings = strings,
                isOnline = isOnline,
                isSpeaking = isSpeaking,
                onBack = onBack,
                onSpeakScreen = {
                    viewModel.toggleReadAloud(preferences.language, preferences.voiceSpeed)
                },
                onStopSpeaking = {
                    mainViewModel.stopSpeaking()
                }
            )
        }
    ) { innerPadding ->
        AdaptiveCenteredBox(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ModuleStatusBadge(
                        isOfflineReady = true,
                        strings = strings,
                        accentColor = ReaderAccent
                    )
                    if (fileName != null) {
                        Text(
                            text = fileName.orEmpty(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Document & Sample Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            openDocLauncher.launch(arrayOf("application/pdf", "text/plain"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp)
                            .testTag("reader_open_doc_button")
                    ) {
                        Icon(Icons.Filled.FolderOpen, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(strings.openPdfOrTxt)
                    }

                    OutlinedButton(
                        onClick = { viewModel.loadSampleText(preferences.language) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp)
                            .testTag("reader_load_sample_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Article, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(strings.loadSampleText)
                    }
                }

                // PDF Page Preview when PDF is loaded
                if (pdfPageBitmap != null) {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                bitmap = pdfPageBitmap!!.asImageBitmap(),
                                contentDescription = "${strings.pageIndicator} ${currentPdfPage + 1}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.changePdfPage(context, -1) },
                                    enabled = currentPdfPage > 0,
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Icon(Icons.Filled.NavigateBefore, contentDescription = strings.previousPage)
                                    Text(strings.previousPage)
                                }
                                Text(
                                    text = "${strings.pageIndicator} ${currentPdfPage + 1} / $pdfPageCount",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                OutlinedButton(
                                    onClick = { viewModel.changePdfPage(context, 1) },
                                    enabled = currentPdfPage < pdfPageCount - 1,
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Text(strings.nextPage)
                                    Icon(Icons.Filled.NavigateNext, contentDescription = strings.nextPage)
                                }
                            }
                        }
                    }
                }

                // Editable Text Box
                OutlinedTextField(
                    value = documentText,
                    onValueChange = { viewModel.updateText(it) },
                    placeholder = { Text(strings.readerInputHint) },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reader_text_input")
                )

                // Reading Speed Slider
                val speedDisplay = ((preferences.voiceSpeed * 10).roundToInt() / 10f)
                Column {
                    Text(
                        text = "${strings.readingSpeedLabel}: ${speedDisplay}x",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Slider(
                        value = preferences.voiceSpeed,
                        onValueChange = { mainViewModel.setVoiceSpeed(it) },
                        valueRange = 0.6f..1.6f,
                        modifier = Modifier.testTag("reader_speed_slider")
                    )
                }

                // Play/Pause + AI Simplify Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.toggleReadAloud(preferences.language, preferences.voiceSpeed) },
                        enabled = sentences.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 56.dp)
                            .testTag("reader_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (isSpeaking) strings.stopReading else strings.playPauseRead)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.simplifyCurrentText(preferences.language, preferences.voiceSpeed)
                        },
                        enabled = documentText.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 56.dp)
                            .testTag("reader_simplify_button")
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(strings.simplifyWithAi)
                    }
                }

                // Live Sentence Highlighting View
                if (sentences.isNotEmpty()) {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reader_sentences_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            sentences.forEachIndexed { index, sentence ->
                                val isHighlighted = index == activeSentenceIndex
                                Surface(
                                    color = if (isHighlighted) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    },
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.speakFromSentence(
                                                index,
                                                preferences.language,
                                                preferences.voiceSpeed
                                            )
                                        }
                                ) {
                                    Text(
                                        text = sentence,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isHighlighted) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Simplified AI Explanation Result
                when (val sState = simplifyState) {
                    SimplifyState.Idle -> Unit
                    SimplifyState.Loading -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("...", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    is SimplifyState.Success -> {
                        if (sState.isPlaceholder) {
                            FirebaseNoticeBanner(strings = strings)
                        }
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                            ),
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reader_simplified_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = strings.simplifiedSectionTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = sState.text,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        AiAccuracyDisclaimer(strings = strings)
                    }
                    is SimplifyState.Error -> {
                        Text(sState.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
