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

private val DarkColorScheme = darkColorScheme(
  primary = IndigoPrimary,
  onPrimary = IndigoOnPrimary,
  primaryContainer = Color(0xFF3730A3),
  onPrimaryContainer = Color(0xFFE0E7FF),
  secondary = AmberSecondary,
  onSecondary = AmberOnSecondary,
  secondaryContainer = Color(0xFF92400E),
  onSecondaryContainer = Color(0xFFFEF3C7),
  tertiary = TealTertiary,
  background = SlateDarkBackground,
  surface = SlateDarkSurface,
  surfaceVariant = SlateDarkContainer,
  onBackground = Color(0xFFF8FAFC),
  onSurface = Color(0xFFF8FAFC),
  onSurfaceVariant = Color(0xFF94A3B8)
)

private val LightColorScheme = lightColorScheme(
  primary = IndigoPrimary,
  onPrimary = IndigoOnPrimary,
  primaryContainer = IndigoPrimaryContainer,
  onPrimaryContainer = IndigoOnPrimaryContainer,
  secondary = AmberSecondary,
  onSecondary = AmberOnSecondary,
  secondaryContainer = AmberSecondaryContainer,
  onSecondaryContainer = AmberOnSecondaryContainer,
  tertiary = TealTertiary,
  background = SlateLightBackground,
  surface = SlateLightSurface,
  surfaceVariant = SlateLightContainer,
  onBackground = Color(0xFF0F172A),
  onSurface = Color(0xFF0F172A),
  onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
