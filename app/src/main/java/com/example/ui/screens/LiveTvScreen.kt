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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.LiveTvChannel
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.NetflixBlack
import com.example.ui.theme.NetflixCardSurface
import com.example.ui.theme.TextMuted

@Composable
fun LiveTvScreen(
  channels: List<LiveTvChannel>,
  isLoading: Boolean,
  onChannelClick: (LiveTvChannel) -> Unit,
  onRetry: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedCategory by remember { mutableStateOf("All") }
  var searchQuery by remember { mutableStateOf("") }

  // Exact categories requested: All, Entertainment, Movies, Sports, News, Music, Kids and Infotainment
  val categories = listOf(
    "All",
    "Entertainment",
    "Movies",
    "Sports",
    "News",
    "Music",
    "Kids",
    "Infotainment"
  )

  val filteredChannels = remember(channels, selectedCategory, searchQuery) {
    channels.filter { channel ->
      val matchesCategory = if (selectedCategory == "All") {
        true
      } else {
        channel.category.contains(selectedCategory, ignoreCase = true)
      }

      val matchesSearch = if (searchQuery.isBlank()) {
        true
      } else {
        channel.name.contains(searchQuery, ignoreCase = true) ||
            channel.category.contains(searchQuery, ignoreCase = true)
      }

      matchesCategory && matchesSearch
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(NetflixBlack)
      .statusBarsPadding()
      .testTag("live_tv_screen")
  ) {
    // 1. Header with branding and total channel counter
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE50914)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Tv,
            contentDescription = null,
            tint = CinemaWhite,
            modifier = Modifier.size(18.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "Live TV",
            color = CinemaWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp
          )
          Text(
            text = "${channels.size} Verified Broadcast Channels",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }

      // Red LIVE Broadcast Badge
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0x33E50914))
          .border(1.dp, Color(0x80E50914), RoundedCornerShape(12.dp))
          .padding(horizontal = 10.dp, vertical = 4.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(7.dp)
              .clip(CircleShape)
              .background(Color(0xFFE50914))
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "ON AIR",
            color = Color(0xFFFF5252),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
          )
        }
      }
    }

    // 2. Search / Quick Filter Bar
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search channel (e.g. Colors, Star Sports, CNBC)...", color = TextMuted, fontSize = 13.sp) },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color(0xFF141720),
          unfocusedContainerColor = Color(0xFF141720),
          focusedBorderColor = Color(0x4DFFFFFF),
          unfocusedBorderColor = Color(0x26FFFFFF),
          focusedTextColor = CinemaWhite,
          unfocusedTextColor = CinemaWhite
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
      )
    }

    // 3. Category Filter Pills
    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(categories) { cat ->
        val isSelected = selectedCategory == cat
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) CinemaWhite else Color(0x1AFFFFFF))
            .border(
              width = 1.dp,
              color = if (isSelected) CinemaWhite else Color(0x26FFFFFF),
              shape = RoundedCornerShape(18.dp)
            )
            .clickable { selectedCategory = cat }
            .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
          Text(
            text = cat,
            color = if (isSelected) Color(0xFF0D1017) else TextMuted,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
          )
        }
      }
    }

    // 4. Content Area: Loading / Empty / Channel Grid
    if (isLoading && channels.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(bottom = 80.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          CircularProgressIndicator(color = Color(0xFFE50914), modifier = Modifier.size(36.dp))
          Spacer(modifier = Modifier.height(14.dp))
          Text("Connecting to Live TV broadcast feeds...", color = TextMuted, fontSize = 13.sp)
        }
      }
    } else if (filteredChannels.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(bottom = 80.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.Tv, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
          Spacer(modifier = Modifier.height(12.dp))
          Text("No channels found in '$selectedCategory'", color = CinemaWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(6.dp))
          Text("Try selecting 'All' or searching a different channel", color = TextMuted, fontSize = 12.sp)
          Spacer(modifier = Modifier.height(16.dp))
          Button(
            onClick = {
              selectedCategory = "All"
              searchQuery = ""
              onRetry()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Reset & Refresh", color = CinemaWhite)
          }
        }
      }
    } else {
      LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 95.dp, top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(filteredChannels, key = { it.id }) { channel ->
          ChannelCard(
            channel = channel,
            onClick = { onChannelClick(channel) }
          )
        }
      }
    }
  }
}

@Composable
fun ChannelCard(
  channel: LiveTvChannel,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(NetflixCardSurface)
      .border(0.8.dp, Color(0x1FFFFFFF), RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .testTag("live_channel_${channel.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Channel Logo container
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFF0F121A)),
        contentAlignment = Alignment.Center
      ) {
        if (channel.logoUrl.isNotBlank()) {
          AsyncImage(
            model = channel.logoUrl,
            contentDescription = channel.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
              .size(64.dp)
              .padding(4.dp)
          )
        } else {
          Icon(
            imageVector = Icons.Default.Tv,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(32.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Clean channel name without duplicate/cluttered HD text
      val cleanChannelName = remember(channel.name) {
        channel.name.replace(Regex("(?i)\\bHD\\b"), "").replace("  ", " ").trim()
      }

      Text(
        text = if (cleanChannelName.isNotBlank()) cleanChannelName else channel.name,
        color = CinemaWhite,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(4.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        // Red LIVE indicator
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(Color(0xFFE50914))
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "LIVE",
            color = Color(0xFFFF5252),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black
          )
        }

        Text("•", color = TextMuted, fontSize = 10.sp)

        Text(
          text = channel.category,
          color = TextMuted,
          fontSize = 10.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    // Modern, sleek HD Pill Badge at top-right corner
    if (channel.isHd) {
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(6.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(Color(0x3300D9F5))
          .border(0.5.dp, Color(0x8000D9F5), RoundedCornerShape(4.dp))
          .padding(horizontal = 5.dp, vertical = 2.dp)
      ) {
        Text(
          text = "HD",
          color = Color(0xFF00D9F5),
          fontSize = 8.sp,
          fontWeight = FontWeight.Black
        )
      }
    }
  }
}
