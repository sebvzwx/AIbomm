package com.example.aibomm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aibomm.ui.screens.HubScreen
import com.example.aibomm.ui.theme.AIBommTheme
import com.example.aibomm.viewmodel.TodoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AIBommTheme {
                App()
            }
        }
    }
}

@Composable
private fun App() {
    val viewModel: TodoViewModel = viewModel()
    HubScreen(viewModel = viewModel)
}
