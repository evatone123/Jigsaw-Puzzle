package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.local.PuzzleProgressEntity
import com.example.data.model.PuzzleCatalogItem
import com.example.data.model.PuzzleDifficulty

@Composable
fun DifficultyDialog(
  puzzleItem: PuzzleCatalogItem,
  savedProgress: PuzzleProgressEntity? = null,
  onDismiss: () -> Unit,
  onResume: (PuzzleProgressEntity) -> Unit = {},
  onStart: (PuzzleDifficulty) -> Unit
) {
  var selectedDifficulty by remember { mutableStateOf(PuzzleDifficulty.MEDIUM_36) }
  val scrollState = rememberScrollState()

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("difficulty_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(scrollState)
          .padding(20.dp)
      ) {
        // Header with Close
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (savedProgress != null) "Resume or New Game" else "Select Difficulty",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Puzzle Preview Card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
        ) {
          if (puzzleItem.drawableResId != null) {
            Image(
              painter = painterResource(id = puzzleItem.drawableResId),
              contentDescription = puzzleItem.title,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxWidth()
            )
          } else if (puzzleItem.customUri != null) {
            AsyncImage(
              model = puzzleItem.customUri,
              contentDescription = puzzleItem.title,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxWidth()
            )
          }

          // Title & Category tag overlay
          Box(
            modifier = Modifier
              .align(Alignment.BottomStart)
              .fillMaxWidth()
              .background(Color.Black.copy(alpha = 0.6f))
              .padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = puzzleItem.title,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                )
                Text(
                  text = puzzleItem.category,
                  color = Color(0xFFFFD54F),
                  fontSize = 11.sp
                )
              }
              if (puzzleItem.completed && puzzleItem.bestTimeSeconds != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = formatElapsedTime(puzzleItem.bestTimeSeconds),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }
            }
          }
        }

        // Resume Section (If saved in-progress puzzle exists)
        if (savedProgress != null && !savedProgress.isCompleted) {
          Spacer(modifier = Modifier.height(16.dp))

          val placedCount = savedProgress.placedPieceIds
            .split(",")
            .filter { it.isNotEmpty() }
            .size
          val percent = if (savedProgress.pieceCount > 0) (placedCount * 100) / savedProgress.pieceCount else 0

          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("resume_saved_card")
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Saved Game In-Progress",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                  )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = formatElapsedTime(savedProgress.elapsedSeconds),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = "$placedCount of ${savedProgress.pieceCount} pieces placed ($percent%)",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
              )

              Spacer(modifier = Modifier.height(10.dp))

              Button(
                onClick = { onResume(savedProgress) },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(46.dp)
                  .testTag("btn_resume_puzzle"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Resume Puzzle ($percent%)", fontWeight = FontWeight.Bold)
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
              text = "OR START NEW GAME",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Difficulty Options
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          PuzzleDifficulty.entries.forEach { diff ->
            val isChosen = diff == selectedDifficulty
            val bg = if (isChosen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            val borderCol = if (isChosen) MaterialTheme.colorScheme.primary else Color.Transparent

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(bg)
                .border(2.dp, borderCol, RoundedCornerShape(12.dp))
                .clickable { selectedDifficulty = diff }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("option_${diff.name}"),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = diff.label,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = if (isChosen) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                val subtitle = when (diff) {
                  PuzzleDifficulty.EASY_16 -> "4×4 grid • Quick & Easy"
                  PuzzleDifficulty.MEDIUM_36 -> "6×6 grid • Classic Relaxed"
                  PuzzleDifficulty.HARD_64 -> "8×8 grid • Challenging"
                  PuzzleDifficulty.EXPERT_100 -> "10×10 grid • True Master"
                }
                Text(
                  text = subtitle,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              if (isChosen) {
                Icon(
                  Icons.Default.Check,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Play Button
        Button(
          onClick = { onStart(selectedDifficulty) },
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("btn_start_puzzle"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (savedProgress != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
          )
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (savedProgress != null) "Start New Game (${selectedDifficulty.pieceCount} Pcs)" else "Play Puzzle (${selectedDifficulty.pieceCount} Pieces)",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}
