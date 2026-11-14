package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.AppContainer
import com.example.data.local.FontScaleOption
import com.example.data.local.ModuleId
import com.example.ui.screens.CommunicationScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LearningScreen
import com.example.ui.screens.ListenerScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VisionScreen
import com.example.ui.screens.VoiceScreen
import com.example.ui.theme.RafiqTheme
import com.example.ui.viewmodel.CommunicationViewModel
import com.example.ui.viewmodel.LearningViewModel
import com.example.ui.viewmodel.ListenerViewModel
import com.example.ui.viewmodel.RafiqMainViewModel
import com.example.ui.viewmodel.ReaderViewModel
import com.example.ui.viewmodel.VisionViewModel
import com.example.ui.viewmodel.VoiceViewModel

object RafiqRoutes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val VISION = "vision"
    const val VOICE = "voice"
    const val COMMUNICATION = "communication"
    const val LISTENER = "listener"
    const val READER = "reader"
    const val LEARNING = "learning"
    const val SETTINGS = "settings"

    fun forModule(moduleId: ModuleId): String = when (moduleId) {
        ModuleId.VISION -> VISION
        ModuleId.VOICE -> VOICE
        ModuleId.COMMUNICATION -> COMMUNICATION
        ModuleId.LISTENER -> LISTENER
        ModuleId.READER -> READER
        ModuleId.LEARNING -> LEARNING
    }
}

@Composable
fun RafiqAppRoot(container: AppContainer) {
    val mainViewModel: RafiqMainViewModel = viewModel(factory = RafiqMainViewModel.Factory(container))
    val preferences by mainViewModel.preferences.collectAsStateWithLifecycle()
    val isOnline by mainViewModel.isOnline.collectAsStateWithLifecycle()
    val isSpeaking by mainViewModel.isSpeaking.collectAsStateWithLifecycle()

    // Live preview overrides during Onboarding before saving
    var previewHighContrast by remember(preferences.highContrast) { mutableStateOf(preferences.highContrast) }
    var previewFontScale by remember(preferences.fontScale) { mutableStateOf(preferences.fontScale) }

    val navController = rememberNavController()

    LaunchedEffect(preferences.onboardingCompleted) {
        val currentRoute = navController.currentDestination?.route
        if (!preferences.onboardingCompleted && currentRoute != RafiqRoutes.ONBOARDING) {
            navController.navigate(RafiqRoutes.ONBOARDING) {
                popUpTo(0) { inclusive = true }
            }
        } else if (preferences.onboardingCompleted && currentRoute == RafiqRoutes.ONBOARDING) {
            navController.navigate(RafiqRoutes.HOME) {
                popUpTo(RafiqRoutes.ONBOARDING) { inclusive = true }
            }
        }
    }

    RafiqTheme(
        language = preferences.language,
        themeMode = preferences.themeMode,
        highContrast = if (preferences.onboardingCompleted) preferences.highContrast else previewHighContrast,
        fontScaleOption = if (preferences.onboardingCompleted) preferences.fontScale else previewFontScale
    ) {
        RafiqNavHost(
            navController = navController,
            container = container,
            mainViewModel = mainViewModel,
            isOnline = isOnline,
            isSpeaking = isSpeaking,
            onPreviewContrast = { previewHighContrast = it },
            onPreviewFontScale = { previewFontScale = it }
        )
    }
}

@Composable
private fun RafiqNavHost(
    navController: NavHostController,
    container: AppContainer,
    mainViewModel: RafiqMainViewModel,
    isOnline: Boolean,
    isSpeaking: Boolean,
    onPreviewContrast: (Boolean) -> Unit,
    onPreviewFontScale: (FontScaleOption) -> Unit
) {
    val preferences by mainViewModel.preferences.collectAsStateWithLifecycle()
    val startDestination = if (preferences.onboardingCompleted) RafiqRoutes.HOME else RafiqRoutes.ONBOARDING

    val navigateToModule: (ModuleId) -> Unit = { moduleId ->
        mainViewModel.stopSpeaking()
        navController.navigate(RafiqRoutes.forModule(moduleId)) {
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(RafiqRoutes.ONBOARDING) {
            OnboardingScreen(
                initialLanguage = preferences.language,
                onLanguagePreviewChange = { mainViewModel.setLanguage(it) },
                onContrastPreviewChange = onPreviewContrast,
                onFontScalePreviewChange = onPreviewFontScale,
                onSpeakWelcome = { mainViewModel.speak(it) },
                onComplete = { lang, needs, hc, fontScale, simple ->
                    mainViewModel.completeOnboarding(lang, needs, hc, fontScale, simple)
                    navController.navigate(RafiqRoutes.HOME) {
                        popUpTo(RafiqRoutes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(RafiqRoutes.HOME) {
            HomeScreen(
                preferences = preferences,
                isOnline = isOnline,
                isSpeaking = isSpeaking,
                speechManager = container.speechRecognizerManager,
                voiceNavEvents = mainViewModel.voiceNavEvents,
                onSpeakText = { mainViewModel.speak(it) },
                onStopSpeaking = { mainViewModel.stopSpeaking() },
                onVoiceCommandRecognized = { mainViewModel.handleVoiceNavigationCommand(it) },
                onOpenModule = navigateToModule,
                onOpenSettings = {
                    mainViewModel.stopSpeaking()
                    navController.navigate(RafiqRoutes.SETTINGS) { launchSingleTop = true }
                }
            )
        }

        composable(RafiqRoutes.VISION) {
            val vm: VisionViewModel = viewModel(factory = VisionViewModel.Factory(container))
            VisionScreen(
                preferences = preferences,
                isOnline = isOnline,
                isSpeaking = isSpeaking,
                viewModel = vm,
                onSpeakText = { mainViewModel.speak(it) },
                onStopSpeaking = { mainViewModel.stopSpeaking() },
                onBack = {
                    mainViewModel.stopSpeaking()
                    navController.popBackStack()
                }
            )
        }

        composable(RafiqRoutes.VOICE) {
            val vm: VoiceViewModel = viewModel(factory = VoiceViewModel.Factory(container))
            VoiceScreen(
                preferences = preferences,
                isOnline = isOnline,
                isSpeaking = isSpeaking,
                speechManager = container.speechRecognizerManager,
                viewModel = vm,
                onSpeakText = { mainViewModel.speak(it) },
                onStopSpeaking = { mainViewModel.stopSpeaking() },
                onNavigateToModule = navigateToModule,
                onBack = {
                    mainViewModel.stopSpeaking()
                    navController.popBackStack()
                }
            )
        }

        composable(RafiqRoutes.COMMUNICATION) {
            val vm: CommunicationViewModel = viewModel(factory = CommunicationViewModel.Factory(container))
            CommunicationScreen(
                preferences = preferences,
                isOnline = isOnline,
                isSpeaking = isSpeaking,
                viewModel = vm,
                onStopSpeaking = { mainViewModel.stopSpeaking() },
                onBack = {
                    mainViewModel.stopSpeaking()
                    navController.popBackStack()
                }
            )
        }

        composable(RafiqRoutes.LISTENER) {
            val vm: ListenerViewModel = viewModel(factory = ListenerViewModel.Factory())
            ListenerScreen(
                preferences = preferences,
                isOnline = isOnline,
                isSpeaking = isSpeaking,
                speechManager = container.speechRecognizerManager,
                viewModel = vm,
                onStopSpeaking = { mainViewModel.stopSpeaking() },
                onBack = {
                    mainViewModel.stopSpeaking()
                    navController.popBackStack()
                }
            )
        }

        composable(RafiqRoutes.READER) {
            val vm: ReaderViewModel = viewModel(factory = ReaderViewModel.Factory(container))
            ReaderScreen(
                preferences = preferences,
                isOnline = isOnline,
                mainViewModel = mainViewModel,
                viewModel = vm,
                onBack = {
                    mainViewModel.stopSpeaking()
                    navController.popBackStack()
                }
            )
        }

        composable(RafiqRoutes.LEARNING) {
            val vm: LearningViewModel = viewModel(factory = LearningViewModel.Factory(container))
            LearningScreen(
                preferences = preferences,
                isOnline = isOnline,
                isSpeaking = isSpeaking,
                viewModel = vm,
                onStopSpeaking = { mainViewModel.stopSpeaking() },
                onBack = {
                    mainViewModel.stopSpeaking()
                    navController.popBackStack()
                }
            )
        }

        composable(RafiqRoutes.SETTINGS) {
            SettingsScreen(
                preferences = preferences,
                isOnline = isOnline,
                isSpeaking = isSpeaking,
                mainViewModel = mainViewModel,
                onBack = {
                    mainViewModel.stopSpeaking()
                    navController.popBackStack()
                }
            )
        }
    }
}
