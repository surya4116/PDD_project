package com.simats.myapplication.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.myapplication.ui.theme.SmartQTheme
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

data class GlowOrb(
    val initialX: Float,
    val initialY: Float,
    val sizeDp: Float,
    val color: Color,
    val speedX: Float,
    val speedY: Float,
    val amplitude: Float
)

@Composable
fun SplashScreen(onNavigateNext: () -> Unit) {
    val currentOnNavigateNext by rememberUpdatedState(onNavigateNext)
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ), label = "alpha_anim"
    )

    val progress by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 2200,
            easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
        ), label = "progress"
    )

    // Infinite transition for cinematic animations
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")

    // Slow rotation for borders
    val rotationBorder1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation_border1"
    )

    val rotationBorder2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation_border2"
    )

    // Radar sweep rotation
    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "radar_angle"
    )

    // Drifting orbs animation parameter
    val driftTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "drift_time"
    )

    // Scale pulse for central elements
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale_pulse"
    )

    // Initialize orbs positions and properties
    val orbs = remember {
        listOf(
            GlowOrb(0.18f, 0.22f, 200f, Color(0xFF7F00FF).copy(alpha = 0.24f), 1.0f, 0.7f, 50f),
            GlowOrb(0.82f, 0.28f, 240f, Color(0xFF00FFCC).copy(alpha = 0.18f), -0.8f, 1.1f, 60f),
            GlowOrb(0.32f, 0.78f, 260f, Color(0xFFFF007F).copy(alpha = 0.16f), 1.2f, -0.8f, 70f),
            GlowOrb(0.78f, 0.82f, 190f, Color(0xFF00E5FF).copy(alpha = 0.20f), -1.1f, -0.9f, 55f)
        )
    }

    // Resolve shadow pixel offsets using Density context
    val density = LocalDensity.current
    val shadowOffsetTitle = remember(density) { with(density) { Offset(0f, 6.dp.toPx()) } }
    val shadowOffsetTagline = remember(density) { with(density) { Offset(0f, 2.dp.toPx()) } }

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500)
        currentOnNavigateNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0C021C), // Deep midnight plum top
                        Color(0xFF14052C), // Dark regal purple
                        Color(0xFF090114)  // Absolute black-purple bottom
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // 1. Draw drifting glowing auroras on Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            orbs.forEachIndexed { index, orb ->
                val driftX = sin(driftTime * orb.speedX + index) * orb.amplitude
                val driftY = cos(driftTime * orb.speedY + index) * orb.amplitude
                
                val centerX = size.width * orb.initialX + driftX
                val centerY = size.height * orb.initialY + driftY
                val radius = orb.sizeDp.dp.toPx()
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(orb.color, Color.Transparent),
                        center = Offset(centerX, centerY),
                        radius = radius
                    ),
                    center = Offset(centerX, centerY),
                    radius = radius
                )
            }
        }

        // 2. Center visual components
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(alphaAnim)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.graphicsLayer(scaleX = scalePulse, scaleY = scalePulse)
            ) {
                // Rotating Border 1 (Neon Purple)
                Box(
                    modifier = Modifier
                        .size(154.dp)
                        .graphicsLayer(rotationZ = rotationBorder1)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color(0xFF7F00FF), 
                                        Color.Transparent, 
                                        Color(0xFF7F00FF)
                                    )
                                ),
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                )

                // Rotating Border 2 (Neon Turquoise)
                Box(
                    modifier = Modifier
                        .size(142.dp)
                        .graphicsLayer(rotationZ = rotationBorder2)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color(0xFF00FFCC), 
                                        Color.Transparent, 
                                        Color(0xFF00FFCC)
                                    )
                                ),
                                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                )

                // Glowing Neon Backdrop shadow
                Box(
                    modifier = Modifier
                        .size(118.dp)
                        .shadow(
                            elevation = 36.dp,
                            shape = CircleShape,
                            clip = false,
                            ambientColor = Color(0xFF7F00FF),
                            spotColor = Color(0xFF00FFCC)
                        )
                )

                // Glassmorphic Central Dial Container
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                    modifier = Modifier.size(120.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(120.dp)) {
                            val cx = size.width / 2
                            val cy = size.height / 2
                            val r = size.width / 2

                            // Outer thin boundary clock ring
                            drawCircle(
                                color = Color.White.copy(alpha = 0.15f),
                                radius = r * 0.9f,
                                style = Stroke(width = 1.dp.toPx())
                            )

                            // Ticking radar sector gradient
                            drawArc(
                                brush = Brush.sweepGradient(
                                    0.0f to Color(0xFF00FFCC).copy(alpha = 0.35f),
                                    0.85f to Color.Transparent,
                                    1.0f to Color(0xFF00FFCC).copy(alpha = 0.35f),
                                    center = Offset(cx, cy)
                                ),
                                startAngle = radarAngle - 60f,
                                sweepAngle = 60f,
                                useCenter = true
                            )

                            // Radar sweeping sharp line
                            val sweepRad = (radarAngle * Math.PI / 180.0)
                            val lineEndX = cx + r * 0.9f * cos(sweepRad).toFloat()
                            val lineEndY = cy + r * 0.9f * sin(sweepRad).toFloat()
                            drawLine(
                                color = Color(0xFF00FFCC),
                                start = Offset(cx, cy),
                                end = Offset(lineEndX, lineEndY),
                                strokeWidth = 2.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            // 12 Clock ticks
                            for (i in 0 until 12) {
                                val tickAngle = i * 30 * Math.PI / 180.0
                                val tickStartRadius = r * 0.8f
                                val tickEndRadius = r * 0.9f
                                
                                val sX = cx + tickStartRadius * cos(tickAngle).toFloat()
                                val sY = cy + tickStartRadius * sin(tickAngle).toFloat()
                                val eX = cx + tickEndRadius * cos(tickAngle).toFloat()
                                val eY = cy + tickEndRadius * sin(tickAngle).toFloat()
                                
                                val isQuarter = i % 3 == 0
                                val tickColor = if (isQuarter) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.3f)
                                val strokeW = if (isQuarter) 2.dp.toPx() else 1.dp.toPx()
                                
                                drawLine(
                                    color = tickColor,
                                    start = Offset(sX, sY),
                                    end = Offset(eX, eY),
                                    strokeWidth = strokeW,
                                    cap = StrokeCap.Round
                                )
                            }

                            // Minute hand - rotates twice over progress duration
                            val minAngle = (progress * 720.0 - 90.0) * Math.PI / 180.0
                            val minHandLen = r * 0.65f
                            val mx = cx + minHandLen * cos(minAngle).toFloat()
                            val my = cy + minHandLen * sin(minAngle).toFloat()
                            drawLine(
                                color = Color.White,
                                start = Offset(cx, cy),
                                end = Offset(mx, my),
                                strokeWidth = 2.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            // Glowing tip dot on minute hand
                            drawCircle(
                                color = Color(0xFF00FFCC),
                                radius = 2.5.dp.toPx(),
                                center = Offset(mx, my)
                            )

                            // Hour hand - rotates slowly
                            val hourAngle = (progress * 60.0 - 90.0) * Math.PI / 180.0
                            val hourHandLen = r * 0.45f
                            val hx = cx + hourHandLen * cos(hourAngle).toFloat()
                            val hy = cy + hourHandLen * sin(hourAngle).toFloat()
                            drawLine(
                                color = Color(0xFFC09BFF),
                                start = Offset(cx, cy),
                                end = Offset(hx, hy),
                                strokeWidth = 3.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            // Central metal pivot rivet
                            drawCircle(
                                color = Color.White,
                                radius = 4.dp.toPx(),
                                center = Offset(cx, cy)
                            )
                            drawCircle(
                                color = Color(0xFF0C021C),
                                radius = 1.5.dp.toPx(),
                                center = Offset(cx, cy)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Premium Gradient Text Title
            Text(
                text = "SmartQueue Pro",
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                style = TextStyle(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFE5CCFF),
                            Color(0xFFC48CFF)
                        )
                    ),
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color(0xFF7F00FF).copy(alpha = 0.5f),
                        offset = shadowOffsetTitle,
                        blurRadius = 14f
                    )
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Glowing Mint Tagline
            Text(
                text = "BOOK SMART • WAIT LESS",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.5.sp,
                color = Color(0xFF80FFE8),
                style = TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color(0xFF00FFCC).copy(alpha = 0.35f),
                        offset = shadowOffsetTagline,
                        blurRadius = 8f
                    )
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Glassmorphic status label
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.04f),
                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF00FFCC), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SECURE CLOUD SYNCHRONIZED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // 3. Neon loading progress bar at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 70.dp)
                .alpha(alphaAnim),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .height(6.dp)
                    .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(3.dp))
                    .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(3.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                // Background Track Glow Fill
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF7F00FF), 
                                    Color(0xFF00FFCC), 
                                    Color(0xFF00E5FF)
                                )
                            ),
                            shape = RoundedCornerShape(3.dp)
                        )
                )
                
                // Sliding tip glow light dot
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(10.dp)
                            .offset(x = 5.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = CircleShape,
                                clip = false,
                                ambientColor = Color(0xFF00FFCC),
                                spotColor = Color(0xFF00FFCC)
                            )
                            .background(Color.White, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Synchronization status text
            val syncText = if (progress < 0.4f) "INITIALIZING PROTOCOLS..."
                          else if (progress < 0.8f) "SYNCHRONIZING QUEUES..."
                          else "OPERATIONAL COMPLIANCE READY"

            Text(
                text = syncText,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = Color.White.copy(alpha = 0.4f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SmartQTheme {
        SplashScreen(onNavigateNext = {})
    }
}
