package com.docscanner.app.model

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

enum class ImageFilter(val displayName: String, val iconName: String) {
    ORIGINAL("Original", "image"),
    AUTO("Auto", "auto_fix_high"),
    ENHANCED("Enhanced", "tune"),
    BLACK_WHITE("B&W", "contrast"),
    GRAYSCALE("Grayscale", "filter_b_and_w"),
    VIVID("Vivid", "palette");

    fun apply(bitmap: Bitmap): Bitmap {
        if (this == ORIGINAL) return bitmap
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.colorFilter = ColorMatrixColorFilter(buildColorMatrix())
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    private fun buildColorMatrix(): ColorMatrix {
        return when (this) {
            ORIGINAL -> ColorMatrix()
            AUTO -> ColorMatrix().apply {
                val contrast = 1.1f
                val translate = (-0.5f * contrast + 0.5f) * 255f
                set(floatArrayOf(
                    contrast, 0f, 0f, 0f, translate,
                    0f, contrast, 0f, 0f, translate,
                    0f, 0f, contrast, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            ENHANCED -> ColorMatrix().apply {
                val contrast = 1.35f
                val translate = (-0.5f * contrast + 0.5f) * 255f + 12.75f
                val sat = ColorMatrix()
                sat.setSaturation(0.9f)
                set(floatArrayOf(
                    contrast, 0f, 0f, 0f, translate,
                    0f, contrast, 0f, 0f, translate,
                    0f, 0f, contrast, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                ))
                postConcat(sat)
            }
            BLACK_WHITE -> ColorMatrix().apply {
                setSaturation(0f)
                val contrast = 1.5f
                val translate = (-0.5f * contrast + 0.5f) * 255f + 10f
                val cm = ColorMatrix(floatArrayOf(
                    contrast, 0f, 0f, 0f, translate,
                    0f, contrast, 0f, 0f, translate,
                    0f, 0f, contrast, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                ))
                postConcat(cm)
            }
            GRAYSCALE -> ColorMatrix().apply {
                setSaturation(0f)
            }
            VIVID -> ColorMatrix().apply {
                setSaturation(1.6f)
                val contrast = 1.15f
                val translate = (-0.5f * contrast + 0.5f) * 255f
                val cm = ColorMatrix(floatArrayOf(
                    contrast, 0f, 0f, 0f, translate,
                    0f, contrast, 0f, 0f, translate,
                    0f, 0f, contrast, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                ))
                postConcat(cm)
            }
        }
    }
}
