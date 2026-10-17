package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
  @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC")
  fun getAllActiveNotes(): Flow<List<NoteEntity>>

  @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isFavorite = 1 ORDER BY isPinned DESC, updatedAt DESC")
  fun getFavoriteNotes(): Flow<List<NoteEntity>>

  @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY deletedAt DESC, updatedAt DESC")
  fun getTrashNotes(): Flow<List<NoteEntity>>

  @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
  fun getNoteByIdFlow(id: String): Flow<NoteEntity?>

  @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
  suspend fun getNoteById(id: String): NoteEntity?

  @Query("SELECT * FROM notes WHERE isDeleted = 0")
  suspend fun getAllActiveNotesDirect(): List<NoteEntity>

  @Query("SELECT * FROM notes")
  suspend fun getAllNotesDirect(): List<NoteEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNote(note: NoteEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotes(notes: List<NoteEntity>)

  @Update
  suspend fun updateNote(note: NoteEntity)

  @Query("UPDATE notes SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
  suspend fun moveToTrash(id: String, deletedAt: Long = System.currentTimeMillis())

  @Query("UPDATE notes SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
  suspend fun restoreFromTrash(id: String)

  @Query("DELETE FROM notes WHERE id = :id")
  suspend fun deletePermanently(id: String)

  @Query("DELETE FROM notes WHERE isDeleted = 1")
  suspend fun emptyTrash()

  @Query("UPDATE notes SET isPinned = :isPinned WHERE id = :id")
  suspend fun setPinned(id: String, isPinned: Boolean)

  @Query("UPDATE notes SET isFavorite = :isFavorite WHERE id = :id")
  suspend fun setFavorite(id: String, isFavorite: Boolean)
}
