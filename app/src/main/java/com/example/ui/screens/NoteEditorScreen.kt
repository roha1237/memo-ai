package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AiBottomSheet
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.SaveStatusIndicator
import com.example.ui.model.NOTE_COLORS
import com.example.ui.model.PRESET_CATEGORIES
import com.example.ui.model.calculateCharCount
import com.example.ui.model.calculateWordCount
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.RoseError
import com.example.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteEditorScreen(
  viewModel: NotesViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val title by viewModel.editorTitle.collectAsStateWithLifecycle()
  val content by viewModel.editorContent.collectAsStateWithLifecycle()
  val category by viewModel.editorCategory.collectAsStateWithLifecycle()
  val tags by viewModel.editorTags.collectAsStateWithLifecycle()
  val colorHex by viewModel.editorColorHex.collectAsStateWithLifecycle()
  val isPinned by viewModel.editorIsPinned.collectAsStateWithLifecycle()
  val isFavorite by viewModel.editorIsFavorite.collectAsStateWithLifecycle()
  val saveStatus by viewModel.saveStatus.collectAsStateWithLifecycle()

  // AI State
  val isAiVisible by viewModel.isAiSheetVisible.collectAsStateWithLifecycle()
  val currentAiTask by viewModel.currentAiTask.collectAsStateWithLifecycle()
  val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
  val aiResult by viewModel.aiResult.collectAsStateWithLifecycle()
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var showColorMenu by remember { mutableStateOf(false) }
  var showCategoryMenu by remember { mutableStateOf(false) }
  var showDeleteDialog by remember { mutableStateOf(false) }
  var newTagInput by remember { mutableStateOf("") }
  var isAddingTag by remember { mutableStateOf(false) }

  val currentColorOption = remember(colorHex) {
    NOTE_COLORS.find { it.hex == colorHex } ?: NOTE_COLORS.first()
  }

  val wordCount = remember(content) { calculateWordCount(content) }
  val charCount = remember(content) { calculateCharCount(content) }

  // Handle system back navigation safely by flushing save
  BackHandler {
    viewModel.flushSaveNow()
    onNavigateBack()
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = if (currentColorOption.tintColor != Color.Transparent) {
      MaterialTheme.colorScheme.background
    } else {
      MaterialTheme.colorScheme.background
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(currentColorOption.tintColor)
        .padding(innerPadding)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        // Top Action Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = {
              viewModel.flushSaveNow()
              onNavigateBack()
            },
            modifier = Modifier.testTag("editor_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Navigate back",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }

          // Save Indicator Pill
          SaveStatusIndicator(saveStatus = saveStatus)

          Row(verticalAlignment = Alignment.CenterVertically) {
            // AI Assistant Button
            IconButton(
              onClick = { viewModel.openAiAssistant() },
              modifier = Modifier.testTag("editor_ai_button")
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI Assistant",
                tint = IndigoLight
              )
            }

            // Pin Toggle
            IconButton(
              onClick = viewModel::toggleEditorPin,
              modifier = Modifier.testTag("editor_pin_toggle")
            ) {
              Icon(
                imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                contentDescription = "Pin toggle",
                tint = if (isPinned) IndigoLight else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            // Favorite Toggle
            IconButton(
              onClick = viewModel::toggleEditorFavorite,
              modifier = Modifier.testTag("editor_favorite_toggle")
            ) {
              Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = "Favorite toggle",
                tint = if (isFavorite) AmberWarning else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            // Color Palette Picker
            Box {
              IconButton(
                onClick = { showColorMenu = true },
                modifier = Modifier.testTag("editor_color_palette")
              ) {
                Icon(
                  imageVector = Icons.Default.Palette,
                  contentDescription = "Color palette",
                  tint = if (currentColorOption.borderColor != Color(0xFF475569)) {
                    currentColorOption.borderColor
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                  }
                )
              }

              DropdownMenu(
                expanded = showColorMenu,
                onDismissRequest = { showColorMenu = false }
              ) {
                NOTE_COLORS.forEach { option ->
                  DropdownMenuItem(
                    text = {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                          modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (option.hex == "#00000000") Color.Gray else option.borderColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(option.name)
                      }
                    },
                    onClick = {
                      viewModel.setEditorColor(option.hex)
                      showColorMenu = false
                    }
                  )
                }
              }
            }

            // Delete action
            IconButton(
              onClick = { showDeleteDialog = true },
              modifier = Modifier.testTag("editor_delete_button")
            ) {
              Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete note",
                tint = RoseError.copy(alpha = 0.8f)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Metadata row: Category pill & Word Counter
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Category Selector
          Box {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .clickable { showCategoryMenu = true }
                .testTag("editor_category_selector")
            ) {
              Text(
                text = "Category: $category ▾",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
              )
            }

            DropdownMenu(
              expanded = showCategoryMenu,
              onDismissRequest = { showCategoryMenu = false }
            ) {
              PRESET_CATEGORIES.forEach { cat ->
                DropdownMenuItem(
                  text = { Text(cat) },
                  onClick = {
                    viewModel.setEditorCategory(cat)
                    showCategoryMenu = false
                  }
                )
              }
            }
          }

          // Word / Character Count
          Text(
            text = "$wordCount words • $charCount chars",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tags Section
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          tags.forEach { tag ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = IndigoLight.copy(alpha = 0.15f),
              modifier = Modifier.testTag("editor_tag_$tag")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "#$tag",
                  style = MaterialTheme.typography.labelSmall,
                  color = IndigoLight
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Remove tag",
                  tint = IndigoLight,
                  modifier = Modifier
                    .size(14.dp)
                    .clickable { viewModel.removeEditorTag(tag) }
                )
              }
            }
          }

          if (isAddingTag) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
              BasicTextField(
                value = newTagInput,
                onValueChange = { newTagInput = it },
                singleLine = true,
                textStyle = TextStyle(
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                  .width(80.dp)
                  .testTag("new_tag_text_input")
              )
              IconButton(
                onClick = {
                  if (newTagInput.isNotBlank()) {
                    viewModel.addEditorTag(newTagInput)
                    newTagInput = ""
                  }
                  isAddingTag = false
                },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = "Confirm add tag",
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          } else {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier
                .clickable { isAddingTag = true }
                .testTag("add_tag_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Add Tag",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title Input Field
        Box(modifier = Modifier.fillMaxWidth()) {
          if (title.isEmpty()) {
            Text(
              text = "Note Title",
              style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
              )
            )
          }
          BasicTextField(
            value = title,
            onValueChange = { newTitle ->
              viewModel.onEditorTextChanged(newTitle, content)
            },
            textStyle = MaterialTheme.typography.headlineSmall.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("note_title_input")
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content Input Field
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(480.dp)
        ) {
          if (content.isEmpty()) {
            Text(
              text = "Start typing your note here...\nAll changes are saved automatically.",
              style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 26.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
              )
            )
          }
          BasicTextField(
            value = content,
            onValueChange = { newContent ->
              viewModel.onEditorTextChanged(title, newContent)
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
              lineHeight = 26.sp,
              color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
              .fillMaxSize()
              .testTag("note_content_input")
          )
        }
      }
    }
  }

  // Delete Confirmation Dialog
  if (showDeleteDialog) {
    ConfirmationDialog(
      title = "Move to Trash?",
      message = "This note will be moved to the trash. You can restore it later if needed.",
      confirmButtonText = "Move to Trash",
      isDestructive = true,
      onConfirm = {
        showDeleteDialog = false
        val currentId = viewModel.editingNoteId.value
        if (currentId != null) {
          viewModel.flushSaveNow()
          // Soft delete
          val entity = com.example.data.local.NoteEntity(
            id = currentId,
            title = title,
            content = content
          )
          viewModel.moveToTrash(entity)
        }
        onNavigateBack()
      },
      onDismiss = { showDeleteDialog = false }
    )
  }

  // AI Bottom Sheet
  if (isAiVisible) {
    AiBottomSheet(
      sheetState = sheetState,
      isConfigured = viewModel.aiService.isConfigured(),
      currentTask = currentAiTask,
      isLoading = isAiLoading,
      result = aiResult,
      onSelectTask = { task -> viewModel.runAiTask(task) },
      onRunTask = { task -> viewModel.runAiTask(task) },
      onAppendResult = { text -> viewModel.appendAiResultToNote(text) },
      onReplaceResult = { text -> viewModel.replaceNoteWithAiResult(text) },
      onDismiss = { viewModel.closeAiAssistant() }
    )
  }
}
