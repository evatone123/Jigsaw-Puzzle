package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PuzzleBitmapHelper {

  /**
   * Loads a scaled [ImageBitmap] from a drawable resource or content URI.
   */
  suspend fun loadPuzzleBitmap(
    context: Context,
    drawableResId: Int?,
    customUri: String?,
    targetWidth: Int = 1000,
    targetHeight: Int = 750
  ): ImageBitmap = withContext(Dispatchers.IO) {
    var rawBitmap: Bitmap? = null

    try {
      if (customUri != null) {
        val uri = Uri.parse(customUri)
        context.contentResolver.openInputStream(uri)?.use { stream ->
          rawBitmap = BitmapFactory.decodeStream(stream)
        }
      } else if (drawableResId != null) {
        rawBitmap = BitmapFactory.decodeResource(context.resources, drawableResId)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }

    val finalBitmap = rawBitmap?.let { src ->
      // Scale while preserving aspect or fitting target
      Bitmap.createScaledBitmap(src, targetWidth, targetHeight, true)
    } ?: createFallbackBitmap(targetWidth, targetHeight)

    finalBitmap.asImageBitmap()
  }

  /**
   * Creates a vibrant fallback graphic bitmap if resource is missing.
   */
  private fun createFallbackBitmap(width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Vibrant sky gradient
    val colors = intArrayOf(
      android.graphics.Color.parseColor("#4F46E5"),
      android.graphics.Color.parseColor("#06B6D4"),
      android.graphics.Color.parseColor("#10B981")
    )
    val shader = android.graphics.LinearGradient(
      0f, 0f, width.toFloat(), height.toFloat(),
      colors, null, android.graphics.Shader.TileMode.CLAMP
    )
    paint.shader = shader
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

    // Sun
    paint.shader = null
    paint.color = android.graphics.Color.parseColor("#FBBF24")
    canvas.drawCircle(width * 0.5f, height * 0.35f, height * 0.2f, paint)

    return bitmap
  }
}
