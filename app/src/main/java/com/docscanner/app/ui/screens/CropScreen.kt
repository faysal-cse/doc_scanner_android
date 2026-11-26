package com.docscanner.app.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.RotateLeft
import androidx.compose.material.icons.automirrored.outlined.RotateRight
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.docscanner.app.model.CropCorners
import com.docscanner.app.viewmodel.CropViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropScreen(
    bitmap: Bitmap,
    onFinish: (Bitmap) -> Unit,
    onBack: () -> Unit
) {
    val viewModel = remember { CropViewModel(bitmap) }
    val corners by viewModel.corners.collectAsState()
    val displayBitmap by viewModel.displayBitmap.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    var imageViewSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top bar
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.Close, "Back", tint = Color.White)
                }
            },
            actions = {
                IconButton(onClick = { viewModel.rotateLeft() }) {
                    Icon(Icons.AutoMirrored.Outlined.RotateLeft, "Rotate Left", tint = Color.White)
                }
                IconButton(onClick = { viewModel.rotateRight() }) {
                    Icon(Icons.AutoMirrored.Outlined.RotateRight, "Rotate Right", tint = Color.White)
                }
                IconButton(onClick = {
                    viewModel.setViewSize(Size(imageViewSize.width.toFloat(), imageViewSize.height.toFloat()))
                }) {
                    Icon(Icons.Outlined.Refresh, "Reset", tint = Color.White)
                }
                IconButton(onClick = {
                    val cropped = viewModel.crop()
                    onFinish(cropped)
                }) {
                    Icon(Icons.Outlined.Check, "Done", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        // Image + crop overlay
        CropCanvas(
            displayBitmap = displayBitmap,
            corners = corners,
            onViewSizeChanged = { size ->
                imageViewSize = size
                viewModel.setViewSize(Size(size.width.toFloat(), size.height.toFloat()))
            },
            onCornersChanged = { viewModel.updateCorners(it) },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(displayBitmap.width.toFloat() / displayBitmap.height.toFloat())
                .align(Alignment.Center)
        )

        if (isProcessing) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun CropCanvas(
    displayBitmap: Bitmap,
    corners: CropCorners,
    onViewSizeChanged: (IntSize) -> Unit,
    onCornersChanged: (CropCorners) -> Unit,
    modifier: Modifier = Modifier
) {
    val handleRadius = 12.dp
    val handleRadiusPx = with(LocalDensity.current) { handleRadius.toPx() }
    val hitRadius = handleRadiusPx * 3f

    var viewSize by remember { mutableStateOf(IntSize.Zero) }

    // Use rememberUpdatedState so the pointerInput coroutine always reads latest values
    val cornersState = rememberUpdatedState(corners)
    val onCornersChangedState = rememberUpdatedState(onCornersChanged)

    Box(
        modifier = modifier
            .onSizeChanged { size ->
                viewSize = size
                onViewSizeChanged(size)
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        // Wait for first touch down
                        val down = awaitPointerEvent()
                        val downPos = down.changes.firstOrNull()?.position ?: continue
                        val downChange = down.changes.first()

                        // Find nearest handle
                        val c = cornersState.value
                        val handles = listOf(c.topLeft, c.topRight, c.bottomRight, c.bottomLeft)
                        val activeIdx = handles.indexOfFirst { handle ->
                            (downPos - handle).getDistance() < hitRadius
                        }

                        if (activeIdx < 0) continue
                        downChange.consume()

                        // Track drag
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            if (!change.pressed) break

                            val delta = change.positionChange()
                            change.consume()

                            val latest = cornersState.value
                            val cur = when (activeIdx) {
                                0 -> latest.topLeft
                                1 -> latest.topRight
                                2 -> latest.bottomRight
                                3 -> latest.bottomLeft
                                else -> break
                            }
                            val newPos = Offset(
                                (cur.x + delta.x).coerceIn(0f, viewSize.width.toFloat()),
                                (cur.y + delta.y).coerceIn(0f, viewSize.height.toFloat())
                            )
                            val newCorners = when (activeIdx) {
                                0 -> latest.copy(topLeft = newPos)
                                1 -> latest.copy(topRight = newPos)
                                2 -> latest.copy(bottomRight = newPos)
                                3 -> latest.copy(bottomLeft = newPos)
                                else -> latest
                            }
                            onCornersChangedState.value(newCorners)
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw image scaled to canvas
            val imageBitmap = displayBitmap.asImageBitmap()
            drawImage(
                image = imageBitmap,
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(imageBitmap.width, imageBitmap.height),
                dstOffset = IntOffset.Zero,
                dstSize = IntSize(size.width.toInt(), size.height.toInt())
            )

            // Build crop path
            val cropPath = Path().apply {
                moveTo(corners.topLeft.x, corners.topLeft.y)
                lineTo(corners.topRight.x, corners.topRight.y)
                lineTo(corners.bottomRight.x, corners.bottomRight.y)
                lineTo(corners.bottomLeft.x, corners.bottomLeft.y)
                close()
            }

            // Dark overlay outside crop
            clipPath(cropPath, clipOp = ClipOp.Difference) {
                drawRect(Color.Black.copy(alpha = 0.5f))
            }

            // Crop border
            drawPath(cropPath, Color.White, style = Stroke(width = 2.dp.toPx()))

            // Corner handles
            val allHandles = listOf(corners.topLeft, corners.topRight, corners.bottomRight, corners.bottomLeft)
            for (handle in allHandles) {
                drawCircle(Color.White.copy(alpha = 0.3f), radius = handleRadiusPx * 1.5f, center = handle)
                drawCircle(Color.White, radius = handleRadiusPx, center = handle)
            }

            // Edge midpoint handles
            val edges = listOf(
                Offset((corners.topLeft.x + corners.topRight.x) / 2f, (corners.topLeft.y + corners.topRight.y) / 2f),
                Offset((corners.topRight.x + corners.bottomRight.x) / 2f, (corners.topRight.y + corners.bottomRight.y) / 2f),
                Offset((corners.bottomRight.x + corners.bottomLeft.x) / 2f, (corners.bottomRight.y + corners.bottomLeft.y) / 2f),
                Offset((corners.bottomLeft.x + corners.topLeft.x) / 2f, (corners.bottomLeft.y + corners.topLeft.y) / 2f)
            )
            for (mid in edges) {
                drawCircle(Color.White.copy(alpha = 0.6f), radius = handleRadiusPx * 0.6f, center = mid)
            }
        }
    }
}
