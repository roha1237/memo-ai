package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
  @PrimaryKey
  val id: String,
  val title: String = "",
  val content: String = "",
  val category: String = "General",
  val tags: String = "", // Comma-delimited list e.g. "Work,Ideas"
  val isPinned: Boolean = false,
  val isFavorite: Boolean = false,
  val isDeleted: Boolean = false,
  val colorHex: String = "#00000000",
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
  val deletedAt: Long? = null
)
