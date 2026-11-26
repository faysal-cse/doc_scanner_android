package com.docscanner.app.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

data class CropCorners(
    var topLeft: Offset,
    var topRight: Offset,
    var bottomRight: Offset,
    var bottomLeft: Offset
) {
    companion object {
        fun default(size: Size): CropCorners {
            val inset = 40f
            return CropCorners(
                topLeft = Offset(inset, inset),
                topRight = Offset(size.width - inset, inset),
                bottomRight = Offset(size.width - inset, size.height - inset),
                bottomLeft = Offset(inset, size.height - inset)
            )
        }
    }
}
