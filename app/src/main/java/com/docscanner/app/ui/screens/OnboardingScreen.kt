package com.docscanner.app.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CropFree
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> WelcomePage()
                1 -> CameraPermissionPage(onGranted = { scope.launch { pagerState.animateScrollToPage(2) } })
                2 -> StoragePermissionPage(onGranted = { onComplete() })
            }
        }

        // Page indicator + button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 48.dp)
        ) {
            // Page dots
            Row(horizontalArrangement = Arrangement.Center) {
                repeat(3) { i ->
                    val width by animateDpAsState(if (i == pagerState.currentPage) 24.dp else 8.dp)
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(width)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (i == pagerState.currentPage) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (pagerState.currentPage < 2) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = when (pagerState.currentPage) {
                        0 -> "Get Started"
                        1 -> "Continue"
                        else -> "Start Scanning"
                    },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

            AnimatedVisibility(
                visible = pagerState.currentPage > 0,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TextButton(onClick = onComplete) {
                    Text("Skip", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }
    }
}

// Custom drawn icon matching the app icon: viewfinder corners + document + scan line
@Composable
private fun AppIconDrawing(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val lineColor = primary
    val scanColor = Color(0xFF66BBFF)
    val textLineColor = primary.copy(alpha = 0.4f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.03f
        val cornerLen = w * 0.18f
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Viewfinder corners
        val margin = w * 0.12f

        // Top-left corner
        drawLine(lineColor, Offset(margin, margin + cornerLen), Offset(margin, margin), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(lineColor, Offset(margin, margin), Offset(margin + cornerLen, margin), strokeWidth = strokeWidth, cap = StrokeCap.Round)

        // Top-right corner
        drawLine(lineColor, Offset(w - margin - cornerLen, margin), Offset(w - margin, margin), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(lineColor, Offset(w - margin, margin), Offset(w - margin, margin + cornerLen), strokeWidth = strokeWidth, cap = StrokeCap.Round)

        // Bottom-right corner
        drawLine(lineColor, Offset(w - margin, h - margin - cornerLen), Offset(w - margin, h - margin), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(lineColor, Offset(w - margin, h - margin), Offset(w - margin - cornerLen, h - margin), strokeWidth = strokeWidth, cap = StrokeCap.Round)

        // Bottom-left corner
        drawLine(lineColor, Offset(margin + cornerLen, h - margin), Offset(margin, h - margin), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(lineColor, Offset(margin, h - margin), Offset(margin, h - margin - cornerLen), strokeWidth = strokeWidth, cap = StrokeCap.Round)

        // Document body
        val docLeft = w * 0.28f
        val docTop = h * 0.22f
        val docRight = w * 0.72f
        val docBottom = h * 0.78f
        val foldSize = w * 0.1f
        val docStroke = Stroke(width = strokeWidth * 0.8f, join = StrokeJoin.Round)

        val docPath = Path().apply {
            moveTo(docLeft, docTop)
            lineTo(docRight - foldSize, docTop)
            lineTo(docRight, docTop + foldSize)
            lineTo(docRight, docBottom)
            lineTo(docLeft, docBottom)
            close()
        }
        drawPath(docPath, lineColor, style = docStroke)

        // Document fold
        val foldPath = Path().apply {
            moveTo(docRight - foldSize, docTop)
            lineTo(docRight - foldSize, docTop + foldSize)
            lineTo(docRight, docTop + foldSize)
        }
        drawPath(foldPath, lineColor, style = docStroke)

        // Text lines inside document
        val lineLeft = docLeft + w * 0.08f
        val lineRight = docRight - w * 0.08f
        val thinStroke = strokeWidth * 0.7f
        drawLine(textLineColor, Offset(lineLeft, h * 0.45f), Offset(lineRight, h * 0.45f), strokeWidth = thinStroke, cap = StrokeCap.Round)
        drawLine(textLineColor, Offset(lineLeft, h * 0.52f), Offset(lineRight, h * 0.52f), strokeWidth = thinStroke, cap = StrokeCap.Round)
        drawLine(textLineColor, Offset(lineLeft, h * 0.59f), Offset(lineLeft + (lineRight - lineLeft) * 0.6f, h * 0.59f), strokeWidth = thinStroke, cap = StrokeCap.Round)

        // Scan line (horizontal glowing line across the middle)
        val scanY = h * 0.49f
        drawLine(
            scanColor,
            Offset(margin * 0.5f, scanY),
            Offset(w - margin * 0.5f, scanY),
            strokeWidth = strokeWidth * 0.6f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun WelcomePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Custom app icon matching the launcher icon
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            AppIconDrawing(modifier = Modifier.size(100.dp))
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            "Welcome to",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Doc Scanner",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Scan, organize, and share your\ndocuments with ease.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(44.dp))

        // Feature highlights
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            FeatureRow(Icons.Outlined.CropFree, Color(0xFF338FFF), "Smart Scanning", "Auto-detect document edges")
            FeatureRow(Icons.Outlined.Tune, Color(0xFF9C27B0), "Enhance & Filter", "Adjust brightness, contrast & more")
            FeatureRow(Icons.Outlined.Share, Color(0xFFFF9800), "Export Anywhere", "Save as PDF or share instantly")
        }

        Spacer(modifier = Modifier.weight(1.2f))
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, color: Color, title: String, subtitle: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
        }
    }
}

@Composable
private fun CameraPermissionPage(onGranted: () -> Unit) {
    var granted by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        granted = isGranted
        if (isGranted) onGranted()
    }
    val context = LocalContext.current

    PermissionPage(
        icon = Icons.Outlined.CameraAlt,
        iconColor = Color(0xFF338FFF),
        title = "Camera Access",
        description = "Doc Scanner uses your camera to capture documents. All processing happens on your device.",
        granted = granted,
        grantedLabel = "Camera access granted",
        buttonLabel = "Allow Camera",
        onRequest = { launcher.launch(Manifest.permission.CAMERA) },
        onOpenSettings = {
            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            })
        }
    )
}

@Composable
private fun StoragePermissionPage(onGranted: () -> Unit) {
    var granted by remember { mutableStateOf(false) }
    val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES
                     else Manifest.permission.READ_EXTERNAL_STORAGE
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        granted = isGranted
        if (isGranted) onGranted()
    }
    val context = LocalContext.current

    PermissionPage(
        icon = Icons.Outlined.PhotoLibrary,
        iconColor = Color(0xFF4CAF50),
        title = "Photo Library",
        description = "Import photos from your library to scan and enhance them.",
        granted = granted,
        grantedLabel = "Photo access granted",
        buttonLabel = "Allow Photos",
        onRequest = { launcher.launch(permission) },
        onOpenSettings = {
            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            })
        }
    )
}

@Composable
private fun PermissionPage(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    granted: Boolean,
    grantedLabel: String,
    buttonLabel: String,
    onRequest: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(56.dp))
        }

        Spacer(modifier = Modifier.height(28.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 26.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            description,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(28.dp))

        if (granted) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(grantedLabel, fontWeight = FontWeight.SemiBold, color = Color(0xFF4CAF50), fontSize = 14.sp)
            }
        } else {
            Button(
                onClick = onRequest,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = iconColor.copy(alpha = 0.1f))
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(buttonLabel, fontWeight = FontWeight.SemiBold, color = iconColor)
            }
        }
    }
}
