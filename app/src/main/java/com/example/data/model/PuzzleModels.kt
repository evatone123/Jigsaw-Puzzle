package com.example.data.model

enum class PieceEdgeType {
  FLAT,
  TAB_OUT, // Male tab extending outwards
  TAB_IN   // Female socket dipping inwards
}

data class PuzzlePiece(
  val id: Int,
  val row: Int,
  val col: Int,
  val topEdge: PieceEdgeType,
  val rightEdge: PieceEdgeType,
  val bottomEdge: PieceEdgeType,
  val leftEdge: PieceEdgeType,
  var isPlaced: Boolean = false,
  var boardX: Float = 0f,
  var boardY: Float = 0f,
  var isOnBoard: Boolean = false // If moved out of tray onto board table
) {
  val isBorder: Boolean
    get() = topEdge == PieceEdgeType.FLAT ||
            rightEdge == PieceEdgeType.FLAT ||
            bottomEdge == PieceEdgeType.FLAT ||
            leftEdge == PieceEdgeType.FLAT
}

enum class PuzzleDifficulty(val pieceCount: Int, val rows: Int, val cols: Int, val label: String) {
  EASY_16(16, 4, 4, "16 Pieces"),
  MEDIUM_36(36, 6, 6, "36 Pieces"),
  HARD_64(64, 8, 8, "64 Pieces"),
  EXPERT_100(100, 10, 10, "100 Pieces")
}

data class PuzzleCatalogItem(
  val id: String,
  val title: String,
  val category: String,
  val drawableResId: Int? = null,
  val customUri: String? = null,
  val isDaily: Boolean = false,
  val completed: Boolean = false,
  val bestTimeSeconds: Long? = null,
  val lastPieceCount: Int = 36
)
