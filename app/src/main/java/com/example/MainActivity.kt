package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.HomeView
import com.example.ui.OttViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Construct OTT ViewModel using standard AndroidViewModel factory
    val viewModel = ViewModelProvider(this)[OttViewModel::class.java]

    setContent {
      MyApplicationTheme(dynamicColor = false) {
        HomeView(viewModel = viewModel)
      }
    }
  }
}
