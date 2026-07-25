package com.fieldministry.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fieldministry.app.di.ServiceLocator
import com.fieldministry.app.ui.navigation.AppNavGraph
import com.fieldministry.app.ui.theme.FieldMinistryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FieldMinistryTheme {
                AppNavGraph(sessionManager = ServiceLocator.sessionManager)
            }
        }
    }
}
