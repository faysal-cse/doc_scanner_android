package com.docscanner.app.viewmodel

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.docscanner.app.model.CropCorners
import com.docscanner.app.model.ImageFilter
import com.docscanner.app.util.ImageProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CropViewModel(private val originalBitmap: Bitmap) : ViewModel() {

    private val _corners = MutableStateFlow(CropCorners.default(Size(300f, 400f)))
    val corners: StateFlow<CropCorners> = _corners.asStateFlow()

    private val _selectedFilter = MutableStateFlow(ImageFilter.ORIGINAL)
    val selectedFilter: StateFlow<ImageFilter> = _selectedFilter.asStateFlow()

    private val _displayBitmap = MutableStateFlow(originalBitmap)
    val displayBitmap: StateFlow<Bitmap> = _displayBitmap.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private var currentBitmap = originalBitmap
    private var viewSize = Size(300f, 400f)

    fun setViewSize(size: Size) {
        viewSize = size
        _corners.value = CropCorners.default(size)
        detectEdges()
    }

    fun updateCorners(corners: CropCorners) {
        _corners.value = corners
    }

    fun detectEdges() {
        ImageProcessor.detectDocumentCorners(currentBitmap, viewSize) { detected ->
            detected?.let { _corners.value = it }
        }
    }

    fun rotateLeft() {
        viewModelScope.launch(Dispatchers.Default) {
            currentBitmap = ImageProcessor.rotated(currentBitmap, -90f)
            _displayBitmap.value = applyCurrentFilter(currentBitmap)
            _corners.value = CropCorners.default(viewSize)
        }
    }

    fun rotateRight() {
        viewModelScope.launch(Dispatchers.Default) {
            currentBitmap = ImageProcessor.rotated(currentBitmap, 90f)
            _displayBitmap.value = applyCurrentFilter(currentBitmap)
            _corners.value = CropCorners.default(viewSize)
        }
    }

    fun applyFilter(filter: ImageFilter) {
        _selectedFilter.value = filter
        viewModelScope.launch(Dispatchers.Default) {
            _displayBitmap.value = applyCurrentFilter(currentBitmap)
        }
    }

    fun crop(): Bitmap {
        _isProcessing.value = true
        val corrected = ImageProcessor.perspectiveCorrected(currentBitmap, _corners.value, viewSize)
            ?: currentBitmap
        val filtered = _selectedFilter.value.apply(corrected)
        _isProcessing.value = false
        return filtered
    }

    private fun applyCurrentFilter(bitmap: Bitmap): Bitmap {
        return _selectedFilter.value.apply(bitmap)
    }
}
