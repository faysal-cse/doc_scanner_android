package com.docscanner.app.model

import android.graphics.Bitmap
import java.util.UUID

data class ScanPage(
    val id: String = UUID.randomUUID().toString(),
    val originalBitmap: Bitmap,
    var processedBitmap: Bitmap? = null
) {
    val displayBitmap: Bitmap get() = processedBitmap ?: originalBitmap
}
