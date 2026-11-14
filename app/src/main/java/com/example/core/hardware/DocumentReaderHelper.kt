package com.example.core.hardware

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

data class LoadedDocument(
    val fileName: String,
    val isPdf: Boolean,
    val pageCount: Int,
    val textContent: String
)

object DocumentReaderHelper {

    suspend fun decodeBitmapFromUri(context: Context, uri: Uri, maxDim: Int = 1280): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, boundsOpts)
                }
                var sampleSize = 1
                while (boundsOpts.outWidth / sampleSize > maxDim || boundsOpts.outHeight / sampleSize > maxDim) {
                    sampleSize *= 2
                }
                val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOpts)
                }
            } catch (_: Exception) {
                null
            }
        }

    suspend fun readDocumentMetadata(context: Context, uri: Uri): LoadedDocument =
        withContext(Dispatchers.IO) {
            val mime = context.contentResolver.getType(uri).orEmpty()
            val displayName = uri.lastPathSegment ?: "document"
            if (mime.contains("pdf") || displayName.endsWith(".pdf", ignoreCase = true)) {
                var pages = 0
                try {
                    context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                        PdfRenderer(pfd).use { renderer ->
                            pages = renderer.pageCount
                        }
                    }
                } catch (_: Exception) {
                }
                LoadedDocument(
                    fileName = displayName,
                    isPdf = true,
                    pageCount = pages,
                    textContent = ""
                )
            } else {
                val text = try {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        BufferedReader(InputStreamReader(input)).readText()
                    }.orEmpty()
                } catch (_: Exception) {
                    ""
                }
                LoadedDocument(
                    fileName = displayName,
                    isPdf = false,
                    pageCount = 1,
                    textContent = text
                )
            }
        }

    suspend fun renderPdfPage(context: Context, uri: Uri, pageIndex: Int): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    PdfRenderer(pfd).use { renderer ->
                        if (pageIndex < 0 || pageIndex >= renderer.pageCount) return@withContext null
                        renderer.openPage(pageIndex).use { page ->
                            val width = (page.width * 2).coerceAtMost(1600)
                            val height = (page.height * 2).coerceAtMost(2200)
                            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            bitmap.eraseColor(Color.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            bitmap
                        }
                    }
                }
            } catch (_: Exception) {
                null
            }
        }

    fun splitIntoSentences(text: String): List<String> {
        return text
            .split(Regex("(?<=[.!؟?\\n])\\s+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    fun triggerHapticPulse(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(180, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(180, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(180)
                }
            }
        } catch (_: Exception) {
        }
    }
}
