package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.NoteEntity
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.NoteCard
import com.example.ui.theme.RoseError
import com.example.viewmodel.NotesViewModel

@Composable
fun TrashScreen(
  viewModel: NotesViewModel,
  modifier: Modifier = Modifier
) {
  val trashNotes by viewModel.trashNotes.collectAsStateWithLifecycle()
  var showEmptyTrashDialog by remember { mutableStateOf(false) }
  var noteToPermanentlyDelete by remember { mutableStateOf<NoteEntity?>(null) }

  Scaffold(
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp)
    ) {
      Spacer(modifier = Modifier.height(8.dp))

      // Header row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Trash",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "${trashNotes.size} ${if (trashNotes.size == 1) "item" else "items"}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        if (trashNotes.isNotEmpty()) {
          OutlinedButton(
            onClick = { showEmptyTrashDialog = true },
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = RoseError
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("empty_trash_button")
          ) {
            Icon(
              imageVector = Icons.Default.DeleteSweep,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Empty Trash")
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (trashNotes.isNotEmpty()) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Notes here can be restored back to your notebook or permanently removed.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
          contentPadding = PaddingValues(bottom = 80.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          items(trashNotes, key = { it.id }) { note ->
            NoteCard(
              note = note,
              isTrashMode = true,
              onClick = { /* In trash, don't edit */ },
              onTogglePin = {},
              onToggleFavorite = {},
              onDelete = {},
              onRestore = { viewModel.restoreFromTrash(note) },
              onPermanentDelete = { noteToPermanentlyDelete = note }
            )
          }
        }
      } else {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          EmptyStateView(
            title = "Trash is Empty",
            message = "No deleted notes. Items moved to the trash will appear here.",
            icon = Icons.Default.DeleteForever
          )
        }
      }
    }
  }

  // Confirmation dialog: Empty Trash
  if (showEmptyTrashDialog) {
    ConfirmationDialog(
      title = "Empty Trash?",
      message = "All ${trashNotes.size} notes in trash will be permanently deleted. This action cannot be undone.",
      confirmButtonText = "Empty Trash",
      isDestructive = true,
      onConfirm = {
        viewModel.emptyTrash()
        showEmptyTrashDialog = false
      },
      onDismiss = { showEmptyTrashDialog = false }
    )
  }

  // Confirmation dialog: Permanently delete single note
  if (noteToPermanentlyDelete != null) {
    val note = noteToPermanentlyDelete!!
    ConfirmationDialog(
      title = "Delete Permanently?",
      message = "Are you sure you want to permanently delete '${note.title.ifBlank { "Untitled" }}'? This cannot be recovered.",
      confirmButtonText = "Delete Permanently",
      isDestructive = true,
      onConfirm = {
        viewModel.deletePermanently(note)
        noteToPermanentlyDelete = null
      },
      onDismiss = { noteToPermanentlyDelete = null }
    )
  }
}
