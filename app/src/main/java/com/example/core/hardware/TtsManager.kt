package com.example.core.hardware

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.local.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

class TtsManager(context: Context) : TextToSpeech.OnInitListener {

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var isReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _activeSentenceIndex = MutableStateFlow(-1)
    val activeSentenceIndex: StateFlow<Int> = _activeSentenceIndex.asStateFlow()

    private var queuedSentences: List<String> = emptyList()
    private var queuedLanguage: AppLanguage = AppLanguage.ARABIC
    private var queuedSpeed: Float = 1.0f

    init {
        tts = TextToSpeech(appContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isReady = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    if (utteranceId?.startsWith("sent_") == true) {
                        val idx = utteranceId.removePrefix("sent_").toIntOrNull() ?: -1
                        _activeSentenceIndex.value = idx
                    }
                }

                override fun onDone(utteranceId: String?) {
                    if (utteranceId?.startsWith("sent_") == true) {
                        val idx = utteranceId.removePrefix("sent_").toIntOrNull() ?: -1
                        val next = idx + 1
                        if (next in queuedSentences.indices) {
                            speakSentenceAt(next)
                        } else {
                            _isSpeaking.value = false
                            _activeSentenceIndex.value = -1
                        }
                    } else {
                        _isSpeaking.value = false
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _activeSentenceIndex.value = -1
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    _activeSentenceIndex.value = -1
                }
            })
        }
    }

    fun speak(text: String, language: AppLanguage, speed: Float = 1.0f) {
        if (!isReady || text.isBlank()) return
        queuedSentences = emptyList()
        _activeSentenceIndex.value = -1
        applyLocaleAndSpeed(language, speed)
        val params = Bundle()
        val id = "utt_${UUID.randomUUID()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, id)
    }

    fun speakSentences(
        sentences: List<String>,
        startIndex: Int = 0,
        language: AppLanguage,
        speed: Float = 1.0f
    ) {
        if (!isReady || sentences.isEmpty()) return
        queuedSentences = sentences
        queuedLanguage = language
        queuedSpeed = speed
        applyLocaleAndSpeed(language, speed)
        val safeStart = startIndex.coerceIn(0, sentences.lastIndex)
        speakSentenceAt(safeStart)
    }

    private fun speakSentenceAt(index: Int) {
        val sentence = queuedSentences.getOrNull(index) ?: return
        applyLocaleAndSpeed(queuedLanguage, queuedSpeed)
        val params = Bundle()
        tts?.speak(sentence, TextToSpeech.QUEUE_FLUSH, params, "sent_$index")
    }

    fun stop() {
        queuedSentences = emptyList()
        _isSpeaking.value = false
        _activeSentenceIndex.value = -1
        tts?.stop()
    }

    private fun applyLocaleAndSpeed(language: AppLanguage, speed: Float) {
        val locale = when (language) {
            AppLanguage.ARABIC -> Locale("ar")
            AppLanguage.FRENCH -> Locale.FRENCH
            AppLanguage.ENGLISH -> Locale.ENGLISH
        }
        val res = tts?.setLanguage(locale)
        if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts?.language = Locale.getDefault()
        }
        tts?.setSpeechRate(speed.coerceIn(0.6f, 1.6f))
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
