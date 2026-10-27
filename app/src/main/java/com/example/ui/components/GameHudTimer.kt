package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Formats a duration in seconds into a human-readable digital clock string.
 * e.g., 65s -> "01:05", 3665s -> "1:01:05"
 */
fun formatElapsedTime(seconds: Long): String {
  val hours = seconds / 3600
  val minutes = (seconds % 3600) / 60
  val secs = seconds % 60
  return if (hours > 0) {
    String.format("%d:%02d:%02d", hours, minutes, secs)
  } else {
    String.format("%02d:%02d", minutes, secs)
  }
}

/**
 * Dedicated Game HUD Timer component to track and display the user's elapsed time
 * while solving a puzzle, with pause/resume support, best time indicator, and animations.
 */
@Composable
fun GameHudTimer(
  elapsedSeconds: Long,
  isPaused: Boolean,
  isCompleted: Boolean,
  onTogglePause: () -> Unit,
  modifier: Modifier = Modifier,
  bestTimeSeconds: Long? = null,
  showBestTime: Boolean = true
) {
  val formattedTime = remember(elapsedSeconds) { formatElapsedTime(elapsedSeconds) }

  // Ticking pulse animation when active and solving
  val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = if (!isPaused && !isCompleted) 1.25f else 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  val containerColor by animateColorAsState(
    targetValue = when {
      isCompleted -> Color(0xFF10B981).copy(alpha = 0.18f)
      isPaused -> Color(0xFFF59E0B).copy(alpha = 0.18f)
      else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    },
    label = "container_color"
  )

  val borderColor by animateColorAsState(
    targetValue = when {
      isCompleted -> Color(0xFF10B981).copy(alpha = 0.6f)
      isPaused -> Color(0xFFF59E0B).copy(alpha = 0.6f)
      else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    },
    label = "border_color"
  )

  val timerIconColor by animateColorAsState(
    targetValue = when {
      isCompleted -> Color(0xFF10B981)
      isPaused -> Color(0xFFF59E0B)
      else -> MaterialTheme.colorScheme.primary
    },
    label = "icon_color"
  )

  val accessibilityText = buildString {
    append("Elapsed time: $formattedTime.")
    if (isCompleted) append(" Puzzle completed.")
    else if (isPaused) append(" Game paused. Tap to resume.")
    else append(" Game running. Tap to pause.")
    if (bestTimeSeconds != null) {
      append(" Best time: ${formatElapsedTime(bestTimeSeconds)}.")
    }
  }

  Surface(
    modifier = modifier
      .defaultMinSize(minHeight = 48.dp)
      .clip(RoundedCornerShape(24.dp))
      .border(1.dp, borderColor, RoundedCornerShape(24.dp))
      .clickable(
        role = Role.Button,
        onClick = onTogglePause,
        enabled = !isCompleted
      )
      .semantics { contentDescription = accessibilityText }
      .testTag("game_hud_timer"),
    shape = RoundedCornerShape(24.dp),
    color = containerColor,
    tonalElevation = 2.dp
  ) {
    Row(
      modifier = Modifier
        .padding(horizontal = 14.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      // Left indicator icon
      Box(
        modifier = Modifier.size(24.dp),
        contentAlignment = Alignment.Center
      ) {
        when {
          isCompleted -> {
            Icon(
              imageVector = Icons.Default.EmojiEvents,
              contentDescription = null,
              tint = Color(0xFFEAB308),
              modifier = Modifier.size(20.dp)
            )
          }
          isPaused -> {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Resume",
              tint = Color(0xFFF59E0B),
              modifier = Modifier.size(18.dp)
            )
          }
          else -> {
            // Pulsing live indicator dot alongside stopwatch icon
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .scale(pulseScale)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = timerIconColor,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Time display and status
      Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = formattedTime,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = when {
              isCompleted -> Color(0xFF10B981)
              isPaused -> Color(0xFFF59E0B)
              else -> MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.testTag("timer_elapsed_text")
          )

          if (isPaused) {
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = Color(0xFFF59E0B).copy(alpha = 0.25f),
              modifier = Modifier.padding(start = 2.dp)
            ) {
              Text(
                text = "PAUSED",
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFD97706),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              )
            }
          }
        }

        // Best time subtext (if available)
        if (showBestTime && bestTimeSeconds != null) {
          Text(
            text = "Best: ${formatElapsedTime(bestTimeSeconds)}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
          )
        }
      }

      // Pause/play toggle quick indicator
      if (!isCompleted) {
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
          imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
          contentDescription = if (isPaused) "Resume puzzle timer" else "Pause puzzle timer",
          tint = if (isPaused) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
          modifier = Modifier
            .size(14.dp)
            .testTag("btn_timer_toggle")
        )
      }
    }
  }
}
