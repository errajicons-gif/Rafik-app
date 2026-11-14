package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.i18n.stringsFor
import com.example.data.local.AppLanguage
import com.example.data.local.LearningActivityEntity
import com.example.data.local.LearningDifficulty
import com.example.data.local.UserPreferences
import com.example.ui.components.AdaptiveCenteredBox
import com.example.ui.components.AiAccuracyDisclaimer
import com.example.ui.components.FirebaseNoticeBanner
import com.example.ui.components.ModuleStatusBadge
import com.example.ui.components.RafiqTopBar
import com.example.ui.theme.LearningAccent
import com.example.ui.viewmodel.AiLearningState
import com.example.ui.viewmodel.LearningViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LearningScreen(
    preferences: UserPreferences,
    isOnline: Boolean,
    isSpeaking: Boolean,
    viewModel: LearningViewModel,
    onStopSpeaking: () -> Unit,
    onBack: () -> Unit
) {
    val strings = stringsFor(preferences.language)
    val activities by viewModel.activities.collectAsStateWithLifecycle()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsStateWithLifecycle()
    val activeIndex by viewModel.activeIndex.collectAsStateWithLifecycle()
    val selectedOptionIndex by viewModel.selectedOptionIndex.collectAsStateWithLifecycle()
    val answerResult by viewModel.answerResult.collectAsStateWithLifecycle()
    val aiState by viewModel.aiState.collectAsStateWithLifecycle()

    val filtered = remember(activities, selectedDifficulty) {
        activities.filter { it.difficulty == selectedDifficulty.name }
    }
    val currentActivity = filtered.getOrNull(activeIndex.coerceIn(0, (filtered.size - 1).coerceAtLeast(0)))

    val completedCount = activities.count { it.isCompleted }
    val totalCount = activities.size.coerceAtLeast(1)
    val progress = completedCount.toFloat() / totalCount.toFloat()

    Scaffold(
        topBar = {
            RafiqTopBar(
                title = strings.learningTitle,
                strings = strings,
                isOnline = isOnline,
                isSpeaking = isSpeaking,
                onBack = onBack,
                onSpeakScreen = {
                    currentActivity?.let {
                        val text = "${it.titleFor(preferences.language)}. ${it.lessonFor(preferences.language)}. ${it.questionFor(preferences.language)}"
                        viewModel.speakLesson(text, preferences.language, preferences.voiceSpeed)
                    }
                },
                onStopSpeaking = onStopSpeaking
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
                        accentColor = LearningAccent
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Filled.OfflinePin,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = strings.downloadedOfflineTag,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                // Overall Progress Card
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(strings.progressLabel, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "$completedCount / $totalCount",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                        )
                    }
                }

                // Difficulty Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LearningDifficulty.entries.forEach { diff ->
                        val label = when (diff) {
                            LearningDifficulty.BEGINNER -> strings.diffBeginner
                            LearningDifficulty.INTERMEDIATE -> strings.diffIntermediate
                            LearningDifficulty.ADVANCED -> strings.diffAdvanced
                        }
                        FilterChip(
                            selected = selectedDifficulty == diff,
                            onClick = { viewModel.selectDifficulty(diff) },
                            label = { Text(label) },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("learning_diff_${diff.name.lowercase()}")
                        )
                    }
                }

                // Active Lesson Card
                if (currentActivity != null) {
                    val title = currentActivity.titleFor(preferences.language)
                    val lesson = currentActivity.lessonFor(preferences.language)
                    val question = currentActivity.questionFor(preferences.language)
                    val options = currentActivity.optionsFor(preferences.language)

                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, LearningAccent.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("learning_activity_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                if (currentActivity.isCompleted) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.speakLesson(
                                            "$title. $lesson. $question",
                                            preferences.language,
                                            preferences.voiceSpeed
                                        )
                                    },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("learning_speak_lesson_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = strings.readAloud,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    text = lesson,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }

                            Text(
                                text = question,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            options.forEachIndexed { idx, opt ->
                                val isSelected = selectedOptionIndex == idx
                                Surface(
                                    onClick = {
                                        viewModel.selectOption(idx)
                                        viewModel.speakLesson(opt, preferences.language, preferences.voiceSpeed)
                                    },
                                    shape = MaterialTheme.shapes.medium,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.secondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    },
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 52.dp)
                                        .testTag("learning_option_$idx")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = opt,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }

                            if (answerResult != null) {
                                val positive = answerResult == true
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (positive) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.tertiaryContainer
                                        }
                                    )
                                ) {
                                    Text(
                                        text = if (positive) strings.correctFeedback else strings.tryAgainFeedback,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.checkAnswer(
                                            activity = currentActivity,
                                            language = preferences.language,
                                            voiceSpeed = preferences.voiceSpeed,
                                            correctFeedback = strings.correctFeedback,
                                            tryAgainFeedback = strings.tryAgainFeedback
                                        )
                                    },
                                    enabled = selectedOptionIndex != null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 52.dp)
                                        .testTag("learning_check_button")
                                ) {
                                    Text(strings.checkAnswer)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.nextActivity(filtered.size) },
                                    modifier = Modifier
                                        .heightIn(min = 52.dp)
                                        .testTag("learning_next_button")
                                ) {
                                    Text(strings.nextActivity)
                                    Spacer(Modifier.width(4.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                                }
                            }
                        }
                    }
                }

                // AI Personalized Practice Trigger
                OutlinedButton(
                    onClick = { viewModel.generateAiPractice(preferences.language, preferences.voiceSpeed) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp)
                        .testTag("learning_ai_practice_button")
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(strings.generateAiPractice)
                }

                when (val state = aiState) {
                    AiLearningState.Idle -> Unit
                    AiLearningState.Loading -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("...")
                        }
                    }
                    is AiLearningState.Success -> {
                        if (state.isPlaceholder) {
                            FirebaseNoticeBanner(strings = strings)
                        }
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                            ),
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = state.suggestion.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(state.suggestion.explanation, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = state.suggestion.practiceQuestion,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        AiAccuracyDisclaimer(strings = strings)
                    }
                    is AiLearningState.Error -> {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

private fun LearningActivityEntity.titleFor(lang: AppLanguage) = when (lang) {
    AppLanguage.ARABIC -> titleAr
    AppLanguage.FRENCH -> titleFr
    AppLanguage.ENGLISH -> titleEn
}

private fun LearningActivityEntity.lessonFor(lang: AppLanguage) = when (lang) {
    AppLanguage.ARABIC -> lessonAr
    AppLanguage.FRENCH -> lessonFr
    AppLanguage.ENGLISH -> lessonEn
}

private fun LearningActivityEntity.questionFor(lang: AppLanguage) = when (lang) {
    AppLanguage.ARABIC -> questionAr
    AppLanguage.FRENCH -> questionFr
    AppLanguage.ENGLISH -> questionEn
}

private fun LearningActivityEntity.optionsFor(lang: AppLanguage): List<String> = when (lang) {
    AppLanguage.ARABIC -> optionsAr.split("|")
    AppLanguage.FRENCH -> optionsFr.split("|")
    AppLanguage.ENGLISH -> optionsEn.split("|")
}
