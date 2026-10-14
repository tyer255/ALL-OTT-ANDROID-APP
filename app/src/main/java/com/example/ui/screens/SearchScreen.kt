package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItem
import com.example.ui.components.MediaCard
import com.example.ui.components.SearchGridSkeleton
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.NetflixBlack
import com.example.ui.theme.NetflixCardSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.liquidGlassSurface
import com.example.ui.theme.neumorphicDepth
import com.example.ui.viewmodel.AetherUiState
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
  uiState: AetherUiState,
  onQueryChange: (String) -> Unit,
  onTypeFilterSelect: (String) -> Unit,
  onClearQuery: () -> Unit,
  onMediaClick: (MediaItem) -> Unit,
  onToggleMyList: (MediaItem) -> Unit,
  isBookmarked: (String) -> Boolean,
  modifier: Modifier = Modifier
) {
  val focusManager = LocalFocusManager.current

  // Rotating placeholder examples: Spider-Man, Anime, Pushpa 2, Stranger Things
  val placeholders = listOf("Search 'Spider-Man'", "Search 'Anime'", "Search 'Pushpa 2'", "Search 'Stranger Things'")
  var placeholderIndex by remember { mutableIntStateOf(0) }

  LaunchedEffect(Unit) {
    while (true) {
      delay(3000)
      placeholderIndex = (placeholderIndex + 1) % placeholders.size
    }
  }

  // Type filter definitions: All -> "all", Movies -> "movie", TV Series -> "tv", Anime -> "anime"
  val typeFilterTabs = listOf(
    "All" to "all",
    "Movies" to "movie",
    "TV Series" to "tv",
    "Anime" to "anime"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(NetflixBlack)
      .statusBarsPadding()
      .padding(bottom = 85.dp)
      .testTag("search_screen")
  ) {
    // 1. Search Bar with Rotating Placeholder Examples & Clear Button
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      OutlinedTextField(
        value = uiState.searchQuery,
        onValueChange = onQueryChange,
        placeholder = {
          AnimatedContent(
            targetState = placeholders[placeholderIndex],
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "search_placeholder_rotation"
          ) { text ->
            Text(
              text = text,
              color = TextMuted,
              fontSize = 14.sp
            )
          }
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = CinemaWhite,
            modifier = Modifier.size(22.dp)
          )
        },
        trailingIcon = {
          if (uiState.searchQuery.isNotEmpty()) {
            IconButton(
              onClick = onClearQuery,
              modifier = Modifier.testTag("search_clear_button")
            ) {
              Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear",
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color(0xFF141720),
          unfocusedContainerColor = Color(0xFF141720),
          focusedBorderColor = Color(0x66FFFFFF),
          unfocusedBorderColor = Color(0x26FFFFFF),
          focusedTextColor = CinemaWhite,
          unfocusedTextColor = CinemaWhite
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("search_input")
      )
    }

    // 2. Type Filters: All, Movies, TV Series, Anime
    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(typeFilterTabs) { (label, typeKey) ->
        val isSelected = uiState.selectedSearchType == typeKey
        com.example.ui.theme.ModernFilterChip(
          selected = isSelected,
          onClick = { onTypeFilterSelect(typeKey) },
          label = label,
          modifier = Modifier.testTag("type_filter_$typeKey")
        )
      }
    }

    // 3. Main Search Results & Suggestions
    if (uiState.isSearching) {
      // Shimmer Skeleton Loading State
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      ) {
        SearchGridSkeleton()
      }
    } else if (uiState.searchQuery.isBlank()) {
      // Trending Searches & Category Suggestions
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(bottom = 12.dp)
        ) {
          Icon(
            imageVector = Icons.Default.TrendingUp,
            contentDescription = null,
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Popular Searches",
            color = CinemaWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          )
        }

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.padding(bottom = 20.dp)
        ) {
          listOf("Spider-Man", "Anime", "Pushpa 2", "Stranger Things", "Extraction 2", "Demon Slayer", "Wednesday", "Toxic").forEach { keyword ->
            Box(
              modifier = Modifier
                .neumorphicDepth(shape = RoundedCornerShape(14.dp), elevation = 2.dp, baseColor = Color(0xFF161A26))
                .clickable { onQueryChange(keyword) }
                .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
              Text(
                text = keyword,
                color = CinemaWhite.copy(alpha = 0.9f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        // Recommended Starter Results (Exactly 2 Infinite Auto-scrolling Rows with compact thumbnails)
        Text(
          text = "Recommended for You",
          color = CinemaWhite,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(top = 10.dp, bottom = 12.dp)
        )

        // Split recommended search results into 2 rows of 7 items each
        val recommendedSource = uiState.searchResults.ifEmpty { uiState.contentRows.flatMap { it.items } }
        val row1Items = remember(recommendedSource) { recommendedSource.take(7) }
        val row2Items = remember(recommendedSource) { recommendedSource.drop(7).take(7).ifEmpty { recommendedSource.take(7) } }

        Column(
          verticalArrangement = Arrangement.spacedBy(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        ) {
          // Row 1: Rightwards moving marquee (7 items)
          InfiniteAutoScrollRow(
            items = row1Items,
            moveRight = true,
            onMediaClick = onMediaClick,
            isBookmarked = isBookmarked,
            onToggleMyList = onToggleMyList
          )

          // Row 2: Leftwards moving marquee (7 items)
          InfiniteAutoScrollRow(
            items = row2Items,
            moveRight = false,
            onMediaClick = onMediaClick,
            isBookmarked = isBookmarked,
            onToggleMyList = onToggleMyList
          )
        }
      }
    } else if (uiState.searchResults.isEmpty()) {
      // Empty Results State
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(horizontal = 32.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(48.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "No results for \"${uiState.searchQuery}\"",
            color = CinemaWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Try checking for spelling or searching with a broader title (e.g. 'Spider-Man', 'Anime', 'Batman')",
            color = TextMuted,
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
    } else {
      // 2-Column Mobile, 3-Column Tablet Grid with 2:3 Aspect Ratio Cards & Bookmarks
      LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .testTag("search_results_grid")
      ) {
        items(uiState.searchResults, key = { it.id }) { item ->
          MediaCard(
            mediaItem = item,
            onClick = { onMediaClick(item) },
            cardWidth = 0, // fills available column width
            showMetaInfo = true,
            isBookmarked = isBookmarked(item.id),
            onBookmarkClick = { onToggleMyList(item) }
          )
        }
      }
    }
  }
}

@Composable
private fun InfiniteAutoScrollRow(
  items: List<MediaItem>,
  moveRight: Boolean,
  onMediaClick: (MediaItem) -> Unit,
  isBookmarked: (String) -> Boolean,
  onToggleMyList: (MediaItem) -> Unit,
  modifier: Modifier = Modifier
) {
  if (items.isEmpty()) return

  val listState = androidx.compose.foundation.lazy.rememberLazyListState()

  // Repeat items count to provide seamless infinite scrolling without excessive memory footprint
  val infiniteList = remember(items) { List(50) { items }.flatten() }
  val startIndex = remember(infiniteList) { (infiniteList.size / 2) - ((infiniteList.size / 2) % items.size) }

  LaunchedEffect(startIndex) {
    listState.scrollToItem(startIndex)
  }

  // Ultra-smooth 60fps hardware-synchronized auto scroll loop
  LaunchedEffect(items, moveRight) {
    val scrollStep = if (moveRight) 0.85f else -0.85f
    while (true) {
      androidx.compose.runtime.withFrameMillis {
        // Only auto-scroll when user is not manually touching/dragging
        if (!listState.isScrollInProgress) {
          // If nearing the edges, seamlessly reset to center
          val currentItem = listState.firstVisibleItemIndex
          if (currentItem < 5 || currentItem > infiniteList.size - 10) {
            val centerOffset = (infiniteList.size / 2) - ((infiniteList.size / 2) % items.size)
            kotlinx.coroutines.runBlocking {
              listState.scrollToItem(centerOffset, listState.firstVisibleItemScrollOffset)
            }
          }
        }
      }
      if (!listState.isScrollInProgress) {
        listState.scrollBy(scrollStep)
      }
    }
  }

  LazyRow(
    state = listState,
    contentPadding = PaddingValues(horizontal = 4.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    userScrollEnabled = true,
    modifier = modifier.fillMaxWidth()
  ) {
    items(
      count = infiniteList.size,
      key = { index -> "auto_row_${if (moveRight) "r" else "l"}_${infiniteList[index].id}_$index" }
    ) { index ->
      val item = infiniteList[index]
      MediaCard(
        mediaItem = item,
        onClick = { onMediaClick(item) },
        cardWidth = 92, // Small, compact thumbnail size
        showMetaInfo = false,
        isBookmarked = isBookmarked(item.id),
        onBookmarkClick = { onToggleMyList(item) }
      )
    }
  }
}
