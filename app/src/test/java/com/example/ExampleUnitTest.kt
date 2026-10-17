package com.example

import com.example.ui.model.calculateCharCount
import com.example.ui.model.calculateWordCount
import com.example.ui.model.formatTags
import com.example.ui.model.parseTags
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testWordCountCalculation() {
    assertEquals(0, calculateWordCount(""))
    assertEquals(0, calculateWordCount("   "))
    assertEquals(4, calculateWordCount("One two three four"))
    assertEquals(3, calculateWordCount("  Multiple   spaces   between  "))
  }

  @Test
  fun testCharCountCalculation() {
    assertEquals(0, calculateCharCount(""))
    assertEquals(5, calculateCharCount("Hello"))
  }

  @Test
  fun testTagParsingAndFormatting() {
    val tags = parseTags("#Work, #Ideas, Planning")
    assertEquals(listOf("Work", "Ideas", "Planning"), tags)

    val formatted = formatTags(tags)
    assertEquals("Work,Ideas,Planning", formatted)
  }
}
