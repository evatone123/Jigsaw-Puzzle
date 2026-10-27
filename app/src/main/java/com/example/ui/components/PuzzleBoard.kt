package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.data.model.PuzzlePiece
import com.example.engine.JigsawPathBuilder
import kotlin.math.hypot

@Composable
fun PuzzleBoard(
  bitmap: ImageBitmap,
  pieces: List<PuzzlePiece>,
  rows: Int,
  cols: Int,
  ghostVisible: Boolean,
  ghostOpacity: Float,
  showGridLines: Boolean,
  selectedPieceId: Int?,
  recentlySnappedId: Int?,
  onBoardTapAt: (touchX: Float, touchY: Float, cellW: Float, cellH: Float) -> Unit,
  onBoardPositioned: (width: Float, height: Float) -> Unit,
  modifier: Modifier = Modifier
) {
  var boardSize by remember { mutableStateOf(Size.Zero) }

  // Pulsing animation for target indicator
  val pulseAnim = remember { Animatable(0.3f) }
  LaunchedEffect(selectedPieceId) {
    if (selectedPieceId != null) {
      pulseAnim.animateTo(
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
          animation = tween(600, easing = LinearEasing),
          repeatMode = RepeatMode.Reverse
        )
      )
    } else {
      pulseAnim.snapTo(0.3f)
    }
  }

  // Snapped celebration flash animation
  val snapFlashAnim = remember { Animatable(0f) }
  LaunchedEffect(recentlySnappedId) {
    if (recentlySnappedId != null) {
      snapFlashAnim.snapTo(1f)
      snapFlashAnim.animateTo(0f, animationSpec = tween(500))
    }
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .aspectRatio(4f / 3f)
      .shadow(8.dp, RoundedCornerShape(16.dp))
      .clip(RoundedCornerShape(16.dp))
      .background(Color(0xFF1E222D)) // Slate puzzle table mat
      .border(2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
      .testTag("puzzle_board")
      .onGloballyPositioned { coordinates ->
        val w = coordinates.size.width.toFloat()
        val h = coordinates.size.height.toFloat()
        boardSize = Size(w, h)
        onBoardPositioned(w, h)
      }
      .pointerInput(selectedPieceId, pieces) {
        detectTapGestures { offset ->
          val cellW = size.width / cols.toFloat()
          val cellH = size.height / rows.toFloat()
          onBoardTapAt(offset.x, offset.y, cellW, cellH)
        }
      }
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      if (w <= 0f || h <= 0f) return@Canvas

      val cellW = w / cols
      val cellH = h / rows

      // 1. Ghost reference image underlay (Easybrain staple)
      if (ghostVisible) {
        drawImage(
          image = bitmap,
          srcOffset = IntOffset.Zero,
          srcSize = IntSize(bitmap.width, bitmap.height),
          dstOffset = IntOffset.Zero,
          dstSize = IntSize(w.toInt(), h.toInt()),
          alpha = ghostOpacity
        )
      }

      // 2. Subtle grid guides
      if (showGridLines) {
        val gridColor = Color.White.copy(alpha = 0.08f)
        val stroke = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
        for (r in 1 until rows) {
          drawLine(gridColor, Offset(0f, r * cellH), Offset(w, r * cellH), strokeWidth = 1f)
        }
        for (c in 1 until cols) {
          drawLine(gridColor, Offset(c * cellW, 0f), Offset(c * cellW, h), strokeWidth = 1f)
        }
      }

      // 3. Highlight target slot for selected piece (if selected)
      if (selectedPieceId != null) {
        val targetPiece = pieces.find { it.id == selectedPieceId && !it.isPlaced }
        if (targetPiece != null) {
          val tx = targetPiece.col * cellW
          val ty = targetPiece.row * cellH
          drawRect(
            color = Color(0xFFFFD54F).copy(alpha = pulseAnim.value * 0.25f),
            topLeft = Offset(tx, ty),
            size = Size(cellW, cellH)
          )
          drawRect(
            color = Color(0xFFFFD54F).copy(alpha = pulseAnim.value * 0.8f),
            topLeft = Offset(tx + 2f, ty + 2f),
            size = Size(cellW - 4f, cellH - 4f),
            style = Stroke(width = 2f)
          )
        }
      }

      // 4. Draw placed pieces with interlocking edges
      for (piece in pieces) {
        if (!piece.isPlaced) continue

        val pieceOffsetX = piece.col * cellW
        val pieceOffsetY = piece.row * cellH

        val path = JigsawPathBuilder.buildPiecePath(
          piece = piece,
          width = cellW,
          height = cellH,
          offsetX = pieceOffsetX,
          offsetY = pieceOffsetY
        )

        // Clip and draw image slice
        clipPath(path) {
          drawImage(
            image = bitmap,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(bitmap.width, bitmap.height),
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(w.toInt(), h.toInt())
          )
        }

        // Seam outline between interlocking pieces
        drawPath(
          path = path,
          color = Color.Black.copy(alpha = 0.35f),
          style = Stroke(width = 1f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
          path = path,
          color = Color.White.copy(alpha = 0.15f),
          style = Stroke(width = 0.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
      }

      // 5. Celebration burst flash on recently snapped piece
      if (recentlySnappedId != null && snapFlashAnim.value > 0f) {
        val snapped = pieces.find { it.id == recentlySnappedId }
        if (snapped != null) {
          val sx = snapped.col * cellW + cellW / 2f
          val sy = snapped.row * cellH + cellH / 2f
          val radius = cellW * 0.9f * (1f + (1f - snapFlashAnim.value) * 0.5f)
          drawCircle(
            color = Color(0xFFFFE082).copy(alpha = snapFlashAnim.value * 0.7f),
            radius = radius,
            center = Offset(sx, sy)
          )
          drawCircle(
            color = Color.White.copy(alpha = snapFlashAnim.value * 0.9f),
            radius = radius * 0.5f,
            center = Offset(sx, sy)
          )
        }
      }
    }
  }
}
