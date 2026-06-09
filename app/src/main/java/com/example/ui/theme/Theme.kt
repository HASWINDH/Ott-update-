package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = OttRed,
    secondary = OttGold,
    tertiary = OttGold,
    background = OttBackground,
    surface = OttSurface,
    surfaceVariant = OttSurfaceVariant,
    onPrimary = TextWhite,
    onSecondary = OttBackground,
    onTertiary = OttBackground,
    onBackground = TextWhite,
    onSurface = TextWhite,
    onSurfaceVariant = TextLightGray
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // We enforce dark theme for the cinematic experience
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve our cinematic brand colors
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
