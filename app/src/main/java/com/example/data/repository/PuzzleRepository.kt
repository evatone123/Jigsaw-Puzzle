package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.R
import com.example.data.local.PuzzleDatabase
import com.example.data.local.PuzzleProgressDao
import com.example.data.local.PuzzleProgressEntity
import com.example.data.model.PuzzleCatalogItem
import com.example.data.model.PuzzleDifficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PuzzleRepository(
  private val context: Context,
  private val progressDao: PuzzleProgressDao = PuzzleDatabase.getDatabase(context).puzzleProgressDao()
) {

  private val prefs: SharedPreferences =
    context.getSharedPreferences("jigsaw_puzzle_prefs", Context.MODE_PRIVATE)

  private val _catalog = MutableStateFlow<List<PuzzleCatalogItem>>(emptyList())
  val catalog: StateFlow<List<PuzzleCatalogItem>> = _catalog.asStateFlow()

  val allSavedProgress: Flow<List<PuzzleProgressEntity>> = progressDao.getAllProgress()

  init {
    loadCatalog()
  }

  fun loadCatalog() {
    val defaultList = listOf(
      PuzzleCatalogItem(
        id = "puzzle_daily",
        title = "Sunset Over High Peaks",
        category = "Nature",
        drawableResId = R.drawable.puzzle_sunset_mountains,
        isDaily = true,
        completed = prefs.getBoolean("completed_puzzle_daily", false),
        bestTimeSeconds = getBestTime("puzzle_daily"),
        lastPieceCount = prefs.getInt("pieces_puzzle_daily", 36)
      ),
      PuzzleCatalogItem(
        id = "puzzle_space",
        title = "Cosmic Nebula & Rings",
        category = "Space",
        drawableResId = R.drawable.puzzle_cosmic_nebula,
        completed = prefs.getBoolean("completed_puzzle_space", false),
        bestTimeSeconds = getBestTime("puzzle_space"),
        lastPieceCount = prefs.getInt("pieces_puzzle_space", 36)
      ),
      PuzzleCatalogItem(
        id = "puzzle_autumn",
        title = "Golden Autumn Forest",
        category = "Nature",
        drawableResId = R.drawable.puzzle_autumn_forest,
        completed = prefs.getBoolean("completed_puzzle_autumn", false),
        bestTimeSeconds = getBestTime("puzzle_autumn"),
        lastPieceCount = prefs.getInt("pieces_puzzle_autumn", 36)
      ),
      PuzzleCatalogItem(
        id = "puzzle_aurora",
        title = "Nordic Aurora Borealis",
        category = "Nature",
        drawableResId = R.drawable.puzzle_aurora_borealis,
        completed = prefs.getBoolean("completed_puzzle_aurora", false),
        bestTimeSeconds = getBestTime("puzzle_aurora"),
        lastPieceCount = prefs.getInt("pieces_puzzle_aurora", 36)
      ),
      PuzzleCatalogItem(
        id = "puzzle_travel",
        title = "Tropical Lagoon Paradise",
        category = "Travel",
        drawableResId = R.drawable.puzzle_tropical_paradise,
        completed = prefs.getBoolean("completed_puzzle_travel", false),
        bestTimeSeconds = getBestTime("puzzle_travel"),
        lastPieceCount = prefs.getInt("pieces_puzzle_travel", 36)
      ),
      PuzzleCatalogItem(
        id = "puzzle_city",
        title = "Neon Cyberpunk Skyline",
        category = "City",
        drawableResId = R.drawable.puzzle_neon_cyberpunk,
        completed = prefs.getBoolean("completed_puzzle_city", false),
        bestTimeSeconds = getBestTime("puzzle_city"),
        lastPieceCount = prefs.getInt("pieces_puzzle_city", 36)
      ),
      PuzzleCatalogItem(
        id = "puzzle_mandala",
        title = "Sacred Geometry Mandala",
        category = "Art",
        drawableResId = R.drawable.puzzle_mandala_art,
        completed = prefs.getBoolean("completed_puzzle_mandala", false),
        bestTimeSeconds = getBestTime("puzzle_mandala"),
        lastPieceCount = prefs.getInt("pieces_puzzle_mandala", 36)
      ),
      PuzzleCatalogItem(
        id = "puzzle_balloons",
        title = "Sunrise Hot Air Balloons",
        category = "Travel",
        drawableResId = R.drawable.puzzle_hot_air_balloons,
        completed = prefs.getBoolean("completed_puzzle_balloons", false),
        bestTimeSeconds = getBestTime("puzzle_balloons"),
        lastPieceCount = prefs.getInt("pieces_puzzle_balloons", 36)
      )
    )

    // Load any custom user photo puzzles from prefs
    val customIdsString = prefs.getString("custom_puzzle_ids", "") ?: ""
    val customList = if (customIdsString.isNotEmpty()) {
      customIdsString.split(",").filter { it.isNotEmpty() }.mapNotNull { customId ->
        val title = prefs.getString("custom_title_$customId", "My Custom Puzzle") ?: "My Custom Puzzle"
        val uri = prefs.getString("custom_uri_$customId", null)
        if (uri != null) {
          PuzzleCatalogItem(
            id = customId,
            title = title,
            category = "Custom",
            customUri = uri,
            completed = prefs.getBoolean("completed_$customId", false),
            bestTimeSeconds = getBestTime(customId),
            lastPieceCount = prefs.getInt("pieces_$customId", 36)
          )
        } else null
      }
    } else emptyList()

    _catalog.value = defaultList + customList
  }

  fun addCustomPuzzle(title: String, uriString: String): String {
    val id = "custom_${System.currentTimeMillis()}"
    val currentIds = prefs.getString("custom_puzzle_ids", "") ?: ""
    val updatedIds = if (currentIds.isEmpty()) id else "$currentIds,$id"
    prefs.edit()
      .putString("custom_puzzle_ids", updatedIds)
      .putString("custom_title_$id", title)
      .putString("custom_uri_$id", uriString)
      .apply()
    loadCatalog()
    return id
  }

  suspend fun getProgress(puzzleId: String): PuzzleProgressEntity? {
    return progressDao.getProgress(puzzleId)
  }

  fun getProgressFlow(puzzleId: String): Flow<PuzzleProgressEntity?> {
    return progressDao.getProgressFlow(puzzleId)
  }

  suspend fun saveProgress(
    puzzleId: String,
    difficulty: PuzzleDifficulty,
    placedPieceIds: Set<Int>,
    elapsedSeconds: Long,
    remainingHints: Int,
    isCompleted: Boolean
  ) {
    val entity = PuzzleProgressEntity(
      puzzleId = puzzleId,
      difficultyName = difficulty.name,
      pieceCount = difficulty.pieceCount,
      placedPieceIds = placedPieceIds.joinToString(","),
      elapsedSeconds = elapsedSeconds,
      remainingHints = remainingHints,
      isCompleted = isCompleted,
      lastUpdatedTimestamp = System.currentTimeMillis()
    )
    progressDao.saveProgress(entity)
  }

  suspend fun deleteProgress(puzzleId: String) {
    progressDao.deleteProgress(puzzleId)
  }

  suspend fun recordCompletion(puzzleId: String, timeSeconds: Long, pieceCount: Int) {
    val currentBest = getBestTime(puzzleId)
    val newBest = if (currentBest == null || timeSeconds < currentBest) timeSeconds else currentBest

    prefs.edit()
      .putBoolean("completed_$puzzleId", true)
      .putLong("best_time_$puzzleId", newBest)
      .putInt("pieces_$puzzleId", pieceCount)
      .apply()

    // Clear unfinished draft progress upon completion
    progressDao.deleteProgress(puzzleId)

    loadCatalog()
  }

  private fun getBestTime(puzzleId: String): Long? {
    val time = prefs.getLong("best_time_$puzzleId", -1L)
    return if (time >= 0L) time else null
  }
}
