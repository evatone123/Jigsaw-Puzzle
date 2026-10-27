package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PuzzleProgressDao {

  @Query("SELECT * FROM puzzle_progress WHERE puzzleId = :puzzleId")
  suspend fun getProgress(puzzleId: String): PuzzleProgressEntity?

  @Query("SELECT * FROM puzzle_progress WHERE puzzleId = :puzzleId")
  fun getProgressFlow(puzzleId: String): Flow<PuzzleProgressEntity?>

  @Query("SELECT * FROM puzzle_progress ORDER BY lastUpdatedTimestamp DESC")
  fun getAllProgress(): Flow<List<PuzzleProgressEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveProgress(progress: PuzzleProgressEntity)

  @Query("DELETE FROM puzzle_progress WHERE puzzleId = :puzzleId")
  suspend fun deleteProgress(puzzleId: String)

  @Query("DELETE FROM puzzle_progress")
  suspend fun clearAll()
}
