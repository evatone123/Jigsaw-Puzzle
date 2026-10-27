package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PuzzleProgressEntity
import com.example.data.model.PuzzleCatalogItem
import com.example.data.model.PuzzleDifficulty
import com.example.data.model.PuzzlePiece
import com.example.data.repository.PuzzleRepository
import com.example.engine.JigsawGenerator
import com.example.engine.PuzzleBitmapHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.hypot

enum class ScreenState {
  GALLERY,
  GAME
}

data class ActiveGameState(
  val puzzleItem: PuzzleCatalogItem,
  val difficulty: PuzzleDifficulty,
  val bitmap: ImageBitmap,
  val pieces: List<PuzzlePiece>,
  val placedCount: Int = 0,
  val totalCount: Int = 0,
  val isCompleted: Boolean = false,
  val elapsedSeconds: Long = 0L,
  val isPaused: Boolean = false,
  val borderOnlyFilter: Boolean = false,
  val ghostImageVisible: Boolean = false,
  val ghostOpacity: Float = 0.28f,
  val showGridLines: Boolean = true,
  val remainingHints: Int = 3,
  val selectedPieceId: Int? = null,
  val recentlySnappedPieceId: Int? = null
)

class PuzzleViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = PuzzleRepository(application)
  val catalog = repository.catalog

  private val soundManager = com.example.engine.PuzzleSoundManager(application)
  val isSoundEnabled: StateFlow<Boolean> = soundManager.soundEnabled

  // Reactive map of puzzleId -> PuzzleProgressEntity from Room
  val savedProgressMap: StateFlow<Map<String, PuzzleProgressEntity>> =
    repository.allSavedProgress
      .map { list -> list.associateBy { it.puzzleId } }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
      )

  private val _screenState = MutableStateFlow(ScreenState.GALLERY)
  val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

  private val _activeGame = MutableStateFlow<ActiveGameState?>(null)
  val activeGame: StateFlow<ActiveGameState?> = _activeGame.asStateFlow()

  private val _selectedCategory = MutableStateFlow("All")
  val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

  // For difficulty picker bottom sheet
  private val _pendingPuzzleSelection = MutableStateFlow<PuzzleCatalogItem?>(null)
  val pendingPuzzleSelection: StateFlow<PuzzleCatalogItem?> = _pendingPuzzleSelection.asStateFlow()

  private val _pendingSavedProgress = MutableStateFlow<PuzzleProgressEntity?>(null)
  val pendingSavedProgress: StateFlow<PuzzleProgressEntity?> = _pendingSavedProgress.asStateFlow()

  private var timerJob: Job? = null

  private val vibrator: Vibrator? by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      vibratorManager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
  }

  fun setCategory(category: String) {
    _selectedCategory.value = category
  }

  fun requestStartPuzzle(item: PuzzleCatalogItem) {
    _pendingPuzzleSelection.value = item
    viewModelScope.launch {
      val saved = repository.getProgress(item.id)
      _pendingSavedProgress.value = saved
    }
  }

  fun dismissDifficultyPicker() {
    _pendingPuzzleSelection.value = null
    _pendingSavedProgress.value = null
  }

  fun resumeSavedPuzzle(item: PuzzleCatalogItem, progress: PuzzleProgressEntity) {
    val difficulty = try {
      PuzzleDifficulty.valueOf(progress.difficultyName)
    } catch (_: Exception) {
      PuzzleDifficulty.entries.find { it.pieceCount == progress.pieceCount } ?: PuzzleDifficulty.MEDIUM_36
    }
    startPuzzle(item, difficulty, resumeProgress = progress)
  }

  fun startPuzzle(
    item: PuzzleCatalogItem,
    difficulty: PuzzleDifficulty,
    resumeProgress: PuzzleProgressEntity? = null
  ) {
    _pendingPuzzleSelection.value = null
    _pendingSavedProgress.value = null

    viewModelScope.launch {
      val bitmap = PuzzleBitmapHelper.loadPuzzleBitmap(
        context = getApplication(),
        drawableResId = item.drawableResId,
        customUri = item.customUri,
        targetWidth = 1000,
        targetHeight = 750
      )

      val pieces = JigsawGenerator.generatePieces(
        rows = difficulty.rows,
        cols = difficulty.cols,
        seed = item.id.hashCode().toLong()
      )

      val placedIds = if (resumeProgress != null) {
        resumeProgress.placedPieceIds
          .split(",")
          .filter { it.isNotEmpty() }
          .mapNotNull { it.toIntOrNull() }
          .toSet()
      } else {
        emptySet()
      }

      val updatedPieces = pieces.map { piece ->
        if (placedIds.contains(piece.id)) {
          piece.copy(isPlaced = true, isOnBoard = false)
        } else {
          piece
        }
      }

      val initialElapsed = resumeProgress?.elapsedSeconds ?: 0L
      val initialHints = resumeProgress?.remainingHints ?: 3
      val placedCount = updatedPieces.count { it.isPlaced }

      _activeGame.value = ActiveGameState(
        puzzleItem = item,
        difficulty = difficulty,
        bitmap = bitmap,
        pieces = updatedPieces,
        placedCount = placedCount,
        totalCount = pieces.size,
        isCompleted = placedCount == pieces.size,
        elapsedSeconds = initialElapsed,
        isPaused = false,
        borderOnlyFilter = false,
        ghostImageVisible = false,
        remainingHints = initialHints
      )

      _screenState.value = ScreenState.GAME
      startTimer()

      // If starting fresh (no resume), clear any old draft in Room
      if (resumeProgress == null) {
        repository.deleteProgress(item.id)
      }
    }
  }

  private fun startTimer() {
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      var ticks = 0
      while (isActive) {
        delay(1000L)
        val current = _activeGame.value ?: break
        if (!current.isPaused && !current.isCompleted) {
          val nextElapsed = current.elapsedSeconds + 1
          _activeGame.value = current.copy(elapsedSeconds = nextElapsed)

          // Periodically save progress to Room every 5 seconds
          ticks++
          if (ticks % 5 == 0) {
            persistCurrentProgress(current.copy(elapsedSeconds = nextElapsed))
          }
        }
      }
    }
  }

  fun togglePause() {
    val current = _activeGame.value ?: return
    val newPauseState = !current.isPaused
    _activeGame.value = current.copy(isPaused = newPauseState)
    persistCurrentProgress(_activeGame.value!!)
  }

  fun toggleBorderFilter() {
    val current = _activeGame.value ?: return
    _activeGame.value = current.copy(borderOnlyFilter = !current.borderOnlyFilter)
  }

  fun toggleGhostImage() {
    val current = _activeGame.value ?: return
    _activeGame.value = current.copy(ghostImageVisible = !current.ghostImageVisible)
  }

  fun toggleGridLines() {
    val current = _activeGame.value ?: return
    _activeGame.value = current.copy(showGridLines = !current.showGridLines)
  }

  fun selectPiece(pieceId: Int?) {
    val current = _activeGame.value ?: return
    _activeGame.value = current.copy(selectedPieceId = pieceId)
  }

  fun trySnapPiece(
    pieceId: Int,
    dropX: Float,
    dropY: Float,
    cellWidth: Float,
    cellHeight: Float,
    snapThreshold: Float = 55f
  ): Boolean {
    val current = _activeGame.value ?: return false
    val piece = current.pieces.find { it.id == pieceId } ?: return false
    if (piece.isPlaced) return true

    val targetX = piece.col * cellWidth
    val targetY = piece.row * cellHeight

    val distance = hypot(dropX - targetX, dropY - targetY)

    if (distance <= snapThreshold) {
      snapPiece(pieceId)
      return true
    } else {
      val updatedPieces = current.pieces.map {
        if (it.id == pieceId) {
          it.copy(isOnBoard = true, boardX = dropX, boardY = dropY)
        } else it
      }
      _activeGame.value = current.copy(pieces = updatedPieces)
      return false
    }
  }

  fun snapPiece(pieceId: Int) {
    val current = _activeGame.value ?: return
    val piece = current.pieces.find { it.id == pieceId } ?: return
    if (piece.isPlaced) return

    vibrateSnap()
    soundManager.playSnapSound()

    val updatedPieces = current.pieces.map {
      if (it.id == pieceId) {
        it.copy(isPlaced = true, isOnBoard = false)
      } else it
    }

    val newPlacedCount = updatedPieces.count { it.isPlaced }
    val isFinished = newPlacedCount == current.totalCount

    val updatedState = current.copy(
      pieces = updatedPieces,
      placedCount = newPlacedCount,
      isCompleted = isFinished,
      recentlySnappedPieceId = pieceId,
      selectedPieceId = null
    )
    _activeGame.value = updatedState

    if (isFinished) {
      vibrateVictory()
      soundManager.playCompletionSound()
      viewModelScope.launch {
        repository.recordCompletion(
          puzzleId = current.puzzleItem.id,
          timeSeconds = current.elapsedSeconds,
          pieceCount = current.totalCount
        )
      }
    } else {
      // Persist progress to Room
      persistCurrentProgress(updatedState)
    }

    viewModelScope.launch {
      delay(800L)
      _activeGame.value?.let { state ->
        if (state.recentlySnappedPieceId == pieceId) {
          _activeGame.value = state.copy(recentlySnappedPieceId = null)
        }
      }
    }
  }

  fun toggleSound(): Boolean = soundManager.toggleSound()

  override fun onCleared() {
    super.onCleared()
    soundManager.release()
  }

  fun returnPieceToTray(pieceId: Int) {
    val current = _activeGame.value ?: return
    val updatedPieces = current.pieces.map {
      if (it.id == pieceId) it.copy(isOnBoard = false) else it
    }
    _activeGame.value = current.copy(pieces = updatedPieces, selectedPieceId = null)
  }

  fun useHint() {
    val current = _activeGame.value ?: return
    if (current.remainingHints <= 0 || current.isCompleted) return

    val candidate = current.pieces.firstOrNull { it.id == current.selectedPieceId && !it.isPlaced }
      ?: current.pieces.firstOrNull { !it.isPlaced && it.isBorder }
      ?: current.pieces.firstOrNull { !it.isPlaced }
      ?: return

    val nextHints = current.remainingHints - 1
    _activeGame.value = current.copy(remainingHints = nextHints)

    snapPiece(candidate.id)
  }

  fun backToGallery() {
    _activeGame.value?.let { current ->
      if (!current.isCompleted) {
        persistCurrentProgress(current)
      }
    }
    timerJob?.cancel()
    _screenState.value = ScreenState.GALLERY
  }

  fun restartCurrentPuzzle() {
    val current = _activeGame.value ?: return
    viewModelScope.launch {
      repository.deleteProgress(current.puzzleItem.id)
      startPuzzle(current.puzzleItem, current.difficulty, resumeProgress = null)
    }
  }

  fun createCustomPhotoPuzzle(title: String, uriString: String) {
    val id = repository.addCustomPuzzle(title, uriString)
    val newItem = repository.catalog.value.find { it.id == id }
    if (newItem != null) {
      requestStartPuzzle(newItem)
    }
  }

  private fun persistCurrentProgress(state: ActiveGameState) {
    if (state.isCompleted) return
    viewModelScope.launch {
      val placedIds = state.pieces.filter { it.isPlaced }.map { it.id }.toSet()
      repository.saveProgress(
        puzzleId = state.puzzleItem.id,
        difficulty = state.difficulty,
        placedPieceIds = placedIds,
        elapsedSeconds = state.elapsedSeconds,
        remainingHints = state.remainingHints,
        isCompleted = false
      )
    }
  }

  private fun vibrateSnap() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createOneShot(35L, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(35L)
      }
    } catch (_: Exception) {}
  }

  private fun vibrateVictory() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val pattern = longArrayOf(0, 100, 80, 150, 100, 250)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(400L)
      }
    } catch (_: Exception) {}
  }
}
