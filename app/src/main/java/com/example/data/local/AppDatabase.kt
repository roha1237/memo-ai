package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(entities = [NoteEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
  abstract fun noteDao(): NoteDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "memo_ai_database"
        )
          .addCallback(DatabaseCallback())
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          CoroutineScope(Dispatchers.IO).launch {
            seedInitialNotes(database.noteDao())
          }
        }
      }

      private suspend fun seedInitialNotes(dao: NoteDao) {
        val currentTime = System.currentTimeMillis()
        val welcomeNote = NoteEntity(
          id = UUID.randomUUID().toString(),
          title = "Welcome to MemoAI \uD83D\uDE80",
          content = "MemoAI is your sleek, distraction-free smart note-taking companion.\n\n" +
              "Key features include:\n" +
              "• Auto-save in real-time as you type\n" +
              "• Categories & #tags for instant organization\n" +
              "• Pinned notes & Favorites\n" +
              "• Soft-delete Trash with Undo & Restore\n" +
              "• Full JSON Backup & Restore\n" +
              "• AI-Ready Assistant for summarization & action points\n" +
              "• 100% offline-first local persistence",
          category = "General",
          tags = "Guide,Tips",
          isPinned = true,
          isFavorite = true,
          isDeleted = false,
          colorHex = "#1A6366F1",
          createdAt = currentTime,
          updatedAt = currentTime
        )

        val sampleIdeasNote = NoteEntity(
          id = UUID.randomUUID().toString(),
          title = "Product Strategy & Architecture",
          content = "Architecture pillars for upcoming release:\n\n" +
              "1. Zero data loss: debounce auto-save with Room SQLite persistence.\n" +
              "2. Clean UI: dark mode by default, accessible contrast, smooth transitions.\n" +
              "3. AI Assistant module: pluggable Gemini integration for summarizing and extracting checklists.\n" +
              "4. Data portability: one-tap JSON backup & restore.",
          category = "Ideas",
          tags = "Architecture,Engineering",
          isPinned = false,
          isFavorite = true,
          isDeleted = false,
          colorHex = "#1A8B5CF6",
          createdAt = currentTime - 3600000,
          updatedAt = currentTime - 1800000
        )

        val sampleTodoNote = NoteEntity(
          id = UUID.randomUUID().toString(),
          title = "Weekly Action Items",
          content = "[✓] Review design guidelines and color palette\n" +
              "[✓] Implement Room persistence and reactive Flow\n" +
              "[ ] Test JSON export and import flow\n" +
              "[ ] Try out AI summarization assistant",
          category = "To-Do",
          tags = "Tasks,Weekly",
          isPinned = false,
          isFavorite = false,
          isDeleted = false,
          colorHex = "#1A10B981",
          createdAt = currentTime - 7200000,
          updatedAt = currentTime - 3600000
        )

        dao.insertNotes(listOf(welcomeNote, sampleIdeasNote, sampleTodoNote))
      }
    }
  }
}
