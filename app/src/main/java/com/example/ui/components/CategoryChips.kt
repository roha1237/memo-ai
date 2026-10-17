package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.PRESET_CATEGORIES
import com.example.ui.theme.IndigoLight

@Composable
fun CategoryFilterRow(
  selectedCategory: String,
  onCategorySelect: (String) -> Unit,
  categoryCounts: Map<String, Int>,
  allTags: List<String>,
  selectedTag: String?,
  onTagSelect: (String?) -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()
  val categories = listOf("All") + PRESET_CATEGORIES

  Column(modifier = modifier.fillMaxWidth()) {
    // Categories Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(scrollState),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      categories.forEach { category ->
        val count = categoryCounts[category] ?: 0
        val isSelected = selectedCategory.equals(category, ignoreCase = true)

        FilterChip(
          selected = isSelected,
          onClick = { onCategorySelect(category) },
          label = {
            Text(
              text = if (count > 0) "$category ($count)" else category,
              style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp)
            )
          },
          shape = RoundedCornerShape(12.dp),
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
          ),
          modifier = Modifier.testTag("filter_chip_$category")
        )
      }
    }

    // Secondary Tags Row (if any tags exist)
    if (allTags.isNotEmpty()) {
      Spacer(modifier = Modifier.height(4.dp))
      val tagScrollState = rememberScrollState()
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(tagScrollState),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        allTags.forEach { tag ->
          val isTagSelected = selectedTag == tag
          FilterChip(
            selected = isTagSelected,
            onClick = {
              if (isTagSelected) onTagSelect(null) else onTagSelect(tag)
            },
            label = {
              Text(
                text = "#$tag",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
              )
            },
            shape = RoundedCornerShape(8.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = IndigoLight.copy(alpha = 0.25f),
              selectedLabelColor = IndigoLight,
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
              labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("tag_chip_$tag")
          )
        }
      }
    }
  }
}
