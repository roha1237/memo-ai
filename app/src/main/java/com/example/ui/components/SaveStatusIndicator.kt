package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.viewmodel.SaveStatus

@Composable
fun SaveStatusIndicator(
  saveStatus: SaveStatus,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .testTag("save_status_indicator")
      .background(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(12.dp)
      )
      .padding(horizontal = 10.dp, vertical = 4.dp)
  ) {
    AnimatedContent(
      targetState = saveStatus,
      transitionSpec = { fadeIn() togetherWith fadeOut() },
      label = "SaveStatusAnimation"
    ) { status ->
      Row(verticalAlignment = Alignment.CenterVertically) {
        when (status) {
          SaveStatus.SAVING -> {
            CircularProgressIndicator(
              modifier = Modifier.size(12.dp),
              strokeWidth = 2.dp,
              color = IndigoLight
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Saving...",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          SaveStatus.SAVED -> {
            Icon(
              imageVector = Icons.Rounded.CloudDone,
              contentDescription = "Saved to storage",
              modifier = Modifier.size(14.dp),
              tint = EmeraldSuccess
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Saved",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
              color = EmeraldSuccess
            )
          }
          SaveStatus.IDLE -> {
            Icon(
              imageVector = Icons.Rounded.Check,
              contentDescription = "Ready",
              modifier = Modifier.size(14.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Ready",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}
