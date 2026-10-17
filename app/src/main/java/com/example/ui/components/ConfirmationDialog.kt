package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.RoseError

@Composable
fun ConfirmationDialog(
  title: String,
  message: String,
  confirmButtonText: String = "Confirm",
  dismissButtonText: String = "Cancel",
  icon: ImageVector = Icons.Default.WarningAmber,
  isDestructive: Boolean = false,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  AlertDialog(
    modifier = modifier.testTag("confirmation_dialog"),
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isDestructive) RoseError else MaterialTheme.colorScheme.primary
      )
    },
    title = {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium
      )
    },
    text = {
      Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        colors = if (isDestructive) {
          ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        } else {
          ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        },
        modifier = Modifier.testTag("confirm_action_button")
      ) {
        Text(confirmButtonText)
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("dismiss_action_button")
      ) {
        Text(dismissButtonText)
      }
    }
  )
}
