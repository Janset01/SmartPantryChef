package com.smartpantry.chef

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.smartpantry.chef.ui.MainScreen
import com.smartpantry.chef.data.DemoContentSeeder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import com.smartpantry.chef.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                var demoReady by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    try {
                        withContext(Dispatchers.IO) {
                            DemoContentSeeder.ensureInstalled(this@MainActivity)
                        }
                    } catch (e: Exception) {
                        Log.e("SmartPantryDemo", "Sample content could not be loaded", e)
                    }
                    demoReady = true
                }
                if (demoReady) MainScreen()
                else Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}