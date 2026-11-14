package com.example.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.AppContainer
import com.example.core.hardware.DocumentReaderHelper
import com.example.data.ai.AiResult
import com.example.data.local.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SimplifyState {
    data object Idle : SimplifyState
    data object Loading : SimplifyState
    data class Success(val text: String, val isPlaceholder: Boolean) : SimplifyState
    data class Error(val message: String) : SimplifyState
}

class ReaderViewModel(private val container: AppContainer) : ViewModel() {

    val isSpeaking: StateFlow<Boolean> = container.ttsManager.isSpeaking
    val activeSentenceIndex: StateFlow<Int> = container.ttsManager.activeSentenceIndex

    private val _documentText = MutableStateFlow("")
    val documentText: StateFlow<String> = _documentText.asStateFlow()

    private val _sentences = MutableStateFlow<List<String>>(emptyList())
    val sentences: StateFlow<List<String>> = _sentences.asStateFlow()

    private val _fileName = MutableStateFlow<String?>(null)
    val fileName: StateFlow<String?> = _fileName.asStateFlow()

    private val _pdfUri = MutableStateFlow<Uri?>(null)
    private val _pdfPageCount = MutableStateFlow(0)
    val pdfPageCount: StateFlow<Int> = _pdfPageCount.asStateFlow()

    private val _currentPdfPage = MutableStateFlow(0)
    val currentPdfPage: StateFlow<Int> = _currentPdfPage.asStateFlow()

    private val _pdfPageBitmap = MutableStateFlow<Bitmap?>(null)
    val pdfPageBitmap: StateFlow<Bitmap?> = _pdfPageBitmap.asStateFlow()

    private val _simplifyState = MutableStateFlow<SimplifyState>(SimplifyState.Idle)
    val simplifyState: StateFlow<SimplifyState> = _simplifyState.asStateFlow()

    fun updateText(newText: String) {
        _documentText.value = newText
        _sentences.value = DocumentReaderHelper.splitIntoSentences(newText)
    }

    fun loadSampleText(language: AppLanguage) {
        val sample = when (language) {
            AppLanguage.ARABIC ->
                "مرحباً بك في القارئ الذكي من رفيق. يمكنك الاستماع إلى المستندات والكتب والنصوص الطويلة بصوت واضح وهادئ. كما يمكنك تعديل سرعة القراءة لتناسب راحتك، مع تظليل كل جملة أثناء نطقها."
            AppLanguage.FRENCH ->
                "Bienvenue dans le Lecteur Intelligent de Rafiq. Vous pouvez écouter vos documents et textes longs d'une voix claire et apaisante. Ajustez la vitesse de lecture selon votre confort tout en suivant chaque phrase surlignée."
            AppLanguage.ENGLISH ->
                "Welcome to Rafiq Smart Reader. You can listen to documents, articles, and long passages in a clear, calm voice. Adjust reading speed to match your comfort while following every highlighted sentence."
        }
        _fileName.value = null
        _pdfUri.value = null
        _pdfPageBitmap.value = null
        _pdfPageCount.value = 0
        _simplifyState.value = SimplifyState.Idle
        updateText(sample)
    }

    fun openDocument(context: Context, uri: Uri, language: AppLanguage) {
        viewModelScope.launch {
            val doc = DocumentReaderHelper.readDocumentMetadata(context, uri)
            _fileName.value = doc.fileName
            _simplifyState.value = SimplifyState.Idle
            if (doc.isPdf) {
                _pdfUri.value = uri
                _pdfPageCount.value = doc.pageCount
                _currentPdfPage.value = 0
                val bitmap = DocumentReaderHelper.renderPdfPage(context, uri, 0)
                _pdfPageBitmap.value = bitmap
                val infoMsg = when (language) {
                    AppLanguage.ARABIC -> "تم فتح ملف PDF (${doc.pageCount} صفحات). يمكنك تصفح الصفحات أو لصق نص لقراءته أو استخدام زر التبسيط."
                    AppLanguage.FRENCH -> "PDF ouvert (${doc.pageCount} pages). Parcourez les pages ou collez un extrait à lire à voix haute."
                    AppLanguage.ENGLISH -> "PDF opened (${doc.pageCount} pages). Browse pages visually or paste text below for highlighted read-aloud."
                }
                if (_documentText.value.isBlank()) {
                    updateText(infoMsg)
                }
            } else {
                _pdfUri.value = null
                _pdfPageCount.value = 0
                _pdfPageBitmap.value = null
                updateText(doc.textContent)
            }
        }
    }

    fun changePdfPage(context: Context, delta: Int) {
        val uri = _pdfUri.value ?: return
        val next = (_currentPdfPage.value + delta).coerceIn(0, (_pdfPageCount.value - 1).coerceAtLeast(0))
        if (next == _currentPdfPage.value) return
        _currentPdfPage.value = next
        viewModelScope.launch {
            _pdfPageBitmap.value = DocumentReaderHelper.renderPdfPage(context, uri, next)
        }
    }

    fun toggleReadAloud(language: AppLanguage, speed: Float) {
        if (isSpeaking.value) {
            container.ttsManager.stop()
        } else {
            val list = _sentences.value
            if (list.isNotEmpty()) {
                container.ttsManager.speakSentences(list, 0, language, speed)
            }
        }
    }

    fun speakFromSentence(index: Int, language: AppLanguage, speed: Float) {
        val list = _sentences.value
        if (index in list.indices) {
            container.ttsManager.speakSentences(list, index, language, speed)
        }
    }

    fun simplifyCurrentText(language: AppLanguage, speed: Float) {
        val text = _documentText.value.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            _simplifyState.value = SimplifyState.Loading
            when (val res = container.aiRepository.simplifyText(text, language)) {
                is AiResult.Success -> {
                    _simplifyState.value = SimplifyState.Success(res.data, res.isPlaceholder)
                    container.ttsManager.speak(res.data, language, speed)
                }
                is AiResult.Error -> {
                    _simplifyState.value = SimplifyState.Error(res.message)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        container.ttsManager.stop()
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ReaderViewModel(container) as T
    }
}
