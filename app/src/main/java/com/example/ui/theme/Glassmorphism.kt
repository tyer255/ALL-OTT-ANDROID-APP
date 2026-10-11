package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Premium 2026 Liquid Glass & Neumorphic design tokens.
 * Crafted with natural contrast, subtle refractive depth, and restrained translucency.
 * Avoids harsh neon or artificial AI gradients.
 */
object GlassTokens {
  // Translucent dark glass layers
  val GlassBackgroundDark = Color(0xCC0D1017) // 80% opacity dark obsidian
  val GlassBackgroundElevated = Color(0xD9141924) // 85% opacity elevated
  val GlassBackgroundCard = Color(0xB3181E2C) // 70% opacity card surface
  val GlassBackgroundPressed = Color(0xE60E121B) // 90% opacity pressed

  // Dual-stroke refractive glass borders
  val BorderHighlight = Color(0x33FFFFFF) // Subtle top/left light bounce
  val BorderMuted = Color(0x1AFFFFFF) // General edge definition
  val BorderShadow = Color(0x0F000000) // Deep contrast bevel
  val BorderNeumorphicLight = Color(0x2EFFFFFF)
  val BorderNeumorphicDark = Color(0x59000000)

  // Subtle ambient shadow colors for depth
  val ShadowAmbient = Color(0x59000000)
  val ShadowSpot = Color(0x80000000)

  // Status and accent colors in glass tones
  val OnlineGreenGlass = Color(0x2610B981)
  val OnlineGreenText = Color(0xFF34D399)
  val WarningAmberGlass = Color(0x26F59E0B)
  val WarningAmberText = Color(0xFFFBBF24)
}

/**
 * Creates a translucent Liquid Glass surface with dual-stop vertical gradient,
 * subtle atmospheric depth, and a refined border stroke.
 */
fun Modifier.liquidGlassSurface(
  shape: Shape = RoundedCornerShape(16.dp),
  elevation: Dp = 4.dp,
  backgroundColor: Color = GlassTokens.GlassBackgroundCard,
  borderColor: Color = GlassTokens.BorderHighlight,
  borderWidth: Dp = 1.dp
): Modifier = this
  .then(
    if (elevation > 0.dp) {
      Modifier.shadow(
        elevation = elevation,
        shape = shape,
        clip = false,
        ambientColor = GlassTokens.ShadowAmbient,
        spotColor = GlassTokens.ShadowSpot
      )
    } else Modifier
  )
  .clip(shape)
  .background(
    brush = Brush.verticalGradient(
      colors = listOf(
        backgroundColor.copy(alpha = (backgroundColor.alpha * 1.15f).coerceAtMost(1f)),
        backgroundColor.copy(alpha = (backgroundColor.alpha * 0.90f))
      )
    )
  )
  .border(
    width = borderWidth,
    brush = Brush.linearGradient(
      listOf(
        borderColor,
        GlassTokens.BorderMuted,
        borderColor.copy(alpha = 0.15f)
      )
    ),
    shape = shape
  )

/**
 * Creates a subtle Neumorphic depth effect with soft outer elevation and dual-tone bevel.
 * Ideal for buttons, cards, and interactive pills.
 */
fun Modifier.neumorphicDepth(
  shape: Shape = RoundedCornerShape(12.dp),
  elevation: Dp = 3.dp,
  isPressed: Boolean = false,
  baseColor: Color = Color(0xFF161B26)
): Modifier {
  val surfaceColor = if (isPressed) baseColor.copy(alpha = 0.85f) else baseColor
  val actualElevation = if (isPressed) 0.dp else elevation

  return this
    .then(
      if (actualElevation > 0.dp) {
        Modifier.shadow(
          elevation = actualElevation,
          shape = shape,
          ambientColor = Color(0x40000000),
          spotColor = Color(0x66000000)
        )
      } else Modifier
    )
    .clip(shape)
    .background(surfaceColor)
    .border(
      width = 0.8.dp,
      brush = Brush.linearGradient(
        listOf(
          if (isPressed) Color(0x14FFFFFF) else Color(0x33FFFFFF),
          if (isPressed) Color(0x29000000) else Color(0x14000000)
        )
      ),
      shape = shape
    )
}
