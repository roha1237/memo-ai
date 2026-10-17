package com.example.data.local

import kotlinx.coroutines.flow.Flow

interface NoteRepository {
  fun getAllActiveNotes(): Flow<List<NoteEntity>>
  fun getFavoriteNotes(): Flow<List<NoteEntity>>
  fun getTrashNotes(): Flow<List<NoteEntity>>
  fun getNoteByIdFlow(id: String): Flow<NoteEntity?>
  suspend fun getNoteById(id: String): NoteEntity?
  suspend fun getAllActiveNotesDirect(): List<NoteEntity>
  suspend fun getAllNotesDirect(): List<NoteEntity>
  suspend fun saveNote(note: NoteEntity)
  suspend fun saveNotes(notes: List<NoteEntity>)
  suspend fun moveToTrash(id: String)
  suspend fun restoreFromTrash(id: String)
  suspend fun deletePermanently(id: String)
  suspend fun emptyTrash()
  suspend fun togglePin(id: String, isPinned: Boolean)
  suspend fun toggleFavorite(id: String, isFavorite: Boolean)
}

class NoteRepositoryImpl(private val noteDao: NoteDao) : NoteRepository {
  override fun getAllActiveNotes(): Flow<List<NoteEntity>> = noteDao.getAllActiveNotes()

  override fun getFavoriteNotes(): Flow<List<NoteEntity>> = noteDao.getFavoriteNotes()

  override fun getTrashNotes(): Flow<List<NoteEntity>> = noteDao.getTrashNotes()

  override fun getNoteByIdFlow(id: String): Flow<NoteEntity?> = noteDao.getNoteByIdFlow(id)

  override suspend fun getNoteById(id: String): NoteEntity? = noteDao.getNoteById(id)

  override suspend fun getAllActiveNotesDirect(): List<NoteEntity> = noteDao.getAllActiveNotesDirect()

  override suspend fun getAllNotesDirect(): List<NoteEntity> = noteDao.getAllNotesDirect()

  override suspend fun saveNote(note: NoteEntity) {
    noteDao.insertNote(note)
  }

  override suspend fun saveNotes(notes: List<NoteEntity>) {
    noteDao.insertNotes(notes)
  }

  override suspend fun moveToTrash(id: String) {
    noteDao.moveToTrash(id)
  }

  override suspend fun restoreFromTrash(id: String) {
    noteDao.restoreFromTrash(id)
  }

  override suspend fun deletePermanently(id: String) {
    noteDao.deletePermanently(id)
  }

  override suspend fun emptyTrash() {
    noteDao.emptyTrash()
  }

  override suspend fun togglePin(id: String, isPinned: Boolean) {
    noteDao.setPinned(id, isPinned)
  }

  override suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
    noteDao.setFavorite(id, isFavorite)
  }
}
