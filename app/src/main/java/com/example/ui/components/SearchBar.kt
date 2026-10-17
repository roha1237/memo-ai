package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.SortOption
import com.example.data.preferences.ViewLayout

@Composable
fun NotesSearchBar(
  query: String,
  onQueryChange: (String) -> Unit,
  viewLayout: ViewLayout,
  onToggleViewLayout: () -> Unit,
  currentSort: SortOption,
  onSortChange: (SortOption) -> Unit,
  modifier: Modifier = Modifier
) {
  var showSortMenu by remember { mutableStateOf(false) }

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    tonalElevation = 2.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Search,
        contentDescription = "Search icon",
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(20.dp)
      )

      Spacer(modifier = Modifier.width(8.dp))

      Box(
        modifier = Modifier.weight(1f),
        contentAlignment = Alignment.CenterStart
      ) {
        if (query.isEmpty()) {
          Text(
            text = "Search notes, tags, or topics...",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
          )
        }
        BasicTextField(
          value = query,
          onValueChange = onQueryChange,
          singleLine = true,
          textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp
          ),
          cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_text_input")
        )
      }

      if (query.isNotEmpty()) {
        IconButton(
          onClick = { onQueryChange("") },
          modifier = Modifier
            .size(36.dp)
            .testTag("clear_search_button")
        ) {
          Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = "Clear search",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      // View layout toggle (Grid vs List)
      IconButton(
        onClick = onToggleViewLayout,
        modifier = Modifier
          .size(36.dp)
          .testTag("view_layout_toggle")
      ) {
        Icon(
          imageVector = if (viewLayout == ViewLayout.STAGGERED_GRID) {
            Icons.AutoMirrored.Filled.ViewList
          } else {
            Icons.Default.GridView
          },
          contentDescription = "Toggle list or grid layout",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp)
        )
      }

      // Sort Menu
      Box {
        IconButton(
          onClick = { showSortMenu = true },
          modifier = Modifier
            .size(36.dp)
            .testTag("sort_menu_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Sort,
            contentDescription = "Sort notes",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
          )
        }

        DropdownMenu(
          expanded = showSortMenu,
          onDismissRequest = { showSortMenu = false }
        ) {
          DropdownMenuItem(
            text = { Text("Last Modified (Newest)") },
            onClick = {
              onSortChange(SortOption.MODIFIED_DESC)
              showSortMenu = false
            }
          )
          DropdownMenuItem(
            text = { Text("Last Modified (Oldest)") },
            onClick = {
              onSortChange(SortOption.MODIFIED_ASC)
              showSortMenu = false
            }
          )
          DropdownMenuItem(
            text = { Text("Created Date") },
            onClick = {
              onSortChange(SortOption.CREATED_DESC)
              showSortMenu = false
            }
          )
          DropdownMenuItem(
            text = { Text("Title (A–Z)") },
            onClick = {
              onSortChange(SortOption.TITLE_ASC)
              showSortMenu = false
            }
          )
        }
      }
    }
  }
}
