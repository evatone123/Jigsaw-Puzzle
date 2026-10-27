package com.example.engine

import androidx.compose.ui.graphics.Path
import com.example.data.model.PieceEdgeType
import com.example.data.model.PuzzlePiece

object JigsawPathBuilder {

  /**
   * Builds an interlocking jigsaw piece [Path] for a piece of nominal [width] and [height].
   * The nominal piece is situated from (offsetX, offsetY) to (offsetX + width, offsetY + height).
   * Tabs extend outwards or inwards beyond this rectangle by roughly 20% of the piece dimensions.
   */
  fun buildPiecePath(
    piece: PuzzlePiece,
    width: Float,
    height: Float,
    offsetX: Float = 0f,
    offsetY: Float = 0f
  ): Path {
    val path = Path()

    val x0 = offsetX
    val y0 = offsetY
    val x1 = offsetX + width
    val y1 = offsetY + height

    path.moveTo(x0, y0)

    // 1. TOP EDGE (from (x0, y0) to (x1, y0))
    when (piece.topEdge) {
      PieceEdgeType.FLAT -> path.lineTo(x1, y0)
      PieceEdgeType.TAB_OUT -> drawHorizontalTab(path, x0, y0, width, sign = -1f) // -1 is outwards (up)
      PieceEdgeType.TAB_IN -> drawHorizontalTab(path, x0, y0, width, sign = 1f)  // +1 is inwards (down)
    }

    // 2. RIGHT EDGE (from (x1, y0) to (x1, y1))
    when (piece.rightEdge) {
      PieceEdgeType.FLAT -> path.lineTo(x1, y1)
      PieceEdgeType.TAB_OUT -> drawVerticalTab(path, x1, y0, height, sign = 1f)  // +1 is outwards (right)
      PieceEdgeType.TAB_IN -> drawVerticalTab(path, x1, y0, height, sign = -1f) // -1 is inwards (left)
    }

    // 3. BOTTOM EDGE (from (x1, y1) to (x0, y1))
    when (piece.bottomEdge) {
      PieceEdgeType.FLAT -> path.lineTo(x0, y1)
      PieceEdgeType.TAB_OUT -> drawHorizontalTabReverse(path, x1, y1, width, sign = 1f)  // +1 is outwards (down)
      PieceEdgeType.TAB_IN -> drawHorizontalTabReverse(path, x1, y1, width, sign = -1f) // -1 is inwards (up)
    }

    // 4. LEFT EDGE (from (x0, y1) to (x0, y0))
    when (piece.leftEdge) {
      PieceEdgeType.FLAT -> path.lineTo(x0, y0)
      PieceEdgeType.TAB_OUT -> drawVerticalTabReverse(path, x0, y1, height, sign = -1f) // -1 is outwards (left)
      PieceEdgeType.TAB_IN -> drawVerticalTabReverse(path, x0, y1, height, sign = 1f)  // +1 is inwards (right)
    }

    path.close()
    return path
  }

  // Horizontal top edge tab: from (startX, y) to (startX + length, y)
  // sign: -1 for up, +1 for down
  private fun drawHorizontalTab(path: Path, startX: Float, y: Float, length: Float, sign: Float) {
    val depth = length * 0.22f * sign
    val p0 = startX
    val p1 = startX + length * 0.35f
    val p2 = startX + length * 0.38f
    val p3 = startX + length * 0.42f
    val p4 = startX + length * 0.50f
    val p5 = startX + length * 0.58f
    val p6 = startX + length * 0.62f
    val p7 = startX + length * 0.65f
    val p8 = startX + length

    path.lineTo(p1, y)
    // Neck base
    path.cubicTo(
      p2, y - depth * 0.05f,
      p3 - length * 0.05f, y + depth * 0.65f,
      p4 - length * 0.12f, y + depth * 0.95f
    )
    // Head bulb
    path.cubicTo(
      p4 - length * 0.05f, y + depth * 1.15f,
      p4 + length * 0.05f, y + depth * 1.15f,
      p4 + length * 0.12f, y + depth * 0.95f
    )
    // Return neck
    path.cubicTo(
      p5 + length * 0.05f, y + depth * 0.65f,
      p6, y - depth * 0.05f,
      p7, y
    )
    path.lineTo(p8, y)
  }

  // Horizontal bottom edge tab (traversing right to left): from (startX, y) to (startX - length, y)
  // sign: +1 for down (outwards), -1 for up (inwards)
  private fun drawHorizontalTabReverse(path: Path, startX: Float, y: Float, length: Float, sign: Float) {
    val depth = length * 0.22f * sign
    val p0 = startX
    val p1 = startX - length * 0.35f
    val p2 = startX - length * 0.38f
    val p3 = startX - length * 0.42f
    val p4 = startX - length * 0.50f
    val p5 = startX - length * 0.58f
    val p6 = startX - length * 0.62f
    val p7 = startX - length * 0.65f
    val p8 = startX - length

    path.lineTo(p1, y)
    path.cubicTo(
      p2, y + depth * 0.05f,
      p3 + length * 0.05f, y + depth * 0.65f,
      p4 + length * 0.12f, y + depth * 0.95f
    )
    path.cubicTo(
      p4 + length * 0.05f, y + depth * 1.15f,
      p4 - length * 0.05f, y + depth * 1.15f,
      p4 - length * 0.12f, y + depth * 0.95f
    )
    path.cubicTo(
      p5 - length * 0.05f, y + depth * 0.65f,
      p6, y + depth * 0.05f,
      p7, y
    )
    path.lineTo(p8, y)
  }

  // Vertical right edge tab: from (x, startY) to (x, startY + length)
  // sign: +1 for right (outwards), -1 for left (inwards)
  private fun drawVerticalTab(path: Path, x: Float, startY: Float, length: Float, sign: Float) {
    val depth = length * 0.22f * sign
    val p1 = startY + length * 0.35f
    val p2 = startY + length * 0.38f
    val p3 = startY + length * 0.42f
    val p4 = startY + length * 0.50f
    val p5 = startY + length * 0.58f
    val p6 = startY + length * 0.62f
    val p7 = startY + length * 0.65f
    val p8 = startY + length

    path.lineTo(x, p1)
    path.cubicTo(
      x + depth * 0.05f, p2,
      x + depth * 0.65f, p3 - length * 0.05f,
      x + depth * 0.95f, p4 - length * 0.12f
    )
    path.cubicTo(
      x + depth * 1.15f, p4 - length * 0.05f,
      x + depth * 1.15f, p4 + length * 0.05f,
      x + depth * 0.95f, p4 + length * 0.12f
    )
    path.cubicTo(
      x + depth * 0.65f, p5 + length * 0.05f,
      x + depth * 0.05f, p6,
      x, p7
    )
    path.lineTo(x, p8)
  }

  // Vertical left edge tab (traversing bottom to top): from (x, startY) to (x, startY - length)
  // sign: -1 for left (outwards), +1 for right (inwards)
  private fun drawVerticalTabReverse(path: Path, x: Float, startY: Float, length: Float, sign: Float) {
    val depth = length * 0.22f * sign
    val p1 = startY - length * 0.35f
    val p2 = startY - length * 0.38f
    val p3 = startY - length * 0.42f
    val p4 = startY - length * 0.50f
    val p5 = startY - length * 0.58f
    val p6 = startY - length * 0.62f
    val p7 = startY - length * 0.65f
    val p8 = startY - length

    path.lineTo(x, p1)
    path.cubicTo(
      x - depth * 0.05f, p2,
      x + depth * 0.65f, p3 + length * 0.05f,
      x + depth * 0.95f, p4 + length * 0.12f
    )
    path.cubicTo(
      x + depth * 1.15f, p4 + length * 0.05f,
      x + depth * 1.15f, p4 - length * 0.05f,
      x + depth * 0.95f, p4 - length * 0.12f
    )
    path.cubicTo(
      x + depth * 0.65f, p5 - length * 0.05f,
      x - depth * 0.05f, p6,
      x, p7
    )
    path.lineTo(x, p8)
  }
}
