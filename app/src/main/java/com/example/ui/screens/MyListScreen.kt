package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.ui.components.MediaCard
import com.example.ui.theme.AmberGoldPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DividerColor
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.WhiteTextPrimary

@Composable
fun MyListScreen(
  items: List<MediaItem>,
  onBackClick: () -> Unit,
  onMediaClick: (MediaItem) -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBackClick() }

  var selectedTab by remember { mutableStateOf("All") }
  val tabs = listOf("All", "Movies", "Series", "Saved")

  val filteredItems = remember(items, selectedTab) {
    when (selectedTab) {
      "Movies" -> items.filter { it.mediaType == MediaType.MOVIE }
      "Series" -> items.filter { it.mediaType == MediaType.SERIES }
      else -> items
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .statusBarsPadding()
      .testTag("my_list_screen")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onBackClick,
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(DarkSurface)
          .testTag("my_list_back_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = WhiteTextPrimary,
          modifier = Modifier.size(20.dp)
        )
      }

      Text(
        text = "My List",
        color = WhiteTextPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 12.dp)
      )
    }

    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.padding(vertical = 10.dp)
    ) {
      items(tabs) { tab ->
        val isSelected = tab == selectedTab
        com.example.ui.theme.ModernFilterChip(
          selected = isSelected,
          onClick = { selectedTab = tab },
          label = tab,
          activeColor = AmberGoldPrimary,
          activeTextColor = DarkBackground
        )
      }
    }

    if (filteredItems.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.BookmarkBorder,
            contentDescription = null,
            tint = MutedSlate,
            modifier = Modifier.size(54.dp)
          )
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "Your Watchlist is Empty",
            color = WhiteTextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "Explore movies and series from Home or Search and tap My List to save them here.",
            color = MutedSlate,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(top = 6.dp)
          )
        }
      }
    } else {
      LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(filteredItems, key = { it.id }) { item ->
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
