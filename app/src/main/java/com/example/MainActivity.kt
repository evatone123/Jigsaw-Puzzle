package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.components.DifficultyDialog
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.GameScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PuzzleViewModel
import com.example.ui.viewmodel.ScreenState

class MainActivity : ComponentActivity() {

  private val viewModel: PuzzleViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          JigsawApp(viewModel = viewModel)
        }
      }
    }
  }
}

@Composable
fun JigsawApp(viewModel: PuzzleViewModel) {
  val screenState by viewModel.screenState.collectAsState()
  val catalog by viewModel.catalog.collectAsState()
  val activeGame by viewModel.activeGame.collectAsState()
  val selectedCategory by viewModel.selectedCategory.collectAsState()
  val pendingPuzzle by viewModel.pendingPuzzleSelection.collectAsState()
  val pendingProgress by viewModel.pendingSavedProgress.collectAsState()
  val savedProgressMap by viewModel.savedProgressMap.collectAsState()

  val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()

  when (screenState) {
    ScreenState.GALLERY -> {
      GalleryScreen(
        catalog = catalog,
        selectedCategory = selectedCategory,
        savedProgressMap = savedProgressMap,
        onSelectCategory = { viewModel.setCategory(it) },
        onSelectPuzzle = { viewModel.requestStartPuzzle(it) },
        onAddCustomPuzzle = { title, uri -> viewModel.createCustomPhotoPuzzle(title, uri) }
      )

      pendingPuzzle?.let { puzzleItem ->
        DifficultyDialog(
          puzzleItem = puzzleItem,
          savedProgress = pendingProgress,
          onDismiss = { viewModel.dismissDifficultyPicker() },
          onResume = { progress -> viewModel.resumeSavedPuzzle(puzzleItem, progress) },
          onStart = { diff -> viewModel.startPuzzle(puzzleItem, diff) }
        )
      }
    }

    ScreenState.GAME -> {
      activeGame?.let { game ->
        GameScreen(
          gameState = game,
          isSoundEnabled = isSoundEnabled,
          onToggleSound = { viewModel.toggleSound() },
          onBack = { viewModel.backToGallery() },
          onTogglePause = { viewModel.togglePause() },
          onToggleBorderFilter = { viewModel.toggleBorderFilter() },
          onToggleGhostImage = { viewModel.toggleGhostImage() },
          onToggleGridLines = { viewModel.toggleGridLines() },
          onUseHint = { viewModel.useHint() },
          onRestart = { viewModel.restartCurrentPuzzle() },
          onSelectPiece = { viewModel.selectPiece(it) },
          onSnapPiece = { viewModel.snapPiece(it) },
          onTryDropPiece = { pieceId, dropX, dropY, cellW, cellH ->
            viewModel.trySnapPiece(pieceId, dropX, dropY, cellW, cellH)
          }
        )
      }
    }
  }
}
