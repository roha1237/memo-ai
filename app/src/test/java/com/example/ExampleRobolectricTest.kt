package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.backup.BackupManager
import com.example.data.backup.ImportResult
import com.example.data.local.AppDatabase
import com.example.data.local.NoteEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  private lateinit var database: AppDatabase
  private lateinit var context: Context

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  @Throws(IOException::class)
  fun tearDown() {
    database.close()
  }

  @Test
  fun testAppNameResource() {
    val appName = context.getString(R.string.app_name)
    assertEquals("MemoAI", appName)
  }

  @Test
  fun testNoteCreationAndPersistence() = runBlocking {
    val dao = database.noteDao()
    val note = NoteEntity(
      id = "test-1",
      title = "Meeting Notes",
      content = "Discuss product roadmap",
      category = "Work",
      tags = "roadmap,q4",
      isPinned = true,
      isFavorite = false
    )

    dao.insertNote(note)
    val fetched = dao.getNoteById("test-1")
    assertNotNull(fetched)
    assertEquals("Meeting Notes", fetched?.title)
    assertTrue(fetched?.isPinned == true)
  }

  @Test
  fun testSoftDeleteAndRestore() = runBlocking {
    val dao = database.noteDao()
    val note = NoteEntity(
      id = "test-delete",
      title = "Draft Idea",
      content = "To be deleted"
    )

    dao.insertNote(note)

    // Move to trash
    dao.moveToTrash("test-delete")
    val activeNotes = dao.getAllActiveNotes().first()
    assertFalse(activeNotes.any { it.id == "test-delete" })

    val trashNotes = dao.getTrashNotes().first()
    assertTrue(trashNotes.any { it.id == "test-delete" })

    // Restore from trash
    dao.restoreFromTrash("test-delete")
    val restoredActive = dao.getAllActiveNotes().first()
    assertTrue(restoredActive.any { it.id == "test-delete" })
  }

  @Test
  fun testJsonExportAndImport() {
    val notes = listOf(
      NoteEntity(
        id = "n-1",
        title = "Export Test Note",
        content = "Testing JSON export",
        category = "Ideas",
        tags = "test,backup",
        isPinned = true,
        isFavorite = true
      )
    )

    val json = BackupManager.exportToJson(notes)
    assertTrue(json.contains("Export Test Note"))
    assertTrue(json.contains("MemoAI"))

    val importResult = BackupManager.importFromJson(json)
    assertTrue(importResult is ImportResult.Success)
    val success = importResult as ImportResult.Success
    assertEquals(1, success.importedCount)
    assertEquals("Export Test Note", success.notes[0].title)
    assertEquals("Ideas", success.notes[0].category)
  }

  @Test
  fun testMalformedJsonHandling() {
    val invalidJson = "{ corrupt_data "
    val result = BackupManager.importFromJson(invalidJson)
    assertTrue(result is ImportResult.Error)
  }
}
