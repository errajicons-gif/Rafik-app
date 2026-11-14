package com.example.core.hardware

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.data.local.AppLanguage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class SpeechRecognizerManager(private val context: Context) {

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _rmsDb = MutableStateFlow(0f)
    val rmsDb: StateFlow<Float> = _rmsDb.asStateFlow()

    private val _loudSoundAlert = MutableSharedFlow<Float>(extraBufferCapacity = 4)
    val loudSoundAlert: SharedFlow<Float> = _loudSoundAlert.asSharedFlow()

    private val _finalResults = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val finalResults: SharedFlow<String> = _finalResults.asSharedFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var continuousMode = false
    private var activeLanguage: AppLanguage = AppLanguage.ARABIC

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(appContext)

    fun startListening(language: AppLanguage, continuous: Boolean = false) {
        continuousMode = continuous
        activeLanguage = language
        _errorMessage.value = null
        _partialText.value = ""

        mainHandler.post {
            if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
                _isListening.value = false
                _errorMessage.value = when (language) {
                    AppLanguage.ARABIC -> "خدمة التعرف الصوتي غير متوفرة على هذا الجهاز."
                    AppLanguage.FRENCH -> "La reconnaissance vocale n'est pas disponible sur cet appareil."
                    AppLanguage.ENGLISH -> "Speech recognition service is not available on this device."
                }
                return@post
            }

            try {
                recognizer?.destroy()
                recognizer = SpeechRecognizer.createSpeechRecognizer(appContext).apply {
                    setRecognitionListener(createListener())
                }
                val intent = buildRecognizerIntent(language)
                _isListening.value = true
                recognizer?.startListening(intent)
            } catch (e: Exception) {
                _isListening.value = false
                _errorMessage.value = e.localizedMessage ?: "Speech recognition error"
            }
        }
    }

    fun stopListening() {
        continuousMode = false
        mainHandler.post {
            _isListening.value = false
            _rmsDb.value = 0f
            try {
                recognizer?.stopListening()
            } catch (_: Exception) {
            }
        }
    }

    private fun buildRecognizerIntent(language: AppLanguage): Intent {
        val localeTag = when (language) {
            AppLanguage.ARABIC -> "ar-SA"
            AppLanguage.FRENCH -> "fr-FR"
            AppLanguage.ENGLISH -> "en-US"
        }
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
    }

    private fun createListener(): RecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _isListening.value = true
            _errorMessage.value = null
        }

        override fun onBeginningOfSpeech() {}

        override fun onRmsChanged(rmsdB: Float) {
            _rmsDb.value = rmsdB
            if (rmsdB >= 8.5f) {
                _loudSoundAlert.tryEmit(rmsdB)
            }
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            if (!continuousMode) {
                _isListening.value = false
            }
        }

        override fun onError(error: Int) {
            _rmsDb.value = 0f
            if (continuousMode && (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                mainHandler.postDelayed({
                    if (continuousMode) {
                        try {
                            recognizer?.startListening(buildRecognizerIntent(activeLanguage))
                        } catch (_: Exception) {
                            _isListening.value = false
                        }
                    }
                }, 250)
                return
            }

            _isListening.value = false
            if (error != SpeechRecognizer.ERROR_CLIENT) {
                _errorMessage.value = mapErrorMessage(error, activeLanguage)
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.firstOrNull().orEmpty().trim()
            if (text.isNotEmpty()) {
                _finalResults.tryEmit(text)
            }
            _partialText.value = ""
            if (continuousMode) {
                mainHandler.postDelayed({
                    if (continuousMode) {
                        try {
                            recognizer?.startListening(buildRecognizerIntent(activeLanguage))
                        } catch (_: Exception) {
                            _isListening.value = false
                        }
                    }
                }, 180)
            } else {
                _isListening.value = false
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val partial = matches?.firstOrNull().orEmpty()
            if (partial.isNotBlank()) {
                _partialText.value = partial
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun mapErrorMessage(code: Int, language: AppLanguage): String = when (language) {
        AppLanguage.ARABIC -> when (code) {
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "يرجى منح إذن الميكروفون."
            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "يلزم اتصال بالإنترنت للتعرف الصوتي."
            SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "لم يُسمع صوت واضح، حاول التحدث مجدداً."
            else -> "تعذر بدء الاستماع ($code)."
        }
        AppLanguage.FRENCH -> when (code) {
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission microphone requise."
            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Connexion réseau requise pour la reconnaissance vocale."
            SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Aucune parole détectée, réessayez."
            else -> "Erreur de reconnaissance vocale ($code)."
        }
        AppLanguage.ENGLISH -> when (code) {
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network connection required for speech recognition."
            SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No clear speech detected. Please try again."
            else -> "Speech recognition error ($code)."
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun destroy() {
        continuousMode = false
        mainHandler.post {
            recognizer?.destroy()
            recognizer = null
        }
    }
}
