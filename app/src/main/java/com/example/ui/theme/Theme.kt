package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val MahiDarkColorScheme = darkColorScheme(
  primary = MahiPrimary,
  onPrimary = Color.White,
  primaryContainer = MahiSurfaceVariant,
  onPrimaryContainer = MahiPrimaryLight,
  secondary = MahiSecondary,
  onSecondary = Color.Black,
  secondaryContainer = Color(0xFF0E3A42),
  onSecondaryContainer = Color(0xFFA5F3FC),
  tertiary = MahiTertiary,
  onTertiary = Color.White,
  background = MahiBackground,
  onBackground = MahiTextPrimary,
  surface = MahiSurface,
  onSurface = MahiTextPrimary,
  surfaceVariant = MahiSurfaceVariant,
  onSurfaceVariant = MahiTextSecondary,
  outline = MahiCardBorder
)

private val MahiLightColorScheme = lightColorScheme(
  primary = Color(0xFF6D28D9),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFEDE9FE),
  onPrimaryContainer = Color(0xFF4C1D95),
  secondary = Color(0xFF0891B2),
  onSecondary = Color.White,
  tertiary = Color(0xFFDB2777),
  background = Color(0xFFF8FAFC),
  surface = Color(0xFFFFFFFF),
  onBackground = Color(0xFF0F172A),
  onSurface = Color(0xFF0F172A),
  surfaceVariant = Color(0xFFF1F5F9),
  onSurfaceVariant = Color(0xFF475569),
  outline = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to dark immersive assistant theme
  dynamicColor: Boolean = false, // Keep Mahi branded theme consistent
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) MahiDarkColorScheme else MahiLightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
