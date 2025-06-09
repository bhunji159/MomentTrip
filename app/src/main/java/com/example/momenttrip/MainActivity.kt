package com.example.momenttrip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.momenttrip.ui.screen.login.LoginScreen
import com.example.momenttrip.ui.theme.MomentTripTheme
import com.example.momenttrip.ui_screen.ExpenseScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MomentTripTheme {
               AppEntryPoint()
            }
        }
    }
}

