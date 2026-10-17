package com.example.ui.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.NoteTintAmber
import com.example.ui.theme.NoteTintCyan
import com.example.ui.theme.NoteTintDefault
import com.example.ui.theme.NoteTintEmerald
import com.example.ui.theme.NoteTintIndigo
import com.example.ui.theme.NoteTintRose
import com.example.ui.theme.NoteTintViolet

val PRESET_CATEGORIES = listOf(
  "General",
  "Work",
  "Personal",
  "Ideas",
  "Study",
  "To-Do"
)

data class NoteColorOption(
  val hex: String,
  val name: String,
  val tintColor: Color,
  val borderColor: Color
)

val NOTE_COLORS = listOf(
  NoteColorOption("#00000000", "Default", NoteTintDefault, Color(0xFF475569)),
  NoteColorOption("#1A6366F1", "Indigo", NoteTintIndigo, Color(0xFF6366F1)),
  NoteColorOption("#1A10B981", "Emerald", NoteTintEmerald, Color(0xFF10B981)),
  NoteColorOption("#1AF59E0B", "Amber", NoteTintAmber, Color(0xFFF59E0B)),
  NoteColorOption("#1AF43F5E", "Rose", NoteTintRose, Color(0xFFF43F5E)),
  NoteColorOption("#1A8B5CF6", "Violet", NoteTintViolet, Color(0xFF8B5CF6)),
  NoteColorOption("#1A06B6D4", "Cyan", NoteTintCyan, Color(0xFF06B6D4))
)

fun parseTags(tagString: String): List<String> {
  return tagString.split(",")
    .map { it.trim().removePrefix("#") }
    .filter { it.isNotEmpty() }
}

fun formatTags(tags: List<String>): String {
  return tags.joinToString(",") { it.trim().removePrefix("#") }
}

fun calculateWordCount(text: String): Int {
  val trimmed = text.trim()
  if (trimmed.isEmpty()) return 0
  return trimmed.split("\\s+".toRegex()).size
}

fun calculateCharCount(text: String): Int {
  return text.length
}
