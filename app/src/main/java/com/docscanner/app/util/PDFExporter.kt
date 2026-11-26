package com.docscanner.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

object PDFExporter {

    private const val A4_WIDTH = 595
    private const val A4_HEIGHT = 842

    fun createPDF(images: List<Bitmap>): ByteArray {
        val document = PdfDocument()

        for ((index, bitmap) in images.withIndex()) {
            val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, index + 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val rect = aspectFitRect(
                bitmap.width.toFloat(),
                bitmap.height.toFloat(),
                A4_WIDTH.toFloat(),
                A4_HEIGHT.toFloat()
            )
            canvas.drawBitmap(bitmap, null, rect, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
            document.finishPage(page)
        }

        val output = java.io.ByteArrayOutputStream()
        document.writeTo(output)
        document.close()
        return output.toByteArray()
    }

    fun exportToFile(images: List<Bitmap>, filename: String, context: Context): File? {
        return try {
            val pdfData = createPDF(images)
            val file = File(context.cacheDir, "$filename.pdf")
            FileOutputStream(file).use { it.write(pdfData) }
            file
        } catch (e: Exception) {
            null
        }
    }

    private fun aspectFitRect(
        imageWidth: Float,
        imageHeight: Float,
        boundsWidth: Float,
        boundsHeight: Float
    ): RectF {
        val scale = minOf(boundsWidth / imageWidth, boundsHeight / imageHeight)
        val width = imageWidth * scale
        val height = imageHeight * scale
        val x = (boundsWidth - width) / 2f
        val y = (boundsHeight - height) / 2f
        return RectF(x, y, x + width, y + height)
    }
}
