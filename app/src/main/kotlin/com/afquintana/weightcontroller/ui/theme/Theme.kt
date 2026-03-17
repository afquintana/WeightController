package com.afquintana.weightcontroller.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(primary = BrandPrimary, secondary = BrandSecondary, tertiary = BrandTertiary)
private val DarkColors = darkColorScheme(primary = BrandPrimary, secondary = BrandSecondary, tertiary = BrandTertiary)

@Composable
fun WeightControllerTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
}
