package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlin.random.Random

@Composable
fun VictoryDialog(
  puzzleTitle: String,
  bitmap: ImageBitmap,
  pieceCount: Int,
  elapsedSeconds: Long,
  onPlayAgain: () -> Unit,
  onBackToGallery: () -> Unit
) {
  // Animated floating confetti particles
  val confettiAnim = remember { Animatable(0f) }
  LaunchedEffect(Unit) {
    confettiAnim.animateTo(
      targetValue = 1f,
      animationSpec = infiniteRepeatable(
        animation = tween(2500, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
      )
    )
  }

  Dialog(onDismissRequest = onBackToGallery) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("victory_dialog")
    ) {
      Box(modifier = Modifier.fillMaxWidth()) {
        // Confetti Canvas in background
        Canvas(
          modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
        ) {
          val rand = Random(42)
          val colors = listOf(
            Color(0xFFFFD54F), Color(0xFFFF4081), Color(0xFF00E5FF),
            Color(0xFF76FF03), Color(0xFFFF9100), Color(0xFFE040FB)
          )
          for (i in 0 until 40) {
            val startX = rand.nextFloat() * size.width
            val speed = 0.5f + rand.nextFloat() * 0.8f
            val curY = ((confettiAnim.value * speed * size.height + i * 20) % size.height)
            val pColor = colors[i % colors.size]
            val pSize = 6f + rand.nextFloat() * 6f
            drawCircle(
              color = pColor.copy(alpha = 0.8f),
              radius = pSize,
              center = Offset(startX, curY)
            )
          }
        }

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Trophy Badge
          Box(
            modifier = Modifier
              .size(68.dp)
              .clip(CircleShape)
              .background(Color(0xFFFFD54F).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Text(text = "🏆", fontSize = 36.sp)
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "Puzzle Completed!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = puzzleTitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(12.dp))

          // 3 Star Rating
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) {
              Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFFD54F),
                modifier = Modifier.size(32.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Stats Banner (Time & Pieces)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
              .padding(vertical = 12.dp, horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "Time",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              val mins = elapsedSeconds / 60
              val secs = elapsedSeconds % 60
              Text(
                text = String.format("%02d:%02d", mins, secs),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "Pieces",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "$pieceCount pcs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }

          Spacer(modifier = Modifier.height(24.dp))

          // Action Buttons
          Button(
            onClick = onPlayAgain,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("btn_play_again"),
            shape = RoundedCornerShape(14.dp)
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Play Again", fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedButton(
            onClick = onBackToGallery,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("btn_back_to_gallery"),
            shape = RoundedCornerShape(14.dp)
          ) {
            Icon(Icons.Default.Home, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Back to Gallery", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
