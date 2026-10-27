package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.data.model.PieceEdgeType
import com.example.data.model.PuzzlePiece
import com.example.engine.JigsawPathBuilder

@Composable
fun JigsawPieceThumbnail(
  piece: PuzzlePiece,
  bitmap: ImageBitmap,
  totalRows: Int,
  totalCols: Int,
  isSelected: Boolean,
  sizeDp: Dp = 80.dp,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
  val containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

  Box(
    modifier = modifier
      .size(sizeDp)
      .clip(RoundedCornerShape(12.dp))
      .background(containerColor)
      .border(if (isSelected) 2.dp else 0.dp, borderColor, RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .padding(6.dp)
      .testTag("piece_thumbnail_${piece.id}"),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(sizeDp - 12.dp)) {
      val canvasW = size.width
      val canvasH = size.height

      // Master board dimensions
      val masterW = bitmap.width.toFloat()
      val masterH = bitmap.height.toFloat()
      val cellW = masterW / totalCols
      val cellH = masterH / totalRows

      // Scale to fit piece in canvas (accounting for 25% tab protrusions on each side)
      val scale = (canvasW / (cellW * 1.5f)).coerceAtMost(canvasH / (cellH * 1.5f))

      val piecePath = JigsawPathBuilder.buildPiecePath(
        piece = piece,
        width = cellW,
        height = cellH,
        offsetX = 0f,
        offsetY = 0f
      )

      scale(scale, pivot = Offset.Zero) {
        // Center the piece inside the canvas
        val centeringX = (canvasW / scale - cellW) / 2f
        val centeringY = (canvasH / scale - cellH) / 2f

        translate(centeringX, centeringY) {
          // Drop shadow effect
          translate(2f, 3f) {
            drawPath(
              path = piecePath,
              color = Color.Black.copy(alpha = 0.25f)
            )
          }

          // Clip to interlocking jigsaw piece shape and draw slice of master bitmap
          clipPath(piecePath) {
            drawImage(
              image = bitmap,
              srcOffset = IntOffset(0, 0),
              srcSize = IntSize(bitmap.width, bitmap.height),
              dstOffset = IntOffset(
                (-piece.col * cellW).toInt(),
                (-piece.row * cellH).toInt()
              ),
              dstSize = IntSize(masterW.toInt(), masterH.toInt())
            )
          }

          // Physical puzzle piece edge highlight and border stroke
          drawPath(
            path = piecePath,
            color = Color.White.copy(alpha = 0.45f),
            style = Stroke(
              width = 1.5f / scale,
              cap = StrokeCap.Round,
              join = StrokeJoin.Round
            )
          )
          drawPath(
            path = piecePath,
            color = Color.Black.copy(alpha = 0.35f),
            style = Stroke(
              width = 0.8f / scale,
              cap = StrokeCap.Round,
              join = StrokeJoin.Round
            )
          )
        }
      }
    }
  }
}
