package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.NoteEntity
import com.example.ui.model.NOTE_COLORS
import com.example.ui.model.parseTags
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.IndigoLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteCard(
  note: NoteEntity,
  onClick: () -> Unit,
  onTogglePin: () -> Unit,
  onToggleFavorite: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
  isTrashMode: Boolean = false,
  onRestore: (() -> Unit)? = null,
  onPermanentDelete: (() -> Unit)? = null
) {
  val noteColorOption = remember(note.colorHex) {
    NOTE_COLORS.find { it.hex == note.colorHex } ?: NOTE_COLORS.first()
  }

  val formattedDate = remember(note.updatedAt) {
    val date = Date(note.updatedAt)
    val now = Date()
    val diff = now.time - date.time
    when {
      diff < 60_000 -> "Just now"
      diff < 3600_000 -> "${diff / 60_000}m ago"
      diff < 86400_000 -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
      else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
    }
  }

  val tagsList = remember(note.tags) { parseTags(note.tags) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("note_card_${note.id}")
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (noteColorOption.tintColor != Color.Transparent) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
      } else {
        MaterialTheme.colorScheme.surface
      }
    ),
    border = BorderStroke(
      width = if (note.isPinned) 1.5.dp else 1.dp,
      color = if (note.isPinned) {
        IndigoLight
      } else if (noteColorOption.borderColor != Color(0xFF475569)) {
        noteColorOption.borderColor.copy(alpha = 0.4f)
      } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
      }
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .background(noteColorOption.tintColor)
        .padding(14.dp)
    ) {
      // Top row: Category badge & Pinned/Favorite indicators
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Category Pill
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.testTag("category_pill_${note.id}")
        ) {
          Text(
            text = note.category.ifBlank { "General" },
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (!isTrashMode) {
            // Pin Toggle Button
            IconButton(
              onClick = onTogglePin,
              modifier = Modifier
                .size(36.dp)
                .testTag("pin_button_${note.id}")
            ) {
              Icon(
                imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                contentDescription = if (note.isPinned) "Unpin note" else "Pin note",
                tint = if (note.isPinned) IndigoLight else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
            }

            // Favorite Toggle Button
            IconButton(
              onClick = onToggleFavorite,
              modifier = Modifier
                .size(36.dp)
                .testTag("favorite_button_${note.id}")
            ) {
              Icon(
                imageVector = if (note.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = if (note.isFavorite) "Remove from favorites" else "Add to favorites",
                tint = if (note.isFavorite) AmberWarning else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Note Title
      Text(
        text = note.title.ifBlank { "Untitled Note" },
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = if (note.title.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      if (note.content.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = note.content,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 4,
          overflow = TextOverflow.Ellipsis,
          lineHeight = 20.sp
        )
      }

      // Tags Row
      if (tagsList.isNotEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          tagsList.take(4).forEach { tag ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "#$tag",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
          if (tagsList.size > 4) {
            Text(
              text = "+${tagsList.size - 4}",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(vertical = 2.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Bottom Row: Date & Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = formattedDate,
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        if (!isTrashMode) {
          IconButton(
            onClick = onDelete,
            modifier = Modifier
              .size(36.dp)
              .testTag("delete_button_${note.id}")
          ) {
            Icon(
              imageVector = Icons.Filled.Delete,
              contentDescription = "Move note to trash",
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
              modifier = Modifier.size(17.dp)
            )
          }
        } else {
          Row {
            if (onRestore != null) {
              IconButton(
                onClick = onRestore,
                modifier = Modifier
                  .size(36.dp)
                  .testTag("restore_button_${note.id}")
              ) {
                Icon(
                  imageVector = Icons.Filled.Bookmark,
                  contentDescription = "Restore note",
                  tint = IndigoLight,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
            if (onPermanentDelete != null) {
              IconButton(
                onClick = onPermanentDelete,
                modifier = Modifier
                  .size(36.dp)
                  .testTag("perm_delete_button_${note.id}")
              ) {
                Icon(
                  imageVector = Icons.Filled.Delete,
                  contentDescription = "Delete permanently",
                  tint = MaterialTheme.colorScheme.error,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}
