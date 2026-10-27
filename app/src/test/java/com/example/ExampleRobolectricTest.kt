package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PuzzleDatabase
import com.example.data.model.PuzzleDifficulty
import com.example.data.repository.PuzzleRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Jigsaw Puzzles", appName)
  }

  @Test
  fun `puzzle repository loads default catalog`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = PuzzleRepository(context)
    val catalog = repo.catalog.value
    assertTrue("Catalog should contain starter puzzles", catalog.isNotEmpty())
    assertTrue("Should have daily puzzle", catalog.any { it.isDaily })
  }

  @Test
  fun `room database saves and resumes puzzle progress`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = PuzzleRepository(context)

    val puzzleId = "test_puzzle_1"
    val placedPieceIds = setOf(0, 1, 3, 5)
    val elapsedSeconds = 42L
    val remainingHints = 2

    // Save progress
    repo.saveProgress(
      puzzleId = puzzleId,
      difficulty = PuzzleDifficulty.MEDIUM_36,
      placedPieceIds = placedPieceIds,
      elapsedSeconds = elapsedSeconds,
      remainingHints = remainingHints,
      isCompleted = false
    )

    // Retrieve progress
    val retrieved = repo.getProgress(puzzleId)
    assertNotNull(retrieved)
    assertEquals(puzzleId, retrieved!!.puzzleId)
    assertEquals("MEDIUM_36", retrieved.difficultyName)
    assertEquals(36, retrieved.pieceCount)
    assertEquals("0,1,3,5", retrieved.placedPieceIds)
    assertEquals(42L, retrieved.elapsedSeconds)
    assertEquals(2, retrieved.remainingHints)

    // Delete progress on completion
    repo.recordCompletion(puzzleId, timeSeconds = 45L, pieceCount = 36)
    val afterCompletion = repo.getProgress(puzzleId)
    assertNull(afterCompletion)
  }

  @Test
  fun `sound manager toggles and plays snap without crash`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val soundManager = com.example.engine.PuzzleSoundManager(context)

    assertTrue("Sound should be enabled by default", soundManager.soundEnabled.value)
    val toggled = soundManager.toggleSound()
    assertEquals(false, toggled)
    assertEquals(false, soundManager.soundEnabled.value)

    soundManager.setSoundEnabled(true)
    assertEquals(true, soundManager.soundEnabled.value)

    // Call snap sound and complete sound safely
    soundManager.playSnapSound()
    soundManager.playCompletionSound()
    soundManager.release()
  }
}
