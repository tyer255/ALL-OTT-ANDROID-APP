package com.example.ui.screens

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItem
import com.example.ui.components.ContinueWatchingCard
import com.example.ui.components.MediaCard
import com.example.ui.theme.AmberGoldPrimary
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DividerColor
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.WhiteTextPrimary
import com.example.ui.theme.liquidGlassSurface
import com.example.ui.viewmodel.AetherUiState

/**
 * Premium 2026 Guest Profile Screen.
 * Tailored for guest mode without unnecessary login forms or artificial AI styling.
 */
@Composable
fun ProfileScreen(
  uiState: AetherUiState,
  onMediaClick: (MediaItem) -> Unit,
  onPlayClick: (MediaItem) -> Unit,
  onOpenMyList: () -> Unit,
  onBackClick: () -> Unit = {},
  onRemoveFromContinueWatching: ((String) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var streamOnWifiOnly by remember { mutableStateOf(true) }
  var enableDolbyAudio by remember { mutableStateOf(true) }
  var downloadQualityHigh by remember { mutableStateOf(true) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .statusBarsPadding()
      .testTag("profile_screen"),
    contentPadding = PaddingValues(bottom = 90.dp)
  ) {
    // 1. Top Navigation Bar
    item(key = "profile_top_nav") {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(DarkSurfaceVariant)
            .clickable(onClick = onBackClick),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = WhiteTextPrimary,
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = "Guest Profile & Settings",
          color = WhiteTextPrimary,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    // 2. Guest Header Section
    item(key = "profile_header") {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(86.dp)
            .clip(CircleShape)
            .background(
              androidx.compose.ui.graphics.Brush.linearGradient(
                listOf(Color(0xFF232D3F), Color(0xFF141924))
              )
            )
            .border(2.dp, GlassTokens.BorderHighlight, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Guest User",
            tint = CinemaWhite,
            modifier = Modifier.size(52.dp)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Guest User",
          color = WhiteTextPrimary,
          fontSize = 19.sp,
          fontWeight = FontWeight.Bold
        )

        Row(
          modifier = Modifier
            .padding(top = 4.dp)
            .liquidGlassSurface(
              shape = RoundedCornerShape(12.dp),
              elevation = 1.dp,
              backgroundColor = Color(0x661E2432),
              borderColor = GlassTokens.BorderHighlight
            )
            .padding(horizontal = 12.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(Color(0xFF34D399))
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Guest Mode • No Login Required",
            color = WhiteTextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }

    // 3. Continue Watching Section
    if (uiState.continueWatchingItems.isNotEmpty()) {
      item(key = "profile_continue_watching") {
        Column(modifier = Modifier.padding(top = 10.dp)) {
          Text(
            text = "Continue Watching",
            color = WhiteTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
          )
          LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            items(uiState.continueWatchingItems, key = { it.id }) { item ->
              ContinueWatchingCard(
                mediaItem = item,
                onPlayClick = { onPlayClick(item) },
                onDetailClick = { onMediaClick(item) },
                onRemoveClick = { onRemoveFromContinueWatching?.invoke(item.id) }
              )
            }
          }
        }
      }
    }

    // 4. My List Section
    item(key = "profile_my_list_row") {
      Column(modifier = Modifier.padding(top = 16.dp)) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenMyList)
            .padding(horizontal = 16.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Bookmark,
              contentDescription = null,
              tint = AmberGoldPrimary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "My List (${uiState.myListItems.size})",
              color = WhiteTextPrimary,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
          }
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = "View all",
            tint = MutedSlate,
            modifier = Modifier.size(14.dp)
          )
        }

        if (uiState.myListItems.isNotEmpty()) {
          LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(vertical = 6.dp)
          ) {
            items(uiState.myListItems, key = { it.id }) { item ->
              MediaCard(
                mediaItem = item,
                onClick = { onMediaClick(item) },
                cardWidth = 110
              )
            }
          }
        }
      }
    }

    // 5. Watch History Section
    if (uiState.watchHistory.isNotEmpty()) {
      item(key = "profile_history") {
        Column(modifier = Modifier.padding(top = 16.dp)) {
          Text(
            text = "Watch History",
            color = WhiteTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
          )
          LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(uiState.watchHistory, key = { "hist_${it.id}" }) { item ->
              MediaCard(
                mediaItem = item,
                onClick = { onMediaClick(item) },
                cardWidth = 110
              )
            }
          }
        }
      }
    }

    // 6. Settings & Preferences Section
    item(key = "profile_settings") {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 20.dp)
      ) {
        Text(
          text = "App Settings & Playback",
          color = WhiteTextPrimary,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(bottom = 12.dp)
        )

        SettingSwitchRow(
          icon = Icons.Default.Tv,
          title = "Stream on Wi-Fi Only",
          subtitle = "Prevent streaming over mobile cellular data",
          checked = streamOnWifiOnly,
          onCheckedChange = { streamOnWifiOnly = it }
        )

        HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))

        SettingSwitchRow(
          icon = Icons.Default.HighQuality,
          title = "Spatial Audio & Dolby Atmos",
          subtitle = "Render multi-channel cinema surround sound",
          checked = enableDolbyAudio,
          onCheckedChange = { enableDolbyAudio = it }
        )

        HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))

        SettingSwitchRow(
          icon = Icons.Default.Download,
          title = "Ultra HD Offline Downloads",
          subtitle = "Save movies in maximum bit-depth",
          checked = downloadQualityHigh,
          onCheckedChange = { downloadQualityHigh = it }
        )

        HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))

        SettingLinkRow(
          icon = Icons.Default.Language,
          title = "Audio & Subtitles Language",
          value = "English (Original)"
        )

        HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))

        SettingLinkRow(
          icon = Icons.Default.Security,
          title = "All OTT Version",
          value = "v2.4.0 (Production)"
        )
      }
    }
  }
}

@Composable
fun SettingSwitchRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = AmberGoldPrimary,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = title,
          color = WhiteTextPrimary,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold
        )
        Text(
          text = subtitle,
          color = MutedSlate,
          fontSize = 11.sp
        )
      }
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = AmberGoldPrimary,
        checkedTrackColor = DarkSurfaceVariant,
        uncheckedTrackColor = DarkSurface
      )
    )
  }
}

@Composable
fun SettingLinkRow(
  icon: ImageVector,
  title: String,
  value: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 10.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = AmberGoldPrimary,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(12.dp))
      Text(
        text = title,
        color = WhiteTextPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold
      )
    }
    Text(
      text = value,
      color = MutedSlate,
      fontSize = 12.sp
    )
  }
}
