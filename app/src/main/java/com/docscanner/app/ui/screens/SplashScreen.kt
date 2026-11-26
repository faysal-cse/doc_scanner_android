package com.docscanner.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.docscanner.app.ui.theme.LaunchBackground
import com.docscanner.app.ui.theme.LaunchBackgroundDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val iconScale = remember { Animatable(0.6f) }
    val iconAlpha = remember { Animatable(0f) }
    val glowScale = remember { Animatable(0.5f) }
    val glowAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffsetY = remember { Animatable(12f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val scanLineY = remember { Animatable(-24f) }
    val scanLineAlpha = remember { Animatable(0f) }
    val dismissAlpha = remember { Animatable(1f) }
    val dismissScale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Phase 1: Icon entrance
        launch {
            iconScale.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow))
        }
        launch {
            iconAlpha.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow))
        }

        // Phase 2: Glow
        delay(150)
        launch { glowScale.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }
        launch { glowAlpha.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }

        // Phase 3: Title
        delay(150)
        launch { titleAlpha.animateTo(1f, tween(450, easing = FastOutSlowInEasing)) }
        launch { titleOffsetY.animateTo(0f, tween(450, easing = FastOutSlowInEasing)) }

        // Phase 4: Subtitle
        delay(250)
        launch { subtitleAlpha.animateTo(1f, tween(400)) }

        // Phase 5: Scan line
        launch {
            scanLineAlpha.animateTo(1f, tween(300))
        }
        launch {
            scanLineY.animateTo(24f, tween(1000, easing = FastOutSlowInEasing))
            scanLineY.animateTo(-24f, tween(1000, easing = FastOutSlowInEasing))
        }

        // Phase 6: Dismiss
        delay(1500)
        launch { dismissAlpha.animateTo(0f, tween(350)) }
        launch { dismissScale.animateTo(1.05f, tween(350)) }
        delay(350)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = dismissAlpha.value
                scaleX = dismissScale.value
                scaleY = dismissScale.value
            }
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(LaunchBackground, LaunchBackgroundDark),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height)
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Particle field
        Canvas(modifier = Modifier.fillMaxSize().alpha(subtitleAlpha.value)) {
            val particles = listOf(
                Triple(0.15f, 0.20f, 0.25f), Triple(0.82f, 0.18f, 0.20f),
                Triple(0.08f, 0.55f, 0.15f), Triple(0.90f, 0.48f, 0.22f),
                Triple(0.35f, 0.78f, 0.18f), Triple(0.72f, 0.72f, 0.20f),
                Triple(0.50f, 0.12f, 0.15f), Triple(0.25f, 0.42f, 0.12f),
                Triple(0.65f, 0.85f, 0.16f), Triple(0.92f, 0.32f, 0.14f)
            )
            for ((x, y, a) in particles) {
                drawCircle(
                    color = Color.White.copy(alpha = a),
                    radius = 3f,
                    center = Offset(x * size.width, y * size.height)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Icon composition
            Box(contentAlignment = Alignment.Center) {
                // Outer ring
                Canvas(
                    modifier = Modifier
                        .size(148.dp)
                        .scale(glowScale.value)
                        .alpha(glowAlpha.value)
                ) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.1f),
                        style = Stroke(width = 1.5f.dp.toPx())
                    )
                }

                // Frosted circle
                Canvas(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(iconScale.value)
                        .alpha(iconAlpha.value)
                ) {
                    drawCircle(color = Color.White.copy(alpha = 0.08f))
                }

                // Icon
                Box(
                    modifier = Modifier
                        .scale(iconScale.value)
                        .alpha(iconAlpha.value),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DocumentScanner,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )

                    // Scan line
                    Canvas(
                        modifier = Modifier
                            .size(width = 36.dp, height = 2.dp)
                            .graphicsLayer { translationY = scanLineY.value.dp.toPx() }
                            .alpha(scanLineAlpha.value)
                    ) {
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.7f),
                                    Color.Transparent
                                )
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "Doc Scanner",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .alpha(titleAlpha.value)
                    .graphicsLayer { translationY = titleOffsetY.value.dp.toPx() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tagline
            Text(
                text = "SCAN. ORGANIZE. SHARE.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(subtitleAlpha.value)
            )

            Spacer(modifier = Modifier.height(120.dp))

            // Bottom branding
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(subtitleAlpha.value * 0.35f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.DocumentScanner,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DocScanner",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
