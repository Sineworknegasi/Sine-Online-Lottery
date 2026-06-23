package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.data.LotteryDatabase
import com.example.data.LotteryRepository
import com.example.ui.LotteryAppMainScreen
import com.example.ui.LotteryViewModel
import com.example.ui.LotteryViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup SQLite Room Database holder locally on device
        val database = LotteryDatabase.getDatabase(applicationContext)
        val repository = LotteryRepository(database.lotteryDao)
        
        // Instantiate the local state ViewModel via constructor injection factory
        val viewModel: LotteryViewModel by viewModels {
            LotteryViewModelFactory(application, repository)
        }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Root app layout shell with safety edge-top/bottom
                    LotteryAppMainScreen(viewModel = viewModel)
                }
            }
        }
    }
}
