package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.ui.components.ContentRow
import com.example.ui.components.ContentRowSkeleton
import com.example.ui.components.ContinueWatchingCard
import com.example.ui.components.HeroBanner
import com.example.ui.components.HeroBannerSkeleton
import com.example.ui.components.ProviderPillsRow
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.NetflixBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.liquidGlassSurface
import com.example.ui.theme.neumorphicDepth
import com.example.ui.viewmodel.AetherUiState

@Composable
fun HomeScreen(
  uiState: AetherUiState,
  onHeroIndexSelect: (Int) -> Unit,
  onMediaClick: (MediaItem) -> Unit,
  onPlayClick: (MediaItem) -> Unit,
  onToggleMyList: (MediaItem) -> Unit,
  onSearchClick: () -> Unit,
  onProfileClick: () -> Unit,
  onSelectProvider: (String?) -> Unit,
  onRemoveFromContinueWatching: ((String) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val listState = rememberLazyListState()

  // Dynamic Category Filter: "All", "TV Shows", "Movies"
  var selectedFilter by remember { mutableStateOf("All") }

  // TV Cast dialog state
  var showCastDialog by remember { mutableStateOf(false) }
  var connectedCastDevice by remember { mutableStateOf<String?>(null) }

  // Filter content based on selected filter and provider
  val filteredRows = remember(uiState.contentRows, selectedFilter, uiState.selectedProviderId) {
    val typeFiltered = when (selectedFilter) {
      "TV Shows" -> uiState.contentRows.map { row ->
        row.copy(items = row.items.filter { it.mediaType == MediaType.SERIES })
      }.filter { it.items.isNotEmpty() }
      "Movies" -> uiState.contentRows.map { row ->
        row.copy(items = row.items.filter { it.mediaType == MediaType.MOVIE })
      }.filter { it.items.isNotEmpty() }
      else -> uiState.contentRows
    }

    val providerFiltered = if (uiState.selectedProviderId != null) {
      val targetProvider = uiState.selectedProviderId.lowercase()
      typeFiltered.map { row ->
        val isProviderSection = row.id.lowercase() == targetProvider
        val filteredItems = if (isProviderSection) {
          row.items
        } else {
          row.items.filter { it.streamingPlatform.lowercase().contains(targetProvider) }
        }
        row.copy(items = filteredItems)
      }.filter { it.items.isNotEmpty() }
    } else {
      typeFiltered
    }

    // STRICT DEDUPLICATION: Ensure no movie repeats across rows
    val seenIds = mutableSetOf<String>()
    val seenTitles = mutableSetOf<String>()
    providerFiltered.mapNotNull { row ->
      val uniqueItems = row.items.filter { item ->
        val norm = item.title.trim().lowercase()
        if (seenIds.contains(item.id) || seenTitles.contains(norm)) {
          false
        } else {
          seenIds.add(item.id)
          seenTitles.add(norm)
        }
      }
      if (uniqueItems.isNotEmpty()) row.copy(items = uniqueItems) else null
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(NetflixBlack)
  ) {
    LazyColumn(
      state = listState,
      modifier = Modifier
        .fillMaxSize()
        .testTag("home_scroll_container"),
      contentPadding = PaddingValues(bottom = 90.dp)
    ) {
      // -------------------------------------------------------------------
      // 1. TOP HEADER (Stays at the very top of the page with Liquid Glass)
      // -------------------------------------------------------------------
      item(key = "top_header") {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
          // Brand Row: Aether Logo + Cast & Search & Profile
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.clickable { selectedFilter = "All" }
            ) {
              Box(
                modifier = Modifier
                  .size(30.dp)
                  .clip(CircleShape)
                  .background(
                    Brush.linearGradient(
                      listOf(Color(0xFF26334A), Color(0xFF131924))
                    )
                  )
                  .border(1.dp, GlassTokens.BorderHighlight, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "OTT",
                  color = CinemaWhite,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Black
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "ALL OTT",
                color = CinemaWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              // Cast Button
              IconButton(
                onClick = { showCastDialog = true },
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(if (connectedCastDevice != null) Color(0x3310B981) else Color(0x1AFFFFFF))
              ) {
                Icon(
                  imageVector = if (connectedCastDevice != null) Icons.Default.CastConnected else Icons.Default.Cast,
                  contentDescription = "Cast to TV",
                  tint = if (connectedCastDevice != null) Color(0xFF34D399) else CinemaWhite,
                  modifier = Modifier.size(19.dp)
                )
              }

              Spacer(modifier = Modifier.width(4.dp))

              // Search Button
              IconButton(
                onClick = onSearchClick,
                modifier = Modifier
                  .size(36.dp)
                  .testTag("home_top_search_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = "Search",
                  tint = CinemaWhite,
                  modifier = Modifier.size(21.dp)
                )
              }

              Spacer(modifier = Modifier.width(4.dp))

              // Guest Profile Avatar Button
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .neumorphicDepth(shape = CircleShape, elevation = 2.dp, baseColor = Color(0xFF1E2432))
                  .border(1.dp, GlassTokens.BorderHighlight, CircleShape)
                  .clickable(onClick = onProfileClick)
                  .testTag("home_top_profile_button"),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = androidx.compose.material.icons.Icons.Default.AccountCircle,
                  contentDescription = "Guest Profile",
                  tint = CinemaWhite.copy(alpha = 0.9f),
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Category Filter Tabs: All, TV Shows, Movies
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            listOf("All", "TV Shows", "Movies").forEach { tab ->
              val isSelected = selectedFilter == tab
              com.example.ui.theme.ModernFilterChip(
                selected = isSelected,
                onClick = { selectedFilter = tab },
                label = tab
              )
            }
          }
        }
      }

      // -------------------------------------------------------------------
      // 2. HERO BANNER OR SHIMMER SKELETON
      // -------------------------------------------------------------------
      if (uiState.isLoadingHome && uiState.featuredHeroItems.isEmpty()) {
        item(key = "hero_skeleton") {
          HeroBannerSkeleton()
        }
      } else {
        item(key = "hero_banner") {
          val currentHero = uiState.featuredHeroItems.getOrNull(uiState.currentHeroIndex)
          val isInList = currentHero?.let { uiState.myListItems.any { item -> item.id == it.id } } ?: false

          HeroBanner(
            items = uiState.featuredHeroItems,
            currentIndex = uiState.currentHeroIndex,
            onIndexSelect = onHeroIndexSelect,
            onPlayClick = { onMediaClick(it) }, // Opens dedicated detail page first
            onDetailClick = { onMediaClick(it) }, // Opens dedicated detail page
            onToggleMyList = onToggleMyList,
            isInMyList = isInList
          )
        }
      }

      // -------------------------------------------------------------------
      // 3. OFFICIAL OTT PLATFORM HUBS ROW
      // -------------------------------------------------------------------
      item(key = "provider_hubs") {
        ProviderPillsRow(
          selectedProviderId = uiState.selectedProviderId,
          onSelectProvider = onSelectProvider
        )
      }

      // -------------------------------------------------------------------
      // 3.5. CONTINUE WATCHING (WATCH HISTORY ROW DIRECTLY BELOW STREAMING HUBS)
      // -------------------------------------------------------------------
      if (uiState.continueWatchingItems.isNotEmpty()) {
        item(key = "continue_watching_section") {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 8.dp, bottom = 12.dp)
              .testTag("home_continue_watching_section")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Continue Watching",
                color = CinemaWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp
              )
            }

            LazyRow(
              contentPadding = PaddingValues(horizontal = 16.dp),
              horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              items(uiState.continueWatchingItems, key = { "cw_${it.id}" }) { item ->
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

      // -------------------------------------------------------------------
      // 4. CONTENT SECTIONS OR SKELETON LOADERS
      // -------------------------------------------------------------------
      if (uiState.isLoadingHome && filteredRows.isEmpty()) {
        items(3) {
          ContentRowSkeleton()
        }
      } else {
        items(filteredRows, key = { it.id }) { section ->
          ContentRow(
            title = section.title,
            subtitle = section.subtitle,
            items = section.items,
            onItemClick = { onMediaClick(it) }
          )
        }
      }
    }

    // -----------------------------------------------------------------------
    // Professional 2026 Liquid Glass Cast Dialog
    // -----------------------------------------------------------------------
    if (showCastDialog) {
      Dialog(onDismissRequest = { showCastDialog = false }) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .liquidGlassSurface(
              shape = RoundedCornerShape(20.dp),
              elevation = 8.dp,
              backgroundColor = Color(0xF20F1420),
              borderColor = GlassTokens.BorderHighlight
            )
            .padding(20.dp)
        ) {
          Column(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Tv,
                  contentDescription = null,
                  tint = CinemaWhite,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = "Cast to Screen",
                  color = CinemaWhite,
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              IconButton(
                onClick = { showCastDialog = false },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
              }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = "Available wireless displays & Smart TVs nearby:",
              color = TextMuted,
              fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            val devices = listOf("Living Room Smart TV (4K)", "Bedroom Android TV", "Chromecast Ultra")
            devices.forEach { deviceName ->
              val isConnected = connectedCastDevice == deviceName
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(if (isConnected) Color(0x2610B981) else Color(0x14FFFFFF))
                  .border(0.8.dp, if (isConnected) Color(0x4D10B981) else Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                  .clickable {
                    connectedCastDevice = if (isConnected) null else deviceName
                    showCastDialog = false
                  }
                  .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = null,
                    tint = if (isConnected) Color(0xFF34D399) else CinemaWhite,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(12.dp))
                  Text(
                    text = deviceName,
                    color = CinemaWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                }

                if (isConnected) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Connected",
                    tint = Color(0xFF34D399),
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.height(8.dp))
            }
          }
        }
      }
    }
  }
}
