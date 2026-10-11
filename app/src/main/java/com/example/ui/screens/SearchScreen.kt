package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) CinemaWhite else Color(0x1AFFFFFF))
            .border(
              width = 1.dp,
              color = if (isSelected) CinemaWhite else Color(0x26FFFFFF),
              shape = RoundedCornerShape(18.dp)
            )
            .clickable { onTypeFilterSelect(typeKey) }
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("type_filter_$typeKey")
        ) {
          Text(
            text = label,
            color = if (isSelected) Color(0xFF0D1017) else TextMuted,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
          )
        }
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
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF161922))
                .border(0.8.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp))
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

        // Recommended Starter Results (2-col mobile, 3-col tablet)
        Text(
          text = "Recommended for You",
          color = CinemaWhite,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyVerticalGrid(
          columns = GridCells.Adaptive(minSize = 140.dp),
          contentPadding = PaddingValues(bottom = 20.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.weight(1f)
        ) {
          items(uiState.searchResults, key = { it.id }) { item ->
            MediaCard(
              mediaItem = item,
              onClick = { onMediaClick(item) },
              cardWidth = 0, // fills column width
              showMetaInfo = true,
              isBookmarked = isBookmarked(item.id),
              onBookmarkClick = { onToggleMyList(item) }
            )
          }
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
