package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.TextMuted

data class ActualPlatformBrand(
  val id: String,
  val displayName: String,
  val drawableRes: Int?,
  val acronym: String,
  val accentColor: Color
)

@Composable
fun ProviderPillsRow(
  selectedProviderId: String?,
  onSelectProvider: (String?) -> Unit,
  modifier: Modifier = Modifier
) {
  // Exact OTT platforms: Netflix, Prime Video, JioHotstar, Crunchyroll, Apple TV+
  val platforms = listOf(
    ActualPlatformBrand("netflix", "Netflix", R.drawable.logo_netflix, "N", Color(0xFFE50914)),
    ActualPlatformBrand("prime", "Prime Video", R.drawable.logo_prime_video, "Prime", Color(0xFF00A8E1)),
    ActualPlatformBrand("jiostar", "JioHotstar", R.drawable.logo_disney_hotstar, "Jio", Color(0xFFFFCC00)),
    ActualPlatformBrand("crunchyroll", "Crunchyroll", R.drawable.logo_crunchyroll, "CR", Color(0xFFF47521)),
    ActualPlatformBrand("apple", "Apple TV+", R.drawable.logo_apple_tv, "TV+", Color(0xFFFFFFFF))
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 12.dp)
      .testTag("provider_pills_row")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Streaming Hubs",
        color = CinemaWhite,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.2).sp
      )
      if (selectedProviderId != null) {
        Text(
          text = "Reset Filter",
          color = Color(0xFFE50914),
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier
            .clickable { onSelectProvider(null) }
            .padding(4.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      items(platforms, key = { it.id }) { platform ->
        val isSelected = selectedProviderId == platform.id

        val borderColor by animateColorAsState(
          targetValue = if (isSelected) platform.accentColor else Color(0x33FFFFFF),
          label = "border_${platform.id}"
        )

        val borderWidth by animateDpAsState(
          targetValue = if (isSelected) 2.dp else 1.dp,
          label = "border_width_${platform.id}"
        )

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .clickable { onSelectProvider(platform.id) }
            .testTag("provider_pill_${platform.id}")
        ) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(
                Brush.verticalGradient(
                  listOf(
                    Color(0xFF1E232E),
                    Color(0xFF10131A)
                  )
                )
              )
              .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
              ),
            contentAlignment = Alignment.Center
          ) {
            if (platform.drawableRes != null) {
              Image(
                painter = painterResource(id = platform.drawableRes),
                contentDescription = platform.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .fillMaxSize()
                  .clip(RoundedCornerShape(15.dp))
              )
            } else {
              // Branded typographical emblem for SonyLIV and Zee5
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(
                    Brush.radialGradient(
                      listOf(
                        platform.accentColor.copy(alpha = 0.45f),
                        Color(0xFF141722)
                      )
                    )
                  ),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = platform.acronym,
                  color = CinemaWhite,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Black,
                  letterSpacing = 0.5.sp
                )
              }
            }

            if (isSelected) {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(platform.accentColor.copy(alpha = 0.15f))
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = platform.displayName,
            color = if (isSelected) CinemaWhite else TextMuted,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
          )
        }
      }
    }
  }
}
