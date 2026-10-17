package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ViewLayout {
  STAGGERED_GRID,
  LINEAR_LIST
}

enum class SortOption {
  MODIFIED_DESC,
  MODIFIED_ASC,
  CREATED_DESC,
  TITLE_ASC
}

class UserPreferences(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("memo_ai_prefs", Context.MODE_PRIVATE)

  private val _themeMode = MutableStateFlow(loadThemeMode())
  val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

  private val _viewLayout = MutableStateFlow(loadViewLayout())
  val viewLayout: StateFlow<ViewLayout> = _viewLayout.asStateFlow()

  private val _sortOption = MutableStateFlow(loadSortOption())
  val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

  private fun loadThemeMode(): ThemeMode {
    val name = prefs.getString("theme_mode", ThemeMode.DARK.name) ?: ThemeMode.DARK.name
    return try {
      ThemeMode.valueOf(name)
    } catch (e: Exception) {
      ThemeMode.DARK
    }
  }

  fun setThemeMode(mode: ThemeMode) {
    prefs.edit().putString("theme_mode", mode.name).apply()
    _themeMode.value = mode
  }

  private fun loadViewLayout(): ViewLayout {
    val name = prefs.getString("view_layout", ViewLayout.STAGGERED_GRID.name) ?: ViewLayout.STAGGERED_GRID.name
    return try {
      ViewLayout.valueOf(name)
    } catch (e: Exception) {
      ViewLayout.STAGGERED_GRID
    }
  }

  fun setViewLayout(layout: ViewLayout) {
    prefs.edit().putString("view_layout", layout.name).apply()
    _viewLayout.value = layout
  }

  private fun loadSortOption(): SortOption {
    val name = prefs.getString("sort_option", SortOption.MODIFIED_DESC.name) ?: SortOption.MODIFIED_DESC.name
    return try {
      SortOption.valueOf(name)
    } catch (e: Exception) {
      SortOption.MODIFIED_DESC
    }
  }

  fun setSortOption(option: SortOption) {
    prefs.edit().putString("sort_option", option.name).apply()
    _sortOption.value = option
  }
}
