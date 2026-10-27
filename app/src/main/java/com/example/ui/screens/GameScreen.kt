package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GameHudTimer
import com.example.ui.components.PieceTray
import com.example.ui.components.PuzzleBoard
import com.example.ui.components.VictoryDialog
import com.example.ui.viewmodel.ActiveGameState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
  gameState: ActiveGameState,
  isSoundEnabled: Boolean = true,
  onToggleSound: () -> Unit = {},
  onBack: () -> Unit,
  onTogglePause: () -> Unit,
  onToggleBorderFilter: () -> Unit,
  onToggleGhostImage: () -> Unit,
  onToggleGridLines: () -> Unit,
  onUseHint: () -> Unit,
  onRestart: () -> Unit,
  onSelectPiece: (Int?) -> Unit,
  onSnapPiece: (Int) -> Unit,
  onTryDropPiece: (pieceId: Int, dropX: Float, dropY: Float, cellW: Float, cellH: Float) -> Boolean,
  modifier: Modifier = Modifier
) {
  BackHandler {
    onBack()
  }

  var boardWidth by remember { mutableFloatStateOf(0f) }
  var boardHeight by remember { mutableFloatStateOf(0f) }

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("btn_game_back")
          ) {
            Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back to Gallery"
            )
          }
        },
        title = {
          Text(
            text = gameState.puzzleItem.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1
          )
        },
        actions = {
          // Sound Effects Toggle (MediaPlayer)
          IconButton(
            onClick = onToggleSound,
            modifier = Modifier.testTag("btn_toggle_sound")
          ) {
            Icon(
              imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
              contentDescription = if (isSoundEnabled) "Mute sound effects" else "Enable sound effects",
              tint = if (isSoundEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Ghost Preview Toggle (Easybrain staple)
          IconButton(
            onClick = onToggleGhostImage,
            modifier = Modifier.testTag("btn_toggle_ghost")
          ) {
            Icon(
              imageVector = if (gameState.ghostImageVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
              contentDescription = "Toggle Reference Ghost",
              tint = if (gameState.ghostImageVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Grid Lines Toggle
          IconButton(
            onClick = onToggleGridLines,
            modifier = Modifier.testTag("btn_toggle_grid")
          ) {
            Icon(
              imageVector = Icons.Default.GridOn,
              contentDescription = "Toggle Grid",
              tint = if (gameState.showGridLines) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Hint 💡 Button with badge count
          IconButton(
            onClick = onUseHint,
            enabled = gameState.remainingHints > 0 && !gameState.isCompleted,
            modifier = Modifier.testTag("btn_use_hint")
          ) {
            BadgedBox(
              badge = {
                Badge(containerColor = Color(0xFFFFB300), contentColor = Color.Black) {
                  Text(
                    text = "${gameState.remainingHints}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                  )
                }
              }
            ) {
              Icon(
                Icons.Default.Lightbulb,
                contentDescription = "Use Hint",
                tint = if (gameState.remainingHints > 0) Color(0xFFFFB300) else Color.Gray
              )
            }
          }

          // Restart Button
          IconButton(
            onClick = onRestart,
            modifier = Modifier.testTag("btn_restart_puzzle")
          ) {
            Icon(Icons.Default.Refresh, contentDescription = "Restart Puzzle")
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(bottom = 0.dp),
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        // Game HUD Bar: Difficulty / Progress stats and Live GameHudTimer
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Difficulty badge & placed progress
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.padding(end = 8.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${gameState.difficulty.pieceCount} pcs",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "• ${gameState.placedCount}/${gameState.totalCount}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          // Dedicated Game HUD Timer component
          GameHudTimer(
            elapsedSeconds = gameState.elapsedSeconds,
            isPaused = gameState.isPaused,
            isCompleted = gameState.isCompleted,
            bestTimeSeconds = gameState.puzzleItem.bestTimeSeconds,
            onTogglePause = onTogglePause
          )
        }

        // Board Area
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = 16.dp, vertical = 4.dp),
          contentAlignment = Alignment.Center
        ) {
          PuzzleBoard(
            bitmap = gameState.bitmap,
            pieces = gameState.pieces,
            rows = gameState.difficulty.rows,
            cols = gameState.difficulty.cols,
            ghostVisible = gameState.ghostImageVisible,
            ghostOpacity = gameState.ghostOpacity,
            showGridLines = gameState.showGridLines,
            selectedPieceId = gameState.selectedPieceId,
            recentlySnappedId = gameState.recentlySnappedPieceId,
            onBoardPositioned = { w, h ->
              boardWidth = w
              boardHeight = h
            },
            onBoardTapAt = { touchX, touchY, cellW, cellH ->
              if (gameState.selectedPieceId != null) {
                // Try snapping the selected piece at this tap location
                val snapped = onTryDropPiece(
                  gameState.selectedPieceId,
                  touchX - cellW / 2f,
                  touchY - cellH / 2f,
                  cellW,
                  cellH
                )
                if (!snapped) {
                  // Direct snap if tapped in the right cell
                  val piece = gameState.pieces.find { it.id == gameState.selectedPieceId }
                  if (piece != null) {
                    val clickedCol = (touchX / cellW).toInt()
                    val clickedRow = (touchY / cellH).toInt()
                    if (clickedCol == piece.col && clickedRow == piece.row) {
                      onSnapPiece(piece.id)
                    }
                  }
                }
              }
            }
          )
        }

        // Bottom Piece Tray
        PieceTray(
          pieces = gameState.pieces,
          bitmap = gameState.bitmap,
          rows = gameState.difficulty.rows,
          cols = gameState.difficulty.cols,
          placedCount = gameState.placedCount,
          totalCount = gameState.totalCount,
          borderOnlyFilter = gameState.borderOnlyFilter,
          selectedPieceId = gameState.selectedPieceId,
          onToggleBorderFilter = onToggleBorderFilter,
          onSelectPiece = onSelectPiece,
          onPlaceSelected = {
            if (gameState.selectedPieceId != null) {
              onSnapPiece(gameState.selectedPieceId)
            }
          }
        )
      }

      // Pause Overlay
      if (gameState.isPaused) {
        Surface(
          modifier = Modifier
            .fillMaxSize()
            .clickable { onTogglePause() }
            .testTag("pause_overlay"),
          color = Color.Black.copy(alpha = 0.75f)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                Icons.Default.Pause,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(54.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Game Paused",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Time: ${com.example.ui.components.formatElapsedTime(gameState.elapsedSeconds)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFFFD54F)
              )
              Spacer(modifier = Modifier.height(18.dp))
              Button(
                onClick = onTogglePause,
                modifier = Modifier.testTag("btn_resume_game")
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Resume")
              }
            }
          }
        }
      }

      // Victory Dialog on completion
      if (gameState.isCompleted) {
        VictoryDialog(
          puzzleTitle = gameState.puzzleItem.title,
          bitmap = gameState.bitmap,
          pieceCount = gameState.totalCount,
          elapsedSeconds = gameState.elapsedSeconds,
          onPlayAgain = onRestart,
          onBackToGallery = onBack
        )
      }
    }
  }
}
