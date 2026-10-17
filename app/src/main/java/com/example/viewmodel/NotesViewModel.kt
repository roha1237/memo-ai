package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiResult
import com.example.data.ai.AiService
import com.example.data.ai.AiTask
import com.example.data.backup.BackupManager
import com.example.data.backup.ImportResult
import com.example.data.local.AppDatabase
import com.example.data.local.NoteEntity
import com.example.data.local.NoteRepository
import com.example.data.local.NoteRepositoryImpl
import com.example.data.preferences.SortOption
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.ViewLayout
import com.example.ui.model.formatTags
import com.example.ui.model.parseTags
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class SaveStatus {
  IDLE,
  SAVING,
  SAVED
}

class NotesViewModel(application: Application) : AndroidViewModel(application) {
  private val database = AppDatabase.getDatabase(application)
  private val repository: NoteRepository = NoteRepositoryImpl(database.noteDao())
  val userPreferences = UserPreferences(application)
  val aiService = AiService()

  // App Theme & Preferences
  val themeMode: StateFlow<ThemeMode> = userPreferences.themeMode
  val viewLayout: StateFlow<ViewLayout> = userPreferences.viewLayout
  val sortOption: StateFlow<SortOption> = userPreferences.sortOption

  // Search & Filter State
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _selectedCategory = MutableStateFlow("All")
  val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

  private val _selectedTag = MutableStateFlow<String?>(null)
  val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

  // Active, Favorite, and Trash raw flows
  val activeNotesRaw = repository.getAllActiveNotes()
  val favoriteNotesRaw = repository.getFavoriteNotes()
  val trashNotes = repository.getTrashNotes()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Filtered Notes
  val filteredNotes: StateFlow<List<NoteEntity>> = combine(
    activeNotesRaw,
    _searchQuery,
    _selectedCategory,
    _selectedTag,
    sortOption
  ) { notes, query, category, tag, sort ->
    var result = notes

    // Filter by category
    if (category != "All") {
      result = result.filter { it.category.equals(category, ignoreCase = true) }
    }

    // Filter by tag
    if (tag != null) {
      result = result.filter {
        parseTags(it.tags).any { t -> t.equals(tag, ignoreCase = true) }
      }
    }

    // Filter by query (title, content, tags, category)
    if (query.isNotBlank()) {
      val q = query.trim().lowercase()
      result = result.filter {
        it.title.lowercase().contains(q) ||
            it.content.lowercase().contains(q) ||
            it.tags.lowercase().contains(q) ||
            it.category.lowercase().contains(q)
      }
    }

    // Sort
    when (sort) {
      SortOption.MODIFIED_DESC -> result.sortedWith(
        compareByDescending<NoteEntity> { it.isPinned }
          .thenByDescending { it.updatedAt }
      )
      SortOption.MODIFIED_ASC -> result.sortedWith(
        compareByDescending<NoteEntity> { it.isPinned }
          .thenBy { it.updatedAt }
      )
      SortOption.CREATED_DESC -> result.sortedWith(
        compareByDescending<NoteEntity> { it.isPinned }
          .thenByDescending { it.createdAt }
      )
      SortOption.TITLE_ASC -> result.sortedWith(
        compareByDescending<NoteEntity> { it.isPinned }
          .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title.ifBlank { "Untitled" } }
      )
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // All distinct tags across active notes
  val allTags: StateFlow<List<String>> = activeNotesRaw.combine(_selectedCategory) { notes, _ ->
    notes.flatMap { parseTags(it.tags) }
      .map { it.trim() }
      .filter { it.isNotEmpty() }
      .distinct()
      .sorted()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Categories count map
  val categoryCounts: StateFlow<Map<String, Int>> = activeNotesRaw.combine(_searchQuery) { notes, _ ->
    val map = mutableMapOf("All" to notes.size)
    for (note in notes) {
      val cat = note.category.ifBlank { "General" }
      map[cat] = (map[cat] ?: 0) + 1
    }
    map
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

  // Editor State
  private val _editingNoteId = MutableStateFlow<String?>(null)
  val editingNoteId: StateFlow<String?> = _editingNoteId.asStateFlow()

  val editorTitle = MutableStateFlow("")
  val editorContent = MutableStateFlow("")
  val editorCategory = MutableStateFlow("General")
  val editorTags = MutableStateFlow<List<String>>(emptyList())
  val editorColorHex = MutableStateFlow("#00000000")
  val editorIsPinned = MutableStateFlow(false)
  val editorIsFavorite = MutableStateFlow(false)

  private val _saveStatus = MutableStateFlow(SaveStatus.SAVED)
  val saveStatus: StateFlow<SaveStatus> = _saveStatus.asStateFlow()

  private var autoSaveJob: Job? = null
  private var editorCreatedAt = System.currentTimeMillis()

  // AI State
  val isAiSheetVisible = MutableStateFlow(false)
  val currentAiTask = MutableStateFlow(AiTask.SUMMARIZE)
  val isAiLoading = MutableStateFlow(false)
  val aiResult = MutableStateFlow<AiResult?>(null)

  // Undo / Message Events
  private val _snackMessages = MutableSharedFlow<String>()
  val snackMessages = _snackMessages.asSharedFlow()

  private var lastDeletedNote: NoteEntity? = null

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setSelectedCategory(category: String) {
    _selectedCategory.value = category
  }

  fun setSelectedTag(tag: String?) {
    _selectedTag.value = tag
  }

  fun toggleViewLayout() {
    val next = if (viewLayout.value == ViewLayout.STAGGERED_GRID) {
      ViewLayout.LINEAR_LIST
    } else {
      ViewLayout.STAGGERED_GRID
    }
    userPreferences.setViewLayout(next)
  }

  fun setSort(option: SortOption) {
    userPreferences.setSortOption(option)
  }

  fun setTheme(mode: ThemeMode) {
    userPreferences.setThemeMode(mode)
  }

  // --- Note Editor Actions ---

  fun openNewNote() {
    autoSaveJob?.cancel()
    val newId = UUID.randomUUID().toString()
    _editingNoteId.value = newId
    editorTitle.value = ""
    editorContent.value = ""
    editorCategory.value = if (_selectedCategory.value != "All") _selectedCategory.value else "General"
    editorTags.value = if (_selectedTag.value != null) listOf(_selectedTag.value!!) else emptyList()
    editorColorHex.value = "#00000000"
    editorIsPinned.value = false
    editorIsFavorite.value = false
    editorCreatedAt = System.currentTimeMillis()
    _saveStatus.value = SaveStatus.SAVED
  }

  fun openExistingNote(note: NoteEntity) {
    autoSaveJob?.cancel()
    _editingNoteId.value = note.id
    editorTitle.value = note.title
    editorContent.value = note.content
    editorCategory.value = note.category
    editorTags.value = parseTags(note.tags)
    editorColorHex.value = note.colorHex
    editorIsPinned.value = note.isPinned
    editorIsFavorite.value = note.isFavorite
    editorCreatedAt = note.createdAt
    _saveStatus.value = SaveStatus.SAVED
  }

  fun onEditorTextChanged(title: String, content: String) {
    editorTitle.value = title
    editorContent.value = content
    scheduleAutoSave()
  }

  fun setEditorCategory(cat: String) {
    editorCategory.value = cat
    scheduleAutoSave()
  }

  fun addEditorTag(tag: String) {
    val clean = tag.trim().removePrefix("#")
    if (clean.isNotEmpty() && !editorTags.value.contains(clean)) {
      editorTags.value = editorTags.value + clean
      scheduleAutoSave()
    }
  }

  fun removeEditorTag(tag: String) {
    editorTags.value = editorTags.value.filter { it != tag }
    scheduleAutoSave()
  }

  fun setEditorColor(hex: String) {
    editorColorHex.value = hex
    scheduleAutoSave()
  }

  fun toggleEditorPin() {
    editorIsPinned.value = !editorIsPinned.value
    scheduleAutoSave()
  }

  fun toggleEditorFavorite() {
    editorIsFavorite.value = !editorIsFavorite.value
    scheduleAutoSave()
  }

  private fun scheduleAutoSave() {
    _saveStatus.value = SaveStatus.SAVING
    autoSaveJob?.cancel()
    autoSaveJob = viewModelScope.launch {
      delay(600) // 600ms debounce
      saveCurrentNoteInternal()
    }
  }

  fun flushSaveNow() {
    autoSaveJob?.cancel()
    viewModelScope.launch {
      saveCurrentNoteInternal()
    }
  }

  private suspend fun saveCurrentNoteInternal() {
    val id = _editingNoteId.value ?: return
    val title = editorTitle.value.trim()
    val content = editorContent.value.trim()

    // Don't create ghost notes if both title and content are completely empty
    if (title.isEmpty() && content.isEmpty()) {
      _saveStatus.value = SaveStatus.SAVED
      return
    }

    val note = NoteEntity(
      id = id,
      title = title,
      content = content,
      category = editorCategory.value,
      tags = formatTags(editorTags.value),
      isPinned = editorIsPinned.value,
      isFavorite = editorIsFavorite.value,
      isDeleted = false,
      colorHex = editorColorHex.value,
      createdAt = editorCreatedAt,
      updatedAt = System.currentTimeMillis()
    )
    repository.saveNote(note)
    _saveStatus.value = SaveStatus.SAVED
  }

  // --- Quick Card Actions ---

  fun togglePin(note: NoteEntity) {
    viewModelScope.launch {
      repository.togglePin(note.id, !note.isPinned)
    }
  }

  fun toggleFavorite(note: NoteEntity) {
    viewModelScope.launch {
      repository.toggleFavorite(note.id, !note.isFavorite)
    }
  }

  fun moveToTrash(note: NoteEntity) {
    viewModelScope.launch {
      lastDeletedNote = note
      repository.moveToTrash(note.id)
      _snackMessages.emit("Moved '${note.title.ifBlank { "Untitled" }}' to trash")
    }
  }

  fun undoDelete() {
    val note = lastDeletedNote ?: return
    viewModelScope.launch {
      repository.restoreFromTrash(note.id)
      lastDeletedNote = null
      _snackMessages.emit("Restored '${note.title.ifBlank { "Untitled" }}'")
    }
  }

  // --- Trash Actions ---

  fun restoreFromTrash(note: NoteEntity) {
    viewModelScope.launch {
      repository.restoreFromTrash(note.id)
      _snackMessages.emit("Restored note")
    }
  }

  fun deletePermanently(note: NoteEntity) {
    viewModelScope.launch {
      repository.deletePermanently(note.id)
      _snackMessages.emit("Note permanently deleted")
    }
  }

  fun emptyTrash() {
    viewModelScope.launch {
      repository.emptyTrash()
      _snackMessages.emit("Trash emptied")
    }
  }

  // --- AI Actions ---

  fun openAiAssistant(task: AiTask = AiTask.SUMMARIZE) {
    currentAiTask.value = task
    aiResult.value = null
    isAiSheetVisible.value = true
    runAiTask(task)
  }

  fun closeAiAssistant() {
    isAiSheetVisible.value = false
    aiResult.value = null
    isAiLoading.value = false
  }

  fun runAiTask(task: AiTask) {
    currentAiTask.value = task
    isAiLoading.value = true
    aiResult.value = null

    viewModelScope.launch {
      val result = aiService.processNote(
        task = task,
        title = editorTitle.value,
        content = editorContent.value
      )
      aiResult.value = result
      isAiLoading.value = false
    }
  }

  fun appendAiResultToNote(text: String) {
    val current = editorContent.value
    editorContent.value = if (current.isBlank()) text else "$current\n\n$text"
    scheduleAutoSave()
    closeAiAssistant()
  }

  fun replaceNoteWithAiResult(text: String) {
    editorContent.value = text
    scheduleAutoSave()
    closeAiAssistant()
  }

  // --- JSON Export & Import ---

  suspend fun exportNotesJson(): String {
    val allActive = repository.getAllActiveNotesDirect()
    return BackupManager.exportToJson(allActive)
  }

  fun importNotesJson(jsonString: String, onComplete: (ImportResult) -> Unit) {
    viewModelScope.launch {
      val result = BackupManager.importFromJson(jsonString)
      if (result is ImportResult.Success) {
        repository.saveNotes(result.notes)
        _snackMessages.emit("Successfully imported ${result.importedCount} notes")
      }
      onComplete(result)
    }
  }
}
