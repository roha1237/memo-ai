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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.NoteEntity
import com.example.data.preferences.ViewLayout
import com.example.ui.components.EmptyStateView
import com.example.ui.components.NoteCard
import com.example.ui.theme.AmberWarning
import com.example.viewmodel.NotesViewModel

@Composable
fun FavoritesScreen(
  viewModel: NotesViewModel,
  onOpenNote: (NoteEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  val favorites by viewModel.favoriteNotesRaw.collectAsStateWithLifecycle(emptyList())
  val viewLayout by viewModel.viewLayout.collectAsStateWithLifecycle()

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

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = AmberWarning,
            modifier = Modifier.size(26.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Favorites",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Text(
          text = "${favorites.size} ${if (favorites.size == 1) "starred" else "starred"}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (favorites.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          EmptyStateView(
            title = "No Favorites Yet",
            message = "Tap the star icon on any note card or inside the editor to keep your priority notes here.",
            icon = Icons.Outlined.StarBorder
          )
        }
      } else {
        if (viewLayout == ViewLayout.STAGGERED_GRID) {
          LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalItemSpacing = 10.dp,
            modifier = Modifier.fillMaxSize()
          ) {
            items(favorites, key = { it.id }) { note ->
              NoteCard(
                note = note,
                onClick = { onOpenNote(note) },
                onTogglePin = { viewModel.togglePin(note) },
                onToggleFavorite = { viewModel.toggleFavorite(note) },
                onDelete = { viewModel.moveToTrash(note) }
              )
            }
          }
        } else {
          LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
          ) {
            items(favorites, key = { it.id }) { note ->
              NoteCard(
                note = note,
                onClick = { onOpenNote(note) },
                onTogglePin = { viewModel.togglePin(note) },
                onToggleFavorite = { viewModel.toggleFavorite(note) },
                onDelete = { viewModel.moveToTrash(note) }
              )
            }
          }
        }
      }
    }
  }
}
