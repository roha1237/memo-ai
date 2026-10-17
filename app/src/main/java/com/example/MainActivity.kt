package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.MemoApp
import com.example.ui.theme.MemoTheme
import com.example.viewmodel.NotesViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val notesViewModel: NotesViewModel = viewModel()
      val themeMode by notesViewModel.themeMode.collectAsStateWithLifecycle()

      MemoTheme(themeMode = themeMode) {
        Surface(modifier = Modifier.fillMaxSize()) {
          MemoApp(viewModel = notesViewModel)
        }
      }
    }
  }
}
