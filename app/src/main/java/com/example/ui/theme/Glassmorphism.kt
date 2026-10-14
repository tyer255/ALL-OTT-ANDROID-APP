package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

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

  // Cached static brushes to avoid allocations during list scrolling
  val DefaultCardGradient = Brush.verticalGradient(
    listOf(
      Color(0xCD181E2C),
      Color(0xA1181E2C)
    )
  )
  val DefaultBorderGradient = Brush.linearGradient(
    listOf(
      BorderHighlight,
      BorderMuted,
      Color(0x08FFFFFF)
    )
  )
}

/**
 * Creates a translucent Liquid Glass surface with dual-stop vertical gradient,
 * subtle atmospheric depth, and a refined border stroke.
 * Optimized with cached brushes for silky 60/120fps scrolling.
 */
fun Modifier.liquidGlassSurface(
  shape: Shape = RoundedCornerShape(16.dp),
  elevation: Dp = 4.dp,
  backgroundColor: Color = GlassTokens.GlassBackgroundCard,
  borderColor: Color = GlassTokens.BorderHighlight,
  borderWidth: Dp = 1.dp
): Modifier {
  val backgroundBrush = if (backgroundColor == GlassTokens.GlassBackgroundCard) {
    GlassTokens.DefaultCardGradient
  } else {
    Brush.verticalGradient(
      colors = listOf(
        backgroundColor.copy(alpha = (backgroundColor.alpha * 1.15f).coerceAtMost(1f)),
        backgroundColor.copy(alpha = (backgroundColor.alpha * 0.90f))
      )
    )
  }

  val borderBrush = if (borderColor == GlassTokens.BorderHighlight) {
    GlassTokens.DefaultBorderGradient
  } else {
    Brush.linearGradient(
      listOf(
        borderColor,
        GlassTokens.BorderMuted,
        borderColor.copy(alpha = 0.15f)
      )
    )
  }

  return this
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
    .background(brush = backgroundBrush)
    .border(
      width = borderWidth,
      brush = borderBrush,
      shape = shape
    )
}

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

/**
 * Interactive Neumorphic Button Modifier with tactile scale depression,
 * soft shadow depth, and refined bevel highlight.
 */
fun Modifier.neumorphicButton(
  shape: Shape = RoundedCornerShape(14.dp),
  elevation: Dp = 3.dp,
  isPressed: Boolean = false,
  baseColor: Color = Color(0xFF161C28)
): Modifier = this
  .scale(if (isPressed) 0.97f else 1.0f)
  .neumorphicDepth(
    shape = shape,
    elevation = if (isPressed) 0.dp else elevation,
    isPressed = isPressed,
    baseColor = baseColor
  )

/**
 * Premium 2026 Custom Neumorphic Switch.
 * Features realistic depth, smooth animated thumb movement, tactile inner track shadow,
 * and subtle status accent glow.
 */
@Composable
fun ModernSwitch(
  checked: Boolean,
  onCheckedChange: ((Boolean) -> Unit)?,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  activeTrackColor: Color = AmberGoldPrimary,
  inactiveTrackColor: Color = Color(0xFF121622)
) {
  val thumbOffset by animateDpAsState(
    targetValue = if (checked) 22.dp else 2.dp,
    animationSpec = spring(stiffness = 500f, dampingRatio = 0.75f),
    label = "switch_thumb_offset"
  )

  val trackColor by animateColorAsState(
    targetValue = if (checked) activeTrackColor.copy(alpha = 0.35f) else inactiveTrackColor,
    animationSpec = tween(200),
    label = "switch_track_color"
  )

  val trackBorderColor by animateColorAsState(
    targetValue = if (checked) activeTrackColor.copy(alpha = 0.7f) else Color(0x33FFFFFF),
    animationSpec = tween(200),
    label = "switch_track_border"
  )

  val thumbColor by animateColorAsState(
    targetValue = if (checked) activeTrackColor else CinemaWhite,
    animationSpec = tween(200),
    label = "switch_thumb_color"
  )

  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  // Outer container enforcing minimum 48dp touch target
  Box(
    modifier = modifier
      .size(width = 56.dp, height = 48.dp)
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        onClick = { onCheckedChange?.invoke(!checked) }
      ),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .width(52.dp)
        .height(30.dp)
        .clip(RoundedCornerShape(15.dp))
        .background(trackColor)
        .border(0.8.dp, trackBorderColor, RoundedCornerShape(15.dp))
        .padding(vertical = 2.dp),
      contentAlignment = Alignment.CenterStart
    ) {
      // Tactile Refined Thumb with Shadow & Bevel Rim
      Box(
        modifier = Modifier
          .offset(x = thumbOffset)
          .size(if (isPressed) 24.dp else 22.dp)
          .shadow(
            elevation = if (checked) 4.dp else 2.dp,
            shape = CircleShape,
            spotColor = if (checked) activeTrackColor.copy(alpha = 0.5f) else Color(0x66000000)
          )
          .clip(CircleShape)
          .background(
            brush = Brush.verticalGradient(
              listOf(
                thumbColor,
                thumbColor.copy(alpha = 0.88f)
              )
            )
          )
          .border(0.8.dp, Color(0x4DFFFFFF), CircleShape)
      )
    }
  }
}

/**
 * Premium 2026 Tactile OTT Slider / Seekbar.
 * Features a refined thumb with soft drop shadow & highlight ring on drag,
 * track depth with active gradient fill, value popup, and >= 48dp touch target.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSlider(
  value: Float,
  onValueChange: (Float) -> Unit,
  modifier: Modifier = Modifier,
  valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
  steps: Int = 0,
  activeColor: Color = AmberGoldPrimary,
  inactiveColor: Color = Color(0xFF202636),
  valueFormatter: ((Float) -> String)? = null
) {
  Column(modifier = modifier.fillMaxWidth()) {
    if (valueFormatter != null) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.End
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0x331E2638))
            .border(0.5.dp, GlassTokens.BorderHighlight, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Text(
            text = valueFormatter(value),
            color = CinemaWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    Slider(
      value = value,
      onValueChange = onValueChange,
      valueRange = valueRange,
      steps = steps,
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp), // Enforce minimum 48dp touch target
      colors = SliderDefaults.colors(
        thumbColor = activeColor,
        activeTrackColor = activeColor,
        inactiveTrackColor = inactiveColor,
        activeTickColor = Color.Transparent,
        inactiveTickColor = Color.Transparent
      ),
      thumb = {
        Box(
          modifier = Modifier
            .size(20.dp)
            .shadow(
              elevation = 4.dp,
              shape = CircleShape,
              spotColor = activeColor.copy(alpha = 0.6f)
            )
            .clip(CircleShape)
            .background(
              Brush.radialGradient(
                listOf(
                  CinemaWhite,
                  activeColor
                )
              )
            )
            .border(1.dp, Color.White, CircleShape)
        )
      },
      track = { sliderState ->
        val fraction = (sliderState.value - sliderState.valueRange.start) /
            (sliderState.valueRange.endInclusive - sliderState.valueRange.start).coerceAtLeast(0.001f)

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(inactiveColor)
            .border(0.5.dp, Color(0x1AFFFFFF), RoundedCornerShape(3.dp))
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth(fraction.coerceIn(0f, 1f))
              .fillMaxHeight()
              .clip(RoundedCornerShape(3.dp))
              .background(
                Brush.horizontalGradient(
                  listOf(
                    activeColor.copy(alpha = 0.7f),
                    activeColor
                  )
                )
              )
          )
        }
      }
    )
  }
}

/**
 * Premium 2026 Interactive Filter Chip / Tab Pill.
 * Tactile neumorphic depth when unselected, glass surface with accent when selected.
 */
@Composable
fun ModernFilterChip(
  selected: Boolean,
  onClick: () -> Unit,
  label: String,
  modifier: Modifier = Modifier,
  activeColor: Color = CinemaWhite,
  activeTextColor: Color = Color(0xFF0B0D13),
  inactiveTextColor: Color = TextMuted
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.95f else 1.0f,
    animationSpec = spring(stiffness = 400f),
    label = "chip_scale"
  )

  Box(
    modifier = modifier
      .scale(scale)
      .clip(RoundedCornerShape(18.dp))
      .then(
        if (selected) {
          Modifier
            .background(activeColor)
            .border(1.dp, activeColor, RoundedCornerShape(18.dp))
            .shadow(2.dp, RoundedCornerShape(18.dp), spotColor = activeColor.copy(alpha = 0.4f))
        } else {
          Modifier
            .neumorphicDepth(
              shape = RoundedCornerShape(18.dp),
              elevation = 2.dp,
              isPressed = isPressed,
              baseColor = Color(0xFF141824)
            )
        }
      )
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
      )
      .padding(horizontal = 14.dp, vertical = 7.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = if (selected) activeTextColor else inactiveTextColor,
      fontSize = 12.sp,
      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
    )
  }
}


/**
 * Premium 2026 Neumorphic Interactive Button with tactile press scale, soft shadows,
 * and polished surface gradients.
 */
@Composable
fun ModernButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  containerColor: Color = CinemaWhite,
  shape: Shape = RoundedCornerShape(14.dp),
  elevation: Dp = 3.dp,
  content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.97f else 1.0f,
    animationSpec = spring(stiffness = 500f),
    label = "button_scale"
  )

  Box(
    modifier = modifier
      .scale(scale)
      .height(48.dp)
      .neumorphicDepth(
        shape = shape,
        elevation = if (isPressed) 0.dp else elevation,
        isPressed = isPressed,
        baseColor = containerColor
      )
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        onClick = onClick
      )
      .padding(horizontal = 16.dp),
    contentAlignment = Alignment.Center
  ) {
    androidx.compose.foundation.layout.Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
      content = content
    )
  }
}
