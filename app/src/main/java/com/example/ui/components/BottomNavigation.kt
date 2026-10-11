package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.TextMuted
import com.example.ui.theme.liquidGlassSurface
import com.example.ui.viewmodel.Screen

data class ModernNavTab(
  val screen: Screen,
  val label: String,
  val activeIcon: ImageVector? = null,
  val inactiveIcon: ImageVector? = null,
  val iconDrawableRes: Int? = null,
  val testTag: String
)

/**
 * Premium 2026 Liquid Glass Bottom Navigation Bar.
 * Built with translucent multi-stop surface, refractive edge borders, and Neumorphic tactile tabs.
 */
@Composable
fun AetherBottomNavigation(
  currentScreen: Screen,
  onNavigate: (Screen) -> Unit,
  modifier: Modifier = Modifier
) {
  val tabs = listOf(
    ModernNavTab(Screen.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home, null, "nav_home"),
    ModernNavTab(Screen.Anime, "Anime", null, null, R.drawable.ic_anime_logo, "nav_anime"),
    ModernNavTab(Screen.LiveTv, "Live TV", Icons.Filled.LiveTv, Icons.Outlined.LiveTv, null, "nav_livetv"),
    ModernNavTab(Screen.Search, "Search", Icons.Filled.Search, Icons.Outlined.Search, null, "nav_search")
  )

  Box(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = 16.dp, vertical = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
        .liquidGlassSurface(
          shape = RoundedCornerShape(32.dp),
          elevation = 6.dp,
          backgroundColor = Color(0xE610141D),
          borderColor = GlassTokens.BorderHighlight,
          borderWidth = 1.dp
        )
        .padding(horizontal = 8.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      tabs.forEach { tab ->
        val isSelected = when (tab.screen) {
          Screen.Home -> currentScreen is Screen.Home
          Screen.Anime -> currentScreen is Screen.Anime
          Screen.LiveTv -> currentScreen is Screen.LiveTv
          Screen.Search -> currentScreen is Screen.Search
          Screen.MyList -> currentScreen is Screen.MyList
          else -> false
        }

        val iconTint by animateColorAsState(
          targetValue = if (isSelected) CinemaWhite else TextMuted,
          label = "nav_tint_${tab.label}"
        )

        val pillBgColor by animateColorAsState(
          targetValue = if (isSelected) Color(0x24FFFFFF) else Color.Transparent,
          label = "nav_bg_${tab.label}"
        )

        val pillBorderColor by animateColorAsState(
          targetValue = if (isSelected) Color(0x33FFFFFF) else Color.Transparent,
          label = "nav_border_${tab.label}"
        )

        // Outer touch container maintaining full 64dp height (> 48dp) for accessibility
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
              onClick = { onNavigate(tab.screen) }
            )
            .testTag(tab.testTag),
          contentAlignment = Alignment.Center
        ) {
          // Compact, balanced active highlight pill with 10dp clearance from outer borders
          Box(
            modifier = Modifier
              .height(44.dp)
              .fillMaxWidth(0.86f)
              .clip(RoundedCornerShape(16.dp))
              .background(pillBgColor)
              .border(0.8.dp, pillBorderColor, RoundedCornerShape(16.dp))
              .padding(horizontal = 4.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              if (tab.iconDrawableRes != null) {
                Icon(
                  painter = painterResource(id = tab.iconDrawableRes),
                  contentDescription = tab.label,
                  tint = iconTint,
                  modifier = Modifier.size(19.dp)
                )
              } else if (tab.activeIcon != null && tab.inactiveIcon != null) {
                Icon(
                  imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                  contentDescription = tab.label,
                  tint = iconTint,
                  modifier = Modifier.size(19.dp)
                )
              }

              Spacer(modifier = Modifier.height(2.dp))

              Text(
                text = tab.label,
                color = iconTint,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
              )

              if (isSelected) {
                Spacer(modifier = Modifier.height(1.dp))
                Box(
                  modifier = Modifier
                    .width(8.dp)
                    .height(2.dp)
                    .clip(CircleShape)
                    .background(
                      Brush.horizontalGradient(
                        listOf(
                          Color(0x33FFFFFF),
                          CinemaWhite,
                          Color(0x33FFFFFF)
                        )
                      )
                    )
                )
              }
            }
          }
        }
      }
    }
  }
}
