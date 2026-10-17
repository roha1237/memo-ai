package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainDestination(
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector
) {
  NOTES("Notes", Icons.Filled.Description, Icons.Outlined.Description),
  FAVORITES("Favorites", Icons.Filled.Star, Icons.Outlined.StarBorder),
  TRASH("Trash", Icons.Filled.Delete, Icons.Outlined.Delete),
  SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}
