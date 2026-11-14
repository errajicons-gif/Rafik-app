package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.data.local.AppLanguage
import com.example.data.local.FontScaleOption
import com.example.data.local.ThemeMode

private val StandardLightColors = lightColorScheme(
    primary = TealPrimaryLight,
    onPrimary = TealOnPrimaryLight,
    primaryContainer = TealContainerLight,
    onPrimaryContainer = OnTealContainerLight,
    secondary = SlateSecondaryLight,
    onSecondary = SlateOnSecondaryLight,
    secondaryContainer = SlateContainerLight,
    onSecondaryContainer = OnSlateContainerLight,
    tertiary = AmberTertiaryLight,
    onTertiary = AmberOnTertiaryLight,
    tertiaryContainer = AmberContainerLight,
    onTertiaryContainer = OnAmberContainerLight,
    background = IvoryBackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight
)

private val StandardDarkColors = darkColorScheme(
    primary = TealPrimaryDark,
    onPrimary = TealOnPrimaryDark,
    primaryContainer = TealContainerDark,
    onPrimaryContainer = OnTealContainerDark,
    secondary = SlateSecondaryDark,
    onSecondary = SlateOnSecondaryDark,
    secondaryContainer = SlateContainerDark,
    onSecondaryContainer = OnSlateContainerDark,
    tertiary = AmberTertiaryDark,
    onTertiary = AmberOnTertiaryDark,
    tertiaryContainer = AmberContainerDark,
    onTertiaryContainer = OnAmberContainerDark,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)

private val HighContrastLightColors = lightColorScheme(
    primary = HcLightPrimary,
    onPrimary = HcLightOnPrimary,
    primaryContainer = HcLightSurfaceVariant,
    onPrimaryContainer = HcLightOnSurface,
    secondary = SlateSecondaryLight,
    onSecondary = HcLightOnPrimary,
    secondaryContainer = HcLightSurfaceVariant,
    onSecondaryContainer = HcLightOnSurface,
    tertiary = AmberTertiaryLight,
    onTertiary = HcLightOnPrimary,
    background = HcLightBackground,
    onBackground = HcLightOnSurface,
    surface = HcLightSurface,
    onSurface = HcLightOnSurface,
    surfaceVariant = HcLightSurfaceVariant,
    onSurfaceVariant = HcLightOnSurface,
    outline = HcLightOutline
)

private val HighContrastDarkColors = darkColorScheme(
    primary = HcDarkPrimary,
    onPrimary = HcDarkOnPrimary,
    primaryContainer = HcDarkSurfaceVariant,
    onPrimaryContainer = HcDarkOnSurface,
    secondary = SlateSecondaryDark,
    onSecondary = HcDarkOnPrimary,
    secondaryContainer = HcDarkSurfaceVariant,
    onSecondaryContainer = HcDarkOnSurface,
    tertiary = AmberTertiaryDark,
    onTertiary = HcDarkOnPrimary,
    background = HcDarkBackground,
    onBackground = HcDarkOnSurface,
    surface = HcDarkSurface,
    onSurface = HcDarkOnSurface,
    surfaceVariant = HcDarkSurfaceVariant,
    onSurfaceVariant = HcDarkOnSurface,
    outline = HcDarkOutline
)

private val RafiqShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun RafiqTheme(
    language: AppLanguage = AppLanguage.ARABIC,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    highContrast: Boolean = false,
    fontScaleOption: FontScaleOption = FontScaleOption.NORMAL,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        highContrast && isDark -> HighContrastDarkColors
        highContrast && !isDark -> HighContrastLightColors
        isDark -> StandardDarkColors
        else -> StandardLightColors
    }

    val layoutDirection = if (language == AppLanguage.ARABIC) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }

    val typography = buildRafiqTypography(
        isArabic = language == AppLanguage.ARABIC,
        scale = fontScaleOption.multiplier
    )

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = RafiqShapes,
            content = content
        )
    }
}
