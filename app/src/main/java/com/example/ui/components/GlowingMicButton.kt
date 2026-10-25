package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.AssistantState
import com.example.ui.theme.MahiListeningGlow
import com.example.ui.theme.MahiPrimary
import com.example.ui.theme.MahiSecondary
import com.example.ui.theme.MahiSpeakingGlow
import com.example.ui.theme.MahiTertiary

@Composable
fun GlowingMicButton(
    assistantState: AssistantState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isListening = assistantState is AssistantState.Listening
    val isThinking = assistantState is AssistantState.Thinking
    val isSpeaking = assistantState is AssistantState.Speaking

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")

    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.55f else if (isSpeaking) 1.35f else 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening) 900 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale_1"
    )

    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = if (isListening || isSpeaking) 0.65f else 0.25f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening) 900 else 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha_1"
    )

    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.9f else 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening) 1200 else 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale_2"
    )

    val pulseAlpha2 by infiniteTransition.animateFloat(
        initialValue = if (isListening) 0.45f else 0.15f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening) 1200 else 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha_2"
    )

    val glowColor = when {
        isListening -> MahiListeningGlow
        isSpeaking -> MahiSpeakingGlow
        isThinking -> MahiSecondary
        else -> MahiPrimary
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(110.dp)
    ) {
        // Outer pulsing ripple ring 2
        Box(
            modifier = Modifier
                .size(76.dp)
                .scale(pulseScale2)
                .clip(CircleShape)
                .background(glowColor.copy(alpha = pulseAlpha2))
        )

        // Outer pulsing ripple ring 1
        Box(
            modifier = Modifier
                .size(76.dp)
                .scale(pulseScale1)
                .clip(CircleShape)
                .background(glowColor.copy(alpha = pulseAlpha1))
        )

        // Gradient button background
        val buttonGradient = when {
            isListening -> Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF00F5D4)))
            isSpeaking -> Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFF72585)))
            isThinking -> Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4)))
            else -> Brush.linearGradient(listOf(MahiPrimary, Color(0xFF7C3AED)))
        }

        val interactionSource = remember { MutableInteractionSource() }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(74.dp)
                .clip(CircleShape)
                .background(buttonGradient)
                .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                .testTag("microphone_button")
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = false, radius = 40.dp),
                    onClick = onClick
                )
        ) {
            when {
                isThinking -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(34.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                }
                isSpeaking -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "মাহী কথা বলছে",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                else -> {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.Mic,
                        contentDescription = if (isListening) "শুনছি, কথা বলুন" else "কথা বলতে চাপুন",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AudioWaveformIndicator(
    rmsDb: Float,
    isListening: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isListening) return

    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "anim_offset"
    )

    val dbNormalized = (rmsDb / 10f).coerceIn(0.1f, 1.0f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(Color(0xFF1E1438), RoundedCornerShape(16.dp))
            .border(1.dp, MahiListeningGlow.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        val heights = listOf(8, 16, 26, 34, 22, 12, 28, 18, 10)
        heights.forEachIndexed { index, baseHeight ->
            val factor = if (index % 2 == 0) animOffset else (1.4f - animOffset)
            val barHeight = (baseHeight * (0.6f + dbNormalized * 0.7f) * factor).coerceIn(4f, 40f)

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(barHeight.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(MahiListeningGlow, MahiSecondary)
                        )
                    )
            )
            if (index < heights.lastIndex) {
                Spacer(modifier = Modifier.width(3.dp))
            }
        }
    }
}
