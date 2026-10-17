package com.example.data.backup

import com.example.data.local.NoteEntity
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

sealed class ImportResult {
  data class Success(val importedCount: Int, val notes: List<NoteEntity>) : ImportResult()
  data class Error(val message: String) : ImportResult()
}

object BackupManager {
  private const val SCHEMA_VERSION = 1
  private const val APP_IDENTIFIER = "MemoAI"

  fun exportToJson(notes: List<NoteEntity>): String {
    val root = JSONObject()
    root.put("app", APP_IDENTIFIER)
    root.put("version", SCHEMA_VERSION)
    root.put("exportedAt", System.currentTimeMillis())
    root.put("noteCount", notes.size)

    val notesArray = JSONArray()
    for (note in notes) {
      val noteObj = JSONObject()
      noteObj.put("id", note.id)
      noteObj.put("title", note.title)
      noteObj.put("content", note.content)
      noteObj.put("category", note.category)
      noteObj.put("tags", note.tags)
      noteObj.put("isPinned", note.isPinned)
      noteObj.put("isFavorite", note.isFavorite)
      noteObj.put("colorHex", note.colorHex)
      noteObj.put("createdAt", note.createdAt)
      noteObj.put("updatedAt", note.updatedAt)
      notesArray.put(noteObj)
    }

    root.put("notes", notesArray)
    return root.toString(2)
  }

  fun importFromJson(jsonString: String): ImportResult {
    val trimmed = jsonString.trim()
    if (trimmed.isEmpty()) {
      return ImportResult.Error("Input JSON string is empty.")
    }

    try {
      val parsedNotes = mutableListOf<NoteEntity>()

      // Allow either full Backup object { "notes": [...] } or raw array [ { ... } ]
      val notesArray: JSONArray = if (trimmed.startsWith("{")) {
        val root = JSONObject(trimmed)
        if (!root.has("notes")) {
          return ImportResult.Error("Invalid MemoAI backup: Missing 'notes' field in JSON object.")
        }
        root.getJSONArray("notes")
      } else if (trimmed.startsWith("[")) {
        JSONArray(trimmed)
      } else {
        return ImportResult.Error("Invalid JSON: Must be an object or array.")
      }

      for (i in 0 until notesArray.length()) {
        val item = notesArray.optJSONObject(i) ?: continue
        val title = item.optString("title", "").trim()
        val content = item.optString("content", "").trim()

        if (title.isEmpty() && content.isEmpty()) {
          // Skip completely blank notes
          continue
        }

        // Generate a new UUID if missing or blank
        val rawId = item.optString("id", "")
        val id = if (rawId.isNotBlank()) rawId else UUID.randomUUID().toString()

        val category = item.optString("category", "General").ifBlank { "General" }
        val tags = item.optString("tags", "")
        val isPinned = item.optBoolean("isPinned", false)
        val isFavorite = item.optBoolean("isFavorite", false)
        val colorHex = item.optString("colorHex", "#00000000")
        val now = System.currentTimeMillis()
        val createdAt = item.optLong("createdAt", now)
        val updatedAt = item.optLong("updatedAt", now)

        parsedNotes.add(
          NoteEntity(
            id = id,
            title = title,
            content = content,
            category = category,
            tags = tags,
            isPinned = isPinned,
            isFavorite = isFavorite,
            isDeleted = false,
            colorHex = colorHex,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = null
          )
        )
      }

      if (parsedNotes.isEmpty()) {
        return ImportResult.Error("No valid notes found in the provided backup.")
      }

      return ImportResult.Success(importedCount = parsedNotes.size, notes = parsedNotes)
    } catch (e: Exception) {
      return ImportResult.Error("Failed to parse JSON: ${e.localizedMessage ?: "Unknown syntax error"}")
    }
  }
}
