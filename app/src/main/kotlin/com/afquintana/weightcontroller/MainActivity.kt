package com.afquintana.weightcontroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.afquintana.weightcontroller.ui.navigation.WeightControllerNavHost
import com.afquintana.weightcontroller.ui.theme.WeightControllerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as WeightControllerApp
        setContent {
            WeightControllerTheme {
                WeightControllerNavHost(appGraph = app.appGraph)
            }
        }
    }
}
