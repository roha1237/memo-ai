package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.backup.ImportResult
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.ThemeMode
import com.example.viewmodel.NotesViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
  viewModel: NotesViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
  val activeNotes by viewModel.activeNotesRaw.collectAsStateWithLifecycle(emptyList())
  val favoriteNotes by viewModel.favoriteNotesRaw.collectAsStateWithLifecycle(emptyList())
  val trashNotes by viewModel.trashNotes.collectAsStateWithLifecycle()

  var showExportDialog by remember { mutableStateOf(false) }
  var exportedJsonText by remember { mutableStateOf("") }
  var showImportDialog by remember { mutableStateOf(false) }
  var importJsonInput by remember { mutableStateOf("") }
  var importFeedbackMessage by remember { mutableStateOf<String?>(null) }

  val isAiConfigured = remember { viewModel.aiService.isConfigured() }

  Scaffold(
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp)
        .verticalScroll(rememberScrollState())
    ) {
      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Settings & Backup",
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "Preferences, data portability, and AI configuration",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Theme Appearance Section
      SettingsSectionHeader(title = "Appearance", icon = Icons.Default.DarkMode)
      Spacer(modifier = Modifier.height(8.dp))

      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Theme Mode",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Dark mode is active by default to protect your eyes and reduce glare.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            ThemeMode.values().forEach { mode ->
              val isSelected = themeMode == mode
              FilterChip(
                selected = isSelected,
                onClick = { viewModel.setTheme(mode) },
                label = {
                  Text(
                    text = when (mode) {
                      ThemeMode.DARK -> "Dark"
                      ThemeMode.LIGHT -> "Light"
                      ThemeMode.SYSTEM -> "System"
                    }
                  )
                },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                  selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("theme_chip_${mode.name.lowercase()}")
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // AI Service Status Section
      SettingsSectionHeader(title = "AI Integration Architecture", icon = Icons.Default.AutoAwesome)
      Spacer(modifier = Modifier.height(8.dp))

      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Gemini AI Engine",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isAiConfigured) EmeraldSuccess.copy(alpha = 0.15f) else AmberWarning.copy(alpha = 0.15f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = if (isAiConfigured) Icons.Default.CheckCircle else Icons.Default.KeyOff,
                  contentDescription = null,
                  tint = if (isAiConfigured) EmeraldSuccess else AmberWarning,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (isAiConfigured) "Connected" else "Not Configured",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isAiConfigured) EmeraldSuccess else AmberWarning
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = if (isAiConfigured) {
              "Real Gemini API is connected and active. You can summarize notes, polish writing, and extract checklists directly from the note editor."
            } else {
              "MemoAI follows a strict zero-mock policy. AI features are disabled until an API key is provided.\n\nTo activate live AI, open the Secrets panel in AI Studio and configure your GEMINI_API_KEY. It is securely injected via BuildConfig without being exposed in frontend code."
            },
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Data Backup & Restore Section
      SettingsSectionHeader(title = "Data Portability & Backup", icon = Icons.Default.Storage)
      Spacer(modifier = Modifier.height(8.dp))

      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "JSON Export & Import",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Export your entire notes archive to a standard JSON format, or restore from an earlier backup.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = {
                coroutineScope.launch {
                  exportedJsonText = viewModel.exportNotesJson()
                  showExportDialog = true
                }
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("export_notes_button")
            ) {
              Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Export JSON")
            }

            OutlinedButton(
              onClick = {
                importFeedbackMessage = null
                importJsonInput = ""
                showImportDialog = true
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("import_notes_button")
            ) {
              Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Import JSON")
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // App Statistics Card
      SettingsSectionHeader(title = "Notebook Statistics", icon = Icons.Default.Info)
      Spacer(modifier = Modifier.height(8.dp))

      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            StatItem(label = "Active Notes", count = activeNotes.size)
            StatItem(label = "Pinned", count = activeNotes.count { it.isPinned })
            StatItem(label = "Favorites", count = favoriteNotes.size)
            StatItem(label = "In Trash", count = trashNotes.size)
          }

          Spacer(modifier = Modifier.height(14.dp))

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "MemoAI • Version 1.0 • Built with Kotlin & Jetpack Compose\nAll data is stored locally in Room SQLite database.",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(10.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(36.dp))
    }
  }

  // Export Dialog
  if (showExportDialog) {
    AlertDialog(
      onDismissRequest = { showExportDialog = false },
      title = { Text("Notes Backup JSON") },
      text = {
        Column {
          Text(
            text = "Here is your exported notes archive. You can copy it to clipboard or share it to another app.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp)
              .verticalScroll(rememberScrollState())
          ) {
            Text(
              text = exportedJsonText,
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              modifier = Modifier.padding(10.dp)
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val sendIntent = Intent().apply {
              action = Intent.ACTION_SEND
              putExtra(Intent.EXTRA_TEXT, exportedJsonText)
              type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share MemoAI Backup"))
            showExportDialog = false
          }
        ) {
          Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Share")
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("MemoAI Backup", exportedJsonText)
            clipboard.setPrimaryClip(clip)
            showExportDialog = false
          }
        ) {
          Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Copy")
        }
      }
    )
  }

  // Import Dialog
  if (showImportDialog) {
    AlertDialog(
      onDismissRequest = { showImportDialog = false },
      title = { Text("Import Notes JSON") },
      text = {
        Column {
          Text(
            text = "Paste a valid MemoAI JSON backup below. Notes will be safely merged into your notebook.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = importJsonInput,
            onValueChange = {
              importJsonInput = it
              importFeedbackMessage = null
            },
            placeholder = { Text("Paste JSON here...", style = MaterialTheme.typography.bodySmall) },
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp)
              .testTag("import_json_input"),
            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
          )

          if (importFeedbackMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = importFeedbackMessage!!,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.error
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.importNotesJson(importJsonInput) { result ->
              when (result) {
                is ImportResult.Success -> {
                  showImportDialog = false
                }
                is ImportResult.Error -> {
                  importFeedbackMessage = result.message
                }
              }
            }
          },
          modifier = Modifier.testTag("confirm_import_button")
        ) {
          Text("Import")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showImportDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = IndigoLight,
      modifier = Modifier.size(18.dp)
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
private fun StatItem(label: String, count: Int) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = count.toString(),
      style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.primary
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}
