package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PuzzleProgressEntity::class], version = 1, exportSchema = false)
abstract class PuzzleDatabase : RoomDatabase() {

  abstract fun puzzleProgressDao(): PuzzleProgressDao

  companion object {
    @Volatile
    private var INSTANCE: PuzzleDatabase? = null

    fun getDatabase(context: Context): PuzzleDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          PuzzleDatabase::class.java,
          "puzzle_database"
        )
          .fallbackToDestructiveMigration(false)
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
