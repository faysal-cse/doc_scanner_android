package com.docscanner.app.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.docscanner.app.model.ImageFilter
import com.docscanner.app.model.ScanPage
import com.docscanner.app.util.ImageProcessor
import com.docscanner.app.util.PDFExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanSessionViewModel : ViewModel() {

    private val _pages = MutableStateFlow<List<ScanPage>>(emptyList())
    val pages: StateFlow<List<ScanPage>> = _pages.asStateFlow()

    fun addPages(images: List<Bitmap>) {
        val startIndex = _pages.value.size
        val newPages = images.map { ScanPage(originalBitmap = it) }
        _pages.value = _pages.value + newPages
        autoEnhanceNewPages(startIndex)
    }

    private fun autoEnhanceNewPages(startIndex: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            val current = _pages.value.toMutableList()
            for (i in startIndex until current.size) {
                val enhanced = ImageProcessor.autoEnhance(current[i].originalBitmap)
                current[i] = current[i].copy(processedBitmap = enhanced)
            }
            _pages.value = current.toList()
        }
    }

    fun updatePage(index: Int, image: Bitmap) {
        val current = _pages.value.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(processedBitmap = image)
            _pages.value = current
        }
    }

    fun applyFilter(index: Int, filter: ImageFilter) {
        viewModelScope.launch(Dispatchers.Default) {
            val current = _pages.value.toMutableList()
            if (index in current.indices) {
                val source = current[index].originalBitmap
                val filtered = filter.apply(source)
                current[index] = current[index].copy(processedBitmap = filtered)
                _pages.value = current
            }
        }
    }

    fun deletePage(index: Int): ScanPage? {
        val current = _pages.value.toMutableList()
        if (index in current.indices) {
            val removed = current.removeAt(index)
            _pages.value = current
            return removed
        }
        return null
    }

    fun restorePage(page: ScanPage, index: Int) {
        val current = _pages.value.toMutableList()
        val insertAt = index.coerceIn(0, current.size)
        current.add(insertAt, page)
        _pages.value = current
    }

    fun movePage(from: Int, to: Int) {
        val current = _pages.value.toMutableList()
        if (from in current.indices && to in current.indices) {
            val item = current.removeAt(from)
            current.add(to, item)
            _pages.value = current
        }
    }

    fun exportPDF(context: Context): File? {
        val images = _pages.value.map { it.displayBitmap }
        if (images.isEmpty()) return null
        val filename = "DocScan_${formattedDate()}"
        return PDFExporter.exportToFile(images, filename, context)
    }

    fun getDisplayBitmaps(): List<Bitmap> = _pages.value.map { it.displayBitmap }

    private fun formattedDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}
