package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.NoteEntity
import com.example.data.preferences.ViewLayout
import com.example.ui.components.CategoryFilterRow
import com.example.ui.components.EmptyStateView
import com.example.ui.components.NoteCard
import com.example.ui.components.NotesSearchBar
import com.example.ui.theme.IndigoLight
import com.example.viewmodel.NotesViewModel

@Composable
fun NotesDashboardScreen(
  viewModel: NotesViewModel,
  onOpenNote: (NoteEntity) -> Unit,
  onCreateNote: () -> Unit,
  modifier: Modifier = Modifier
) {
  val notes by viewModel.filteredNotes.collectAsStateWithLifecycle()
  val query by viewModel.searchQuery.collectAsStateWithLifecycle()
  val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
  val selectedTag by viewModel.selectedTag.collectAsStateWithLifecycle()
  val categoryCounts by viewModel.categoryCounts.collectAsStateWithLifecycle()
  val allTags by viewModel.allTags.collectAsStateWithLifecycle()
  val viewLayout by viewModel.viewLayout.collectAsStateWithLifecycle()
  val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()

  val pinnedNotes = remember(notes) { notes.filter { it.isPinned } }
  val unpinnedNotes = remember(notes) { notes.filter { !it.isPinned } }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    floatingActionButton = {
      FloatingActionButton(
        onClick = onCreateNote,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.testTag("create_note_fab")
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Create new note",
          modifier = Modifier.size(24.dp)
        )
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp)
    ) {
      Spacer(modifier = Modifier.height(8.dp))

      // Top Title Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.EditNote,
            contentDescription = null,
            tint = IndigoLight,
            modifier = Modifier.size(28.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "MemoAI",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Text(
          text = "${notes.size} ${if (notes.size == 1) "note" else "notes"}",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Search Bar
      NotesSearchBar(
        query = query,
        onQueryChange = viewModel::setSearchQuery,
        viewLayout = viewLayout,
        onToggleViewLayout = viewModel::toggleViewLayout,
        currentSort = sortOption,
        onSortChange = viewModel::setSort
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Category & Tag Filter Chips
      CategoryFilterRow(
        selectedCategory = selectedCategory,
        onCategorySelect = viewModel::setSelectedCategory,
        categoryCounts = categoryCounts,
        allTags = allTags,
        selectedTag = selectedTag,
        onTagSelect = viewModel::setSelectedTag
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Content: Empty State or Notes List/Grid
      if (notes.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          if (query.isNotEmpty() || selectedCategory != "All" || selectedTag != null) {
            EmptyStateView(
              title = "No Matching Notes",
              message = "No notes matched your current search filters. Try clearing the search query or selecting 'All' categories.",
              icon = Icons.Default.SearchOff,
              actionLabel = "Clear Filters",
              onActionClick = {
                viewModel.setSearchQuery("")
                viewModel.setSelectedCategory("All")
                viewModel.setSelectedTag(null)
              }
            )
          } else {
            EmptyStateView(
              title = "Your Canvas is Clean",
              message = "Tap the + button to create your first note, organize with tags, and explore AI summaries.",
              icon = Icons.Default.EditNote,
              actionLabel = "Create Note",
              onActionClick = onCreateNote
            )
          }
        }
      } else {
        if (viewLayout == ViewLayout.STAGGERED_GRID) {
          LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(bottom = 88.dp, top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalItemSpacing = 10.dp,
            modifier = Modifier.fillMaxSize()
          ) {
            if (pinnedNotes.isNotEmpty()) {
              item(span = androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan.FullLine) {
                PinnedHeaderRow(count = pinnedNotes.size)
              }
              items(pinnedNotes, key = { it.id }) { note ->
                NoteCard(
                  note = note,
                  onClick = { onOpenNote(note) },
                  onTogglePin = { viewModel.togglePin(note) },
                  onToggleFavorite = { viewModel.toggleFavorite(note) },
                  onDelete = { viewModel.moveToTrash(note) }
                )
              }
              if (unpinnedNotes.isNotEmpty()) {
                item(span = androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan.FullLine) {
                  Text(
                    text = "Other Notes",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                  )
                }
              }
            }

            items(unpinnedNotes, key = { it.id }) { note ->
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
          // Linear List
          LazyColumn(
            contentPadding = PaddingValues(bottom = 88.dp, top = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
          ) {
            if (pinnedNotes.isNotEmpty()) {
              item {
                PinnedHeaderRow(count = pinnedNotes.size)
              }
              items(pinnedNotes, key = { it.id }) { note ->
                NoteCard(
                  note = note,
                  onClick = { onOpenNote(note) },
                  onTogglePin = { viewModel.togglePin(note) },
                  onToggleFavorite = { viewModel.toggleFavorite(note) },
                  onDelete = { viewModel.moveToTrash(note) }
                )
              }
              if (unpinnedNotes.isNotEmpty()) {
                item {
                  Text(
                    text = "Other Notes",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                  )
                }
              }
            }

            items(unpinnedNotes, key = { it.id }) { note ->
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

@Composable
private fun PinnedHeaderRow(count: Int) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.padding(bottom = 4.dp)
  ) {
    Icon(
      imageVector = Icons.Default.PushPin,
      contentDescription = null,
      tint = IndigoLight,
      modifier = Modifier.size(14.dp)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = "Pinned ($count)",
      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
      color = IndigoLight
    )
  }
}
