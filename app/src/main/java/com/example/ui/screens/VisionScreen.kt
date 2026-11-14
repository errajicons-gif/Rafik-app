package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.hardware.DocumentReaderHelper
import com.example.core.i18n.stringsFor
import com.example.data.ai.VisionAnalysisMode
import com.example.data.local.UserPreferences
import com.example.ui.components.AdaptiveCenteredBox
import com.example.ui.components.AiAccuracyDisclaimer
import com.example.ui.components.FirebaseNoticeBanner
import com.example.ui.components.PermissionGateCard
import com.example.ui.components.RafiqTopBar
import com.example.ui.viewmodel.VisionUiState
import com.example.ui.viewmodel.VisionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VisionScreen(
    preferences: UserPreferences,
    isOnline: Boolean,
    isSpeaking: Boolean,
    viewModel: VisionViewModel,
    onSpeakText: (String) -> Unit,
    onStopSpeaking: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val strings = stringsFor(preferences.language)

    val selectedMode by viewModel.selectedMode.collectAsStateWithLifecycle()
    val capturedBitmap by viewModel.capturedBitmap.collectAsStateWithLifecycle()
    val autoSpeak by viewModel.autoSpeak.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val bmp = DocumentReaderHelper.decodeBitmapFromUri(context, uri)
                if (bmp != null) {
                    viewModel.onImageSelected(bmp, preferences.language, preferences.voiceSpeed)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            RafiqTopBar(
                title = strings.visionTitle,
                strings = strings,
                isOnline = isOnline,
                isSpeaking = isSpeaking,
                onBack = onBack,
                onSpeakScreen = {
                    val textToRead = (uiState as? VisionUiState.Success)?.analysis?.summary
                        ?: strings.visionEmptyHint
                    onSpeakText(textToRead)
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
                // Mode Selection Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VisionAnalysisMode.entries.forEach { mode ->
                        val label = when (mode) {
                            VisionAnalysisMode.OBJECTS -> strings.visionModeObjects
                            VisionAnalysisMode.SCENE -> strings.visionModeScene
                            VisionAnalysisMode.OCR_TEXT -> strings.visionModeOcr
                            VisionAnalysisMode.COLORS -> strings.visionModeColors
                        }
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = {
                                viewModel.setMode(mode)
                                if (capturedBitmap != null) {
                                    viewModel.analyzeCurrentImage(preferences.language, preferences.voiceSpeed)
                                }
                            },
                            label = { Text(label) },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("vision_mode_${mode.name.lowercase()}")
                        )
                    }
                }

                // Auto-speak toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = strings.autoSpeakResult,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = autoSpeak,
                        onCheckedChange = { viewModel.setAutoSpeak(it) },
                        modifier = Modifier.testTag("vision_auto_speak_switch")
                    )
                }

                // Camera Preview or Captured Image
                if (capturedBitmap != null) {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Image(
                                bitmap = capturedBitmap!!.asImageBitmap(),
                                contentDescription = strings.visionTitle,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.analyzeCurrentImage(preferences.language, preferences.voiceSpeed)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 50.dp)
                                        .testTag("vision_reanalyze_button")
                                ) {
                                    Icon(Icons.Filled.Refresh, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text(strings.analyzeImage)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.clearImage() },
                                    modifier = Modifier
                                        .heightIn(min = 50.dp)
                                        .testTag("vision_clear_button")
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = strings.clearAll)
                                    Spacer(Modifier.width(4.dp))
                                    Text(strings.clearAll)
                                }
                            }
                        }
                    }
                } else if (hasCameraPermission) {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                        ) {
                            val previewView = remember { PreviewView(context) }

                            DisposableEffect(lifecycleOwner) {
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                                val executor = ContextCompat.getMainExecutor(context)
                                cameraProviderFuture.addListener({
                                    try {
                                        val cameraProvider = cameraProviderFuture.get()
                                        val preview = Preview.Builder().build().also {
                                            it.surfaceProvider = previewView.surfaceProvider
                                        }
                                        val capture = ImageCapture.Builder()
                                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                            .build()
                                        imageCapture = capture
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview,
                                            capture
                                        )
                                        cameraError = null
                                    } catch (e: Exception) {
                                        cameraError = e.localizedMessage
                                    }
                                }, executor)

                                onDispose {
                                    runCatching { cameraProviderFuture.get().unbindAll() }
                                }
                            }

                            AndroidView(
                                factory = { previewView },
                                modifier = Modifier.fillMaxSize()
                            )

                            if (cameraError != null) {
                                Text(
                                    text = cameraError.orEmpty(),
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp)
                                )
                            }
                        }
                    }
                } else {
                    PermissionGateCard(
                        icon = Icons.Filled.CameraAlt,
                        title = strings.cameraPermissionTitle,
                        rationale = strings.cameraPermissionRationale,
                        grantLabel = strings.grantPermission,
                        deniedHint = strings.permissionDeniedHint,
                        onGrantClick = {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    )
                }

                // Action Buttons: Capture + Pick from Gallery
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (hasCameraPermission && capturedBitmap == null) {
                        Button(
                            onClick = {
                                val capture = imageCapture ?: return@Button
                                capture.takePicture(
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageCapturedCallback() {
                                        override fun onCaptureSuccess(image: ImageProxy) {
                                            val bmp = imageProxyToBitmap(image)
                                            image.close()
                                            if (bmp != null) {
                                                viewModel.onImageSelected(
                                                    bmp,
                                                    preferences.language,
                                                    preferences.voiceSpeed
                                                )
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            cameraError = exception.localizedMessage
                                        }
                                    }
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 56.dp)
                                .testTag("vision_capture_button")
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(strings.capturePhoto, style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 56.dp)
                            .testTag("vision_pick_image_button")
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(strings.pickFromGallery, style = MaterialTheme.typography.labelLarge)
                    }
                }

                // Analysis Result Card
                when (val state = uiState) {
                    VisionUiState.Idle -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = strings.visionEmptyHint,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(18.dp)
                            )
                        }
                    }

                    VisionUiState.Analyzing -> {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(strings.analyzingImage, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }

                    is VisionUiState.Success -> {
                        if (state.isPlaceholder) {
                            FirebaseNoticeBanner(strings = strings)
                        }
                        Card(
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("vision_result_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = state.analysis.summary,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                state.analysis.details.forEach { detail ->
                                    Text(
                                        text = "• $detail",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Button(
                                    onClick = { onSpeakText(state.analysis.summary) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 50.dp)
                                        .testTag("vision_speak_result_button")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(strings.readAloud)
                                }
                            }
                        }
                        AiAccuracyDisclaimer(strings = strings)
                    }

                    is VisionUiState.Error -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
    val bitmap = runCatching { image.toBitmap() }.getOrNull() ?: return null
    val rotation = image.imageInfo.rotationDegrees
    if (rotation == 0) return bitmap
    val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
