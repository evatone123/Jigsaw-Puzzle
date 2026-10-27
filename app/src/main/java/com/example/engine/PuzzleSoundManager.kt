package com.example.engine

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages subtle audio sound effects using the Android [MediaPlayer] API.
 * Triggers satisfying tactile clicks when puzzle pieces interlock.
 */
class PuzzleSoundManager(private val context: Context) {

  private val prefs: SharedPreferences =
    context.getSharedPreferences("puzzle_audio_prefs", Context.MODE_PRIVATE)

  private val _soundEnabled = MutableStateFlow(prefs.getBoolean("sound_fx_enabled", true))
  val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

  private val activePlayers = mutableListOf<MediaPlayer>()

  fun setSoundEnabled(enabled: Boolean) {
    _soundEnabled.value = enabled
    prefs.edit().putBoolean("sound_fx_enabled", enabled).apply()
  }

  fun toggleSound(): Boolean {
    val next = !_soundEnabled.value
    setSoundEnabled(next)
    return next
  }

  /**
   * Plays a subtle, tactile click sound using the MediaPlayer API when two puzzle pieces correctly interlock.
   */
  fun playSnapSound() {
    if (!_soundEnabled.value) return

    try {
      val mediaPlayer = MediaPlayer.create(context, R.raw.piece_snap) ?: return

      mediaPlayer.setAudioAttributes(
        AudioAttributes.Builder()
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .setUsage(AudioAttributes.USAGE_GAME)
          .build()
      )

      // Set subtle volume (50% left/right) for pleasant feedback without distortion
      mediaPlayer.setVolume(0.55f, 0.55f)

      synchronized(activePlayers) {
        activePlayers.add(mediaPlayer)
      }

      mediaPlayer.setOnCompletionListener { mp ->
        cleanupPlayer(mp)
      }

      mediaPlayer.setOnErrorListener { mp, _, _ ->
        cleanupPlayer(mp)
        true
      }

      mediaPlayer.start()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  /**
   * Plays a celebratory victory chime when the puzzle is completed.
   */
  fun playCompletionSound() {
    if (!_soundEnabled.value) return

    try {
      val mediaPlayer = MediaPlayer.create(context, R.raw.puzzle_complete) ?: return

      mediaPlayer.setAudioAttributes(
        AudioAttributes.Builder()
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .setUsage(AudioAttributes.USAGE_GAME)
          .build()
      )

      mediaPlayer.setVolume(0.65f, 0.65f)

      synchronized(activePlayers) {
        activePlayers.add(mediaPlayer)
      }

      mediaPlayer.setOnCompletionListener { mp ->
        cleanupPlayer(mp)
      }

      mediaPlayer.setOnErrorListener { mp, _, _ ->
        cleanupPlayer(mp)
        true
      }

      mediaPlayer.start()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun cleanupPlayer(mp: MediaPlayer) {
    try {
      synchronized(activePlayers) {
        activePlayers.remove(mp)
      }
      mp.stop()
      mp.reset()
      mp.release()
    } catch (_: Exception) {}
  }

  /**
   * Releases any playing MediaPlayer instances when cleaning up.
   */
  fun release() {
    synchronized(activePlayers) {
      for (mp in activePlayers) {
        try {
          mp.stop()
          mp.reset()
          mp.release()
        } catch (_: Exception) {}
      }
      activePlayers.clear()
    }
  }
}
