package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NetflixDarkColorScheme = darkColorScheme(
  primary = NetflixRed,
  onPrimary = CinemaWhite,
  primaryContainer = NetflixRedDark,
  onPrimaryContainer = CinemaWhite,
  secondary = TextMuted,
  onSecondary = CinemaWhite,
  secondaryContainer = NetflixCardSurface,
  onSecondaryContainer = CinemaWhite,
  tertiary = GoldAccent,
  onTertiary = NetflixBlack,
  background = NetflixBlack,
  onBackground = CinemaWhite,
  surface = NetflixDarkGrey,
  onSurface = CinemaWhite,
  surfaceVariant = NetflixCardSurface,
  onSurfaceVariant = TextMuted,
  outline = NetflixBorder
)

@Composable
fun AetherStreamTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = NetflixDarkColorScheme,
    typography = Typography,
    content = content
  )
}
