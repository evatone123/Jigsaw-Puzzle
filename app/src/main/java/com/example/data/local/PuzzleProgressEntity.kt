package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "puzzle_progress")
data class PuzzleProgressEntity(
  @PrimaryKey
  val puzzleId: String,
  val difficultyName: String,
  val pieceCount: Int,
  val placedPieceIds: String, // Comma-separated list of placed piece IDs, e.g. "0,2,3,5"
  val elapsedSeconds: Long,
  val remainingHints: Int,
  val isCompleted: Boolean,
  val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
