package com.afquintana.weightcontroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.afquintana.weightcontroller.ui.navigation.WeightControllerNavHost
import com.afquintana.weightcontroller.ui.theme.WeightControllerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeightControllerTheme {
                WeightControllerNavHost()
            }
        }
    }
}
