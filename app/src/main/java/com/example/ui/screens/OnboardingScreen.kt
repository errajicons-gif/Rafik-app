package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.i18n.stringsFor
import com.example.data.local.AccessibilityNeed
import com.example.data.local.AppLanguage
import com.example.data.local.FontScaleOption
import com.example.ui.components.AdaptiveCenteredBox

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    initialLanguage: AppLanguage,
    onLanguagePreviewChange: (AppLanguage) -> Unit,
    onContrastPreviewChange: (Boolean) -> Unit,
    onFontScalePreviewChange: (FontScaleOption) -> Unit,
    onSpeakWelcome: (String) -> Unit,
    onComplete: (
        language: AppLanguage,
        needs: Set<AccessibilityNeed>,
        highContrast: Boolean,
        fontScale: FontScaleOption,
        simplifiedInterface: Boolean
    ) -> Unit
) {
    var selectedLang by remember(initialLanguage) { mutableStateOf(initialLanguage) }
    var selectedNeeds by remember { mutableStateOf(setOf<AccessibilityNeed>()) }
    var highContrast by remember { mutableStateOf(false) }
    var largeText by remember { mutableStateOf(false) }
    var simplified by remember { mutableStateOf(false) }

    val strings = stringsFor(selectedLang)

    Scaffold { innerPadding ->
        AdaptiveCenteredBox(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Hero Illustration & Logo Header
                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_rafiq_onboarding_1794686217758),
                            contentDescription = strings.onboardingWelcomeTitle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                                .clip(RoundedCornerShape(18.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.onboardingWelcomeTitle,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = strings.onboardingWelcomeBody,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    onSpeakWelcome("${strings.onboardingWelcomeTitle}. ${strings.onboardingWelcomeBody}")
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("onboarding_speak_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = strings.readAloud,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Step 1: Language Selector
                Text(
                    text = strings.selectLanguageLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppLanguage.entries.forEach { lang ->
                        FilterChip(
                            selected = selectedLang == lang,
                            onClick = {
                                selectedLang = lang
                                onLanguagePreviewChange(lang)
                            },
                            label = {
                                Text(
                                    text = lang.displayLabel,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("onboarding_lang_${lang.code}")
                        )
                    }
                }

                // Step 2: Accessibility Needs Multi-Select
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = strings.selectNeedsTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.selectNeedsSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AccessibilityNeed.entries.forEach { need ->
                        val isSelected = need in selectedNeeds
                        val icon = needIcon(need)
                        Surface(
                            onClick = {
                                selectedNeeds = if (isSelected) selectedNeeds - need else selectedNeeds + need
                                if (need == AccessibilityNeed.VISION && !isSelected) {
                                    largeText = true
                                    onFontScalePreviewChange(FontScaleOption.LARGE)
                                }
                                if ((need == AccessibilityNeed.MOTOR || need == AccessibilityNeed.LEARNING) && !isSelected) {
                                    simplified = true
                                }
                            },
                            shape = MaterialTheme.shapes.medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .testTag("need_option_${need.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = strings.needLabel(need),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // Step 3: Instant Visual Comfort Toggles
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OnboardingToggleRow(
                            icon = Icons.Filled.Contrast,
                            label = strings.highContrastLabel,
                            checked = highContrast,
                            testTag = "onboarding_high_contrast",
                            onCheckedChange = {
                                highContrast = it
                                onContrastPreviewChange(it)
                            }
                        )
                        OnboardingToggleRow(
                            icon = Icons.Filled.FormatSize,
                            label = strings.largeTextLabel,
                            checked = largeText,
                            testTag = "onboarding_large_text",
                            onCheckedChange = {
                                largeText = it
                                onFontScalePreviewChange(if (it) FontScaleOption.LARGE else FontScaleOption.NORMAL)
                            }
                        )
                        OnboardingToggleRow(
                            icon = Icons.Filled.ViewAgenda,
                            label = strings.simpleLayoutLabel,
                            checked = simplified,
                            testTag = "onboarding_simple_layout",
                            onCheckedChange = { simplified = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        onComplete(
                            selectedLang,
                            selectedNeeds,
                            highContrast,
                            if (largeText) FontScaleOption.LARGE else FontScaleOption.NORMAL,
                            simplified
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 58.dp)
                        .testTag("onboarding_start_button"),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(
                        text = strings.startAppButton,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun OnboardingToggleRow(
    icon: ImageVector,
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
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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

private fun needIcon(need: AccessibilityNeed): ImageVector = when (need) {
    AccessibilityNeed.VISION -> Icons.Filled.Visibility
    AccessibilityNeed.HEARING -> Icons.Filled.Hearing
    AccessibilityNeed.MOTOR -> Icons.Filled.AccessibilityNew
    AccessibilityNeed.SPEECH -> Icons.Filled.RecordVoiceOver
    AccessibilityNeed.LEARNING -> Icons.Filled.School
}
