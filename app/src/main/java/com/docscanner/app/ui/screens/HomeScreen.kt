package com.docscanner.app.ui.screens

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

@Composable
fun HomeScreen(
    onStartSession: (List<Bitmap>) -> Unit,
    onCropImage: (Bitmap) -> Unit,
    onNavigateToLibrary: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // ML Kit Document Scanner
    val scannerOptions = GmsDocumentScannerOptions.Builder()
        .setGalleryImportAllowed(true)
        .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
        .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
        .build()
    val scanner = GmsDocumentScanning.getClient(scannerOptions)

    val scanLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            scanResult?.pages?.let { pages ->
                val bitmaps = pages.mapNotNull { page ->
                    uriToBitmap(context, page.imageUri)
                }
                if (bitmaps.isNotEmpty()) onStartSession(bitmaps)
            }
        }
    }

    // Photo picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uriToBitmap(context, it)?.let { bitmap ->
                onCropImage(bitmap)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top spacer
        Spacer(modifier = Modifier.weight(1f))

        // Logo section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                DocIconDrawing(modifier = Modifier.size(60.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Doc Scanner",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Scan documents instantly",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.weight(1.2f))

        // Action buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 60.dp)
        ) {
            ActionCard(
                icon = Icons.Outlined.CameraAlt,
                tint = Color(0xFF338FFF),
                title = "Scan Document",
                subtitle = "Use camera to scan",
                onClick = {
                    activity?.let { act ->
                        scanner.getStartScanIntent(act)
                            .addOnSuccessListener { intentSender ->
                                scanLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                            }
                    }
                }
            )

            ActionCard(
                icon = Icons.Outlined.PhotoLibrary,
                tint = Color(0xFF4CAF50),
                title = "Choose from Library",
                subtitle = "Pick an existing photo",
                onClick = { photoPickerLauncher.launch("image/*") }
            )

            ActionCard(
                icon = Icons.Outlined.FolderOpen,
                tint = Color(0xFF9C27B0),
                title = "My Library",
                subtitle = "View saved documents",
                onClick = onNavigateToLibrary
            )
        }
    }
}

@Composable
private fun ActionCard(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 0.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun DocIconDrawing(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val scanColor = Color(0xFF66BBFF)
    val textLineColor = primary.copy(alpha = 0.4f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.03f
        val cornerLen = w * 0.18f
        val margin = w * 0.08f

        // Viewfinder corners
        drawLine(primary, Offset(margin, margin + cornerLen), Offset(margin, margin), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(primary, Offset(margin, margin), Offset(margin + cornerLen, margin), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(primary, Offset(w - margin - cornerLen, margin), Offset(w - margin, margin), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(primary, Offset(w - margin, margin), Offset(w - margin, margin + cornerLen), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(primary, Offset(w - margin, h - margin - cornerLen), Offset(w - margin, h - margin), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(primary, Offset(w - margin, h - margin), Offset(w - margin - cornerLen, h - margin), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(primary, Offset(margin + cornerLen, h - margin), Offset(margin, h - margin), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(primary, Offset(margin, h - margin), Offset(margin, h - margin - cornerLen), strokeWidth = strokeWidth, cap = StrokeCap.Round)

        // Document
        val docLeft = w * 0.28f; val docTop = h * 0.22f; val docRight = w * 0.72f; val docBottom = h * 0.78f; val fold = w * 0.1f
        val docStroke = Stroke(width = strokeWidth * 0.8f, join = StrokeJoin.Round)
        drawPath(Path().apply {
            moveTo(docLeft, docTop); lineTo(docRight - fold, docTop); lineTo(docRight, docTop + fold)
            lineTo(docRight, docBottom); lineTo(docLeft, docBottom); close()
        }, primary, style = docStroke)
        drawPath(Path().apply {
            moveTo(docRight - fold, docTop); lineTo(docRight - fold, docTop + fold); lineTo(docRight, docTop + fold)
        }, primary, style = docStroke)

        // Text lines
        val ll = docLeft + w * 0.08f; val lr = docRight - w * 0.08f; val ts = strokeWidth * 0.7f
        drawLine(textLineColor, Offset(ll, h * 0.45f), Offset(lr, h * 0.45f), strokeWidth = ts, cap = StrokeCap.Round)
        drawLine(textLineColor, Offset(ll, h * 0.52f), Offset(lr, h * 0.52f), strokeWidth = ts, cap = StrokeCap.Round)
        drawLine(textLineColor, Offset(ll, h * 0.59f), Offset(ll + (lr - ll) * 0.6f, h * 0.59f), strokeWidth = ts, cap = StrokeCap.Round)

        // Scan line
        drawLine(scanColor, Offset(margin * 0.5f, h * 0.49f), Offset(w - margin * 0.5f, h * 0.49f), strokeWidth = strokeWidth * 0.6f, cap = StrokeCap.Round)
    }
}

internal fun uriToBitmap(context: android.content.Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= 28) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        null
    }
}
