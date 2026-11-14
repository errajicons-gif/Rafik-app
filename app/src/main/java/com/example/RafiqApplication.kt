package com.example

import android.app.Application
import com.example.core.hardware.NetworkMonitor
import com.example.core.hardware.SpeechRecognizerManager
import com.example.core.hardware.TtsManager
import com.example.data.ai.AiRepository
import com.example.data.ai.PlaceholderAiRepository
import com.example.data.local.LocalDataRepository
import com.example.data.local.PreferencesRepository
import com.example.data.local.RafiqDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(application: Application) {
    private val database = RafiqDatabase.getInstance(application)
    val preferencesRepository = PreferencesRepository(application)
    val localDataRepository = LocalDataRepository(database.rafiqDao())
    val ttsManager = TtsManager(application)
    val speechRecognizerManager = SpeechRecognizerManager(application)
    val networkMonitor = NetworkMonitor(application)
    val aiRepository: AiRepository = PlaceholderAiRepository()
}

class RafiqApplication : Application() {
    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        appScope.launch {
            container.localDataRepository.ensureSeeded()
        }
    }
}
