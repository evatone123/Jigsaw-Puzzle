package com.example.engine

import com.example.data.model.PieceEdgeType
import com.example.data.model.PuzzlePiece
import kotlin.random.Random

object JigsawGenerator {

  /**
   * Generates a grid of interlocking puzzle pieces for [rows] x [cols].
   * Adjacent pieces share complementary edges (TAB_OUT <-> TAB_IN).
   * Outer perimeter edges are FLAT.
   */
  fun generatePieces(rows: Int, cols: Int, seed: Long = 42L): List<PuzzlePiece> {
    val random = Random(seed)

    // horizontalEdges[r][c] is the edge between (r, c) and (r+1, c)
    // true = tab points DOWN (TAB_OUT for r, TAB_IN for r+1)
    val horizontalEdges = Array(rows - 1) {
      BooleanArray(cols) { random.nextBoolean() }
    }

    // verticalEdges[r][c] is the edge between (r, c) and (r, c+1)
    // true = tab points RIGHT (TAB_OUT for c, TAB_IN for c+1)
    val verticalEdges = Array(rows) {
      BooleanArray(cols - 1) { random.nextBoolean() }
    }

    val pieces = mutableListOf<PuzzlePiece>()
    var idCounter = 0

    for (r in 0 until rows) {
      for (c in 0 until cols) {
        val top = if (r == 0) {
          PieceEdgeType.FLAT
        } else {
          if (horizontalEdges[r - 1][c]) PieceEdgeType.TAB_IN else PieceEdgeType.TAB_OUT
        }

        val bottom = if (r == rows - 1) {
          PieceEdgeType.FLAT
        } else {
          if (horizontalEdges[r][c]) PieceEdgeType.TAB_OUT else PieceEdgeType.TAB_IN
        }

        val left = if (c == 0) {
          PieceEdgeType.FLAT
        } else {
          if (verticalEdges[r][c - 1]) PieceEdgeType.TAB_IN else PieceEdgeType.TAB_OUT
        }

        val right = if (c == cols - 1) {
          PieceEdgeType.FLAT
        } else {
          if (verticalEdges[r][c]) PieceEdgeType.TAB_OUT else PieceEdgeType.TAB_IN
        }

        pieces.add(
          PuzzlePiece(
            id = idCounter++,
            row = r,
            col = c,
            topEdge = top,
            rightEdge = right,
            bottomEdge = bottom,
            leftEdge = left
          )
        )
      }
    }

    return pieces
  }
}
