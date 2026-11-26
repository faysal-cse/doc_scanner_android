package com.docscanner.app.util

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object OCRService {

    data class PageResult(val pageNumber: Int, val text: String)

    suspend fun recognizeText(images: List<Bitmap>, context: Context): List<PageResult> {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val results = mutableListOf<PageResult>()

        for ((index, bitmap) in images.withIndex()) {
            val text = recognizeSingle(recognizer, bitmap)
            if (text.isNotBlank()) {
                results.add(PageResult(pageNumber = index + 1, text = text))
            }
        }

        return results
    }

    private suspend fun recognizeSingle(
        recognizer: com.google.mlkit.vision.text.TextRecognizer,
        bitmap: Bitmap
    ): String = suspendCancellableCoroutine { cont ->
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                cont.resume(visionText.text)
            }
            .addOnFailureListener {
                cont.resume("")
            }
    }
}
