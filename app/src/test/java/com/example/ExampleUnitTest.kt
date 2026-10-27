package com.example

import com.example.data.model.PieceEdgeType
import com.example.engine.JigsawGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun jigsawGenerator_producesCorrectPieceCount() {
    val pieces16 = JigsawGenerator.generatePieces(4, 4)
    assertEquals(16, pieces16.size)

    val pieces36 = JigsawGenerator.generatePieces(6, 6)
    assertEquals(36, pieces36.size)
  }

  @Test
  fun jigsawGenerator_outerPerimeterEdgesAreFlat() {
    val rows = 4
    val cols = 4
    val pieces = JigsawGenerator.generatePieces(rows, cols)

    for (piece in pieces) {
      if (piece.row == 0) {
        assertEquals(PieceEdgeType.FLAT, piece.topEdge)
      } else {
        assertNotEquals(PieceEdgeType.FLAT, piece.topEdge)
      }

      if (piece.row == rows - 1) {
        assertEquals(PieceEdgeType.FLAT, piece.bottomEdge)
      } else {
        assertNotEquals(PieceEdgeType.FLAT, piece.bottomEdge)
      }

      if (piece.col == 0) {
        assertEquals(PieceEdgeType.FLAT, piece.leftEdge)
      } else {
        assertNotEquals(PieceEdgeType.FLAT, piece.leftEdge)
      }

      if (piece.col == cols - 1) {
        assertEquals(PieceEdgeType.FLAT, piece.rightEdge)
      } else {
        assertNotEquals(PieceEdgeType.FLAT, piece.rightEdge)
      }
    }
  }

  @Test
  fun jigsawGenerator_adjacentEdgesAreComplementary() {
    val rows = 5
    val cols = 5
    val pieces = JigsawGenerator.generatePieces(rows, cols)
    val grid = Array(rows) { r -> Array(cols) { c -> pieces[r * cols + c] } }

    for (r in 0 until rows) {
      for (c in 0 until cols) {
        // Horizontal neighbor
        if (c < cols - 1) {
          val leftPiece = grid[r][c]
          val rightPiece = grid[r][c + 1]
          assertTrue(
            (leftPiece.rightEdge == PieceEdgeType.TAB_OUT && rightPiece.leftEdge == PieceEdgeType.TAB_IN) ||
            (leftPiece.rightEdge == PieceEdgeType.TAB_IN && rightPiece.leftEdge == PieceEdgeType.TAB_OUT)
          )
        }

        // Vertical neighbor
        if (r < rows - 1) {
          val topPiece = grid[r][c]
          val bottomPiece = grid[r + 1][c]
          assertTrue(
            (topPiece.bottomEdge == PieceEdgeType.TAB_OUT && bottomPiece.topEdge == PieceEdgeType.TAB_IN) ||
            (topPiece.bottomEdge == PieceEdgeType.TAB_IN && bottomPiece.topEdge == PieceEdgeType.TAB_OUT)
          )
        }
      }
    }
  }

  @Test
  fun jigsawGenerator_borderPieceCountIsCorrect() {
    val rows = 4
    val cols = 4
    val pieces = JigsawGenerator.generatePieces(rows, cols)
    // In a 4x4 grid: 4*4 - 2*2 interior = 16 - 4 = 12 border pieces
    val borderPieces = pieces.filter { it.isBorder }
    assertEquals(12, borderPieces.size)
  }

  @Test
  fun formatElapsedTime_formatsCorrectly() {
    assertEquals("00:00", com.example.ui.components.formatElapsedTime(0L))
    assertEquals("00:09", com.example.ui.components.formatElapsedTime(9L))
    assertEquals("00:59", com.example.ui.components.formatElapsedTime(59L))
    assertEquals("01:00", com.example.ui.components.formatElapsedTime(60L))
    assertEquals("01:05", com.example.ui.components.formatElapsedTime(65L))
    assertEquals("10:30", com.example.ui.components.formatElapsedTime(630L))
    assertEquals("1:00:00", com.example.ui.components.formatElapsedTime(3600L))
    assertEquals("1:02:05", com.example.ui.components.formatElapsedTime(3725L))
  }
}
