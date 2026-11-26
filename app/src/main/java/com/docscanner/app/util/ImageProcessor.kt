package com.docscanner.app.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import androidx.compose.ui.geometry.Size
import com.docscanner.app.model.CropCorners

object ImageProcessor {

    fun perspectiveCorrected(bitmap: Bitmap, corners: CropCorners, viewSize: Size): Bitmap? {
        val scaleX = bitmap.width.toFloat() / viewSize.width
        val scaleY = bitmap.height.toFloat() / viewSize.height

        val srcPoints = floatArrayOf(
            corners.topLeft.x * scaleX, corners.topLeft.y * scaleY,
            corners.topRight.x * scaleX, corners.topRight.y * scaleY,
            corners.bottomRight.x * scaleX, corners.bottomRight.y * scaleY,
            corners.bottomLeft.x * scaleX, corners.bottomLeft.y * scaleY
        )

        val width = maxOf(
            distance(srcPoints[0], srcPoints[1], srcPoints[2], srcPoints[3]),
            distance(srcPoints[6], srcPoints[7], srcPoints[4], srcPoints[5])
        ).toInt().coerceAtLeast(1)

        val height = maxOf(
            distance(srcPoints[0], srcPoints[1], srcPoints[6], srcPoints[7]),
            distance(srcPoints[2], srcPoints[3], srcPoints[4], srcPoints[5])
        ).toInt().coerceAtLeast(1)

        val dstPoints = floatArrayOf(
            0f, 0f,
            width.toFloat(), 0f,
            width.toFloat(), height.toFloat(),
            0f, height.toFloat()
        )

        val matrix = Matrix()
        if (!matrix.setPolyToPoly(srcPoints, 0, dstPoints, 0, 4)) return null

        return try {
            val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            canvas.drawBitmap(bitmap, matrix, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
            result
        } catch (e: Exception) {
            null
        }
    }

    fun rotated(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun autoEnhance(bitmap: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val contrast = 1.1f
        val translate = (-0.5f * contrast + 0.5f) * 255f
        val cm = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, translate,
            0f, contrast, 0f, 0f, translate,
            0f, 0f, contrast, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    fun detectDocumentCorners(bitmap: Bitmap, viewSize: Size, callback: (CropCorners?) -> Unit) {
        // Simple edge detection: return slightly inset corners as default
        // A production app would use ML Kit or OpenCV for real edge detection
        Thread {
            try {
                val insetX = viewSize.width * 0.1f
                val insetY = viewSize.height * 0.1f
                val corners = CropCorners(
                    topLeft = androidx.compose.ui.geometry.Offset(insetX, insetY),
                    topRight = androidx.compose.ui.geometry.Offset(viewSize.width - insetX, insetY),
                    bottomRight = androidx.compose.ui.geometry.Offset(viewSize.width - insetX, viewSize.height - insetY),
                    bottomLeft = androidx.compose.ui.geometry.Offset(insetX, viewSize.height - insetY)
                )
                callback(corners)
            } catch (e: Exception) {
                callback(null)
            }
        }.start()
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }
}
