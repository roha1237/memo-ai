package com.example.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.NoteEditorScreen
import com.example.ui.screens.NotesDashboardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TrashScreen
import com.example.viewmodel.NotesViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MemoApp(
  viewModel: NotesViewModel,
  modifier: Modifier = Modifier
) {
  var currentDestination by remember { mutableStateOf(MainDestination.NOTES) }
  var isEditingNote by remember { mutableStateOf(false) }
  val snackbarHostState = remember { SnackbarHostState() }

  // Listen to snackbar events from ViewModel (e.g. "Moved note to trash" with Undo)
  LaunchedEffect(Unit) {
    viewModel.snackMessages.collectLatest { message ->
      if (message.contains("Moved", ignoreCase = true)) {
        val result = snackbarHostState.showSnackbar(
          message = message,
          actionLabel = "Undo",
          duration = SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) {
          viewModel.undoDelete()
        }
      } else {
        snackbarHostState.showSnackbar(
          message = message,
          duration = SnackbarDuration.Short
        )
      }
    }
  }

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val isExpanded = maxWidth >= 720.dp

    if (isEditingNote && !isExpanded) {
      // Full screen editor on compact phones
      NoteEditorScreen(
        viewModel = viewModel,
        onNavigateBack = {
          isEditingNote = false
        }
      )
    } else {
      Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
          if (!isExpanded) {
            NavigationBar(
              modifier = Modifier
                .testTag("bottom_nav_bar")
                .windowInsetsPadding(WindowInsets.navigationBars),
              containerColor = MaterialTheme.colorScheme.surface,
              tonalElevation = 4.dp
            ) {
              MainDestination.values().forEach { destination ->
                val isSelected = currentDestination == destination
                NavigationBarItem(
                  selected = isSelected,
                  onClick = {
                    currentDestination = destination
                    isEditingNote = false
                  },
                  icon = {
                    Icon(
                      imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                      contentDescription = destination.label
                    )
                  },
                  label = { Text(destination.label) },
                  modifier = Modifier.testTag("nav_item_${destination.name.lowercase()}")
                )
              }
            }
          }
        }
      ) { innerPadding ->
        Row(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
          // Navigation Rail for Tablet / Foldable / Desktop expanded view
          if (isExpanded) {
            NavigationRail(
              modifier = Modifier.testTag("navigation_rail"),
              containerColor = MaterialTheme.colorScheme.surface
            ) {
              MainDestination.values().forEach { destination ->
                val isSelected = currentDestination == destination
                NavigationRailItem(
                  selected = isSelected,
                  onClick = {
                    currentDestination = destination
                  },
                  icon = {
                    Icon(
                      imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                      contentDescription = destination.label
                    )
                  },
                  label = { Text(destination.label) },
                  modifier = Modifier.testTag("rail_item_${destination.name.lowercase()}")
                )
              }
            }
          }

          // Main Screen Area
          Box(modifier = Modifier.weight(1f)) {
            if (isExpanded && isEditingNote) {
              // Dual pane / side-by-side on wide screens: Left list, Right editor
              Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.width(360.dp)) {
                  NotesDashboardScreen(
                    viewModel = viewModel,
                    onOpenNote = { note ->
                      viewModel.openExistingNote(note)
                      isEditingNote = true
                    },
                    onCreateNote = {
                      viewModel.openNewNote()
                      isEditingNote = true
                    }
                  )
                }

                Box(modifier = Modifier.weight(1f)) {
                  NoteEditorScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                      isEditingNote = false
                    }
                  )
                }
              }
            } else {
              AnimatedContent(
                targetState = currentDestination,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
              ) { destination ->
                when (destination) {
                  MainDestination.NOTES -> {
                    NotesDashboardScreen(
                      viewModel = viewModel,
                      onOpenNote = { note ->
                        viewModel.openExistingNote(note)
                        isEditingNote = true
                      },
                      onCreateNote = {
                        viewModel.openNewNote()
                        isEditingNote = true
                      }
                    )
                  }
                  MainDestination.FAVORITES -> {
                    FavoritesScreen(
                      viewModel = viewModel,
                      onOpenNote = { note ->
                        viewModel.openExistingNote(note)
                        isEditingNote = true
                      }
                    )
                  }
                  MainDestination.TRASH -> {
                    TrashScreen(viewModel = viewModel)
                  }
                  MainDestination.SETTINGS -> {
                    SettingsScreen(viewModel = viewModel)
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
