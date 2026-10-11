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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.util.CuratedAnimeCatalog
import com.example.ui.components.ContentRowSkeleton
import com.example.ui.components.HeroBanner
import com.example.ui.components.HeroBannerSkeleton
import com.example.ui.components.MediaCard
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.NetflixBlack
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.delay

@Composable
fun AnimeScreen(
  animeItems: List<MediaItem>,
  isLoading: Boolean,
  onMediaClick: (MediaItem) -> Unit,
  onPlayClick: (MediaItem) -> Unit,
  onToggleMyList: (MediaItem) -> Unit,
  isBookmarked: (String) -> Boolean,
  modifier: Modifier = Modifier
) {
  var selectedGenre by remember { mutableStateOf("All") }

  // 1. Comprehensive Anime pool (combines API response + curated Crunchyroll catalogue)
  val allAnime = remember(animeItems) {
    val existingIds = animeItems.map { it.id }.toSet()
    val supplementary = CuratedAnimeCatalog.items.filter { !existingIds.contains(it.id) }
    animeItems + supplementary
  }

  // 2. Animated Hero Banner items (Top 5 highly-rated anime for the stacked deck)
  val heroItems = remember(allAnime) {
    allAnime.filter { !it.isComingSoon }
      .sortedByDescending { it.rating }
      .take(5)
  }

  var currentHeroIndex by remember { mutableIntStateOf(0) }

  // Auto-rotation matching Home Screen Hero Banner
  LaunchedEffect(heroItems.size) {
    while (true) {
      delay(6500)
      if (heroItems.isNotEmpty()) {
        currentHeroIndex = (currentHeroIndex + 1) % heroItems.size
      }
    }
  }

  // 3. The 6 Distinct Sections with Per-Section Deduplication
  val (topTrending, shonenAction, mustWatch, isekaiFantasy, topMovies, emotionalMovies) = remember(allAnime) {
    fun filterSection(list: List<MediaItem>, maxCount: Int = 8): List<MediaItem> {
      val sectionSeen = mutableSetOf<String>()
      return list.filter { item ->
        val norm = item.title.trim().lowercase()
        if (sectionSeen.contains(norm)) false else { sectionSeen.add(norm); true }
      }.take(maxCount)
    }

    // Section 1: Top Trending Anime
    val s1 = filterSection(allAnime.filter { it.isTrending })

    // Section 2: Action & Shonen Hits
    val s2 = filterSection(
      allAnime.filter { item ->
        item.genres.any { it.contains("Shonen", ignoreCase = true) || it.contains("Action", ignoreCase = true) }
      }
    )

    // Section 3: Must-Watch Anime Series
    val s3 = filterSection(
      allAnime.filter { it.mediaType == MediaType.SERIES }
    )

    // Section 4: Fantasy & Isekai Adventures
    val s4 = filterSection(
      allAnime.filter { item ->
        item.genres.any { it.contains("Isekai", ignoreCase = true) || it.contains("Fantasy", ignoreCase = true) || it.contains("Adventure", ignoreCase = true) }
      }
    )

    // Section 5: Top Rated Anime Movies
    val s5 = filterSection(
      allAnime.filter { it.mediaType == MediaType.MOVIE && !it.isComingSoon }
    )

    // Section 6: Emotional & Heartfelt Anime Movies (At the bottom)
    val s6 = filterSection(
      allAnime.filter { item ->
        item.mediaType == MediaType.MOVIE &&
            item.genres.any { it.contains("Emotional", ignoreCase = true) || it.contains("Drama", ignoreCase = true) || it.contains("Romance", ignoreCase = true) }
      }
    )

    Tuple6(s1, s2, s3, s4, s5, s6)
  }

  // Filtered view when user taps a specific genre tag
  val genreFilteredItems = remember(allAnime, selectedGenre) {
    when (selectedGenre) {
      "Action / Shonen" -> allAnime.filter { item ->
        item.genres.any { it.contains("Action", ignoreCase = true) || it.contains("Shonen", ignoreCase = true) }
      }
      "Romance / Drama" -> allAnime.filter { item ->
        item.genres.any { it.contains("Romance", ignoreCase = true) || it.contains("Drama", ignoreCase = true) }
      }
      "Isekai / Fantasy" -> allAnime.filter { item ->
        item.genres.any { it.contains("Fantasy", ignoreCase = true) || it.contains("Isekai", ignoreCase = true) }
      }
      "Anime Movies" -> allAnime.filter { it.mediaType == MediaType.MOVIE && !it.isComingSoon }
      else -> allAnime
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(NetflixBlack)
      .testTag("anime_screen")
  ) {
    if (isLoading && allAnime.isEmpty()) {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 95.dp)
      ) {
        item { HeroBannerSkeleton() }
        items(3) { ContentRowSkeleton() }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 95.dp)
      ) {
        // --- 1. Top Header with Dedicated Anime Logo ---
        item(key = "anime_header") {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .statusBarsPadding()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFFF47521)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  painter = painterResource(id = R.drawable.ic_anime_logo),
                  contentDescription = "Anime Logo",
                  tint = CinemaWhite,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "Crunchyroll Anime",
                color = CinemaWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
              )
            }
          }
        }

        // --- 2. Animated Stacked Deck Hero Banner ---
        if (heroItems.isNotEmpty()) {
          item(key = "anime_hero_banner") {
            val currentHero = heroItems.getOrNull(currentHeroIndex)
            val isInList = currentHero?.let { isBookmarked(it.id) } ?: false

            HeroBanner(
              items = heroItems,
              currentIndex = currentHeroIndex,
              onIndexSelect = { currentHeroIndex = it },
              onPlayClick = { onMediaClick(it) },
              onDetailClick = { onMediaClick(it) },
              onToggleMyList = onToggleMyList,
              isInMyList = isInList
            )
          }
        }

        // --- 3. Genre Pills Filter Bar ---
        item(key = "anime_genre_pills") {
          val genreOptions = listOf("All", "Action / Shonen", "Romance / Drama", "Isekai / Fantasy", "Anime Movies")
          LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(genreOptions) { genre ->
              val isSelected = selectedGenre == genre
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .background(if (isSelected) Color(0xFFF47521) else Color(0x1AFFFFFF))
                  .border(
                    width = 1.dp,
                    color = if (isSelected) Color(0xFFF47521) else Color(0x26FFFFFF),
                    shape = RoundedCornerShape(20.dp)
                  )
                  .clickable { selectedGenre = genre }
                  .padding(horizontal = 14.dp, vertical = 6.dp)
              ) {
                Text(
                  text = genre,
                  color = if (isSelected) CinemaWhite else TextMuted,
                  fontSize = 12.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
              }
            }
          }
        }

        // --- 4. Content Rows ---
        if (selectedGenre != "All") {
          // Genre-specific filtered view
          item(key = "filtered_anime_section") {
            AnimeSectionRow(
              title = selectedGenre,
              items = genreFilteredItems,
              onMediaClick = onMediaClick,
              isBookmarked = isBookmarked,
              onToggleMyList = onToggleMyList
            )
          }
        } else {
          // Exactly 5 distinct, non-repeated anime sections:
          if (topTrending.isNotEmpty()) {
            item(key = "row_top_trending") {
              AnimeSectionRow(
                title = "Top Trending Anime",
                items = topTrending,
                onMediaClick = onMediaClick,
                isBookmarked = isBookmarked,
                onToggleMyList = onToggleMyList
              )
            }
          }

          if (shonenAction.isNotEmpty()) {
            item(key = "row_shonen_action") {
              AnimeSectionRow(
                title = "Action & Shonen Hits",
                items = shonenAction,
                onMediaClick = onMediaClick,
                isBookmarked = isBookmarked,
                onToggleMyList = onToggleMyList
              )
            }
          }

          if (mustWatch.isNotEmpty()) {
            item(key = "row_must_watch") {
              AnimeSectionRow(
                title = "Must-Watch Anime Series",
                items = mustWatch,
                onMediaClick = onMediaClick,
                isBookmarked = isBookmarked,
                onToggleMyList = onToggleMyList
              )
            }
          }

          if (isekaiFantasy.isNotEmpty()) {
            item(key = "row_isekai_fantasy") {
              AnimeSectionRow(
                title = "Fantasy & Isekai Adventures",
                items = isekaiFantasy,
                onMediaClick = onMediaClick,
                isBookmarked = isBookmarked,
                onToggleMyList = onToggleMyList
              )
            }
          }

          if (topMovies.isNotEmpty()) {
            item(key = "row_top_movies") {
              AnimeSectionRow(
                title = "Top Rated Anime Movies",
                items = topMovies,
                onMediaClick = onMediaClick,
                isBookmarked = isBookmarked,
                onToggleMyList = onToggleMyList
              )
            }
          }

          if (emotionalMovies.isNotEmpty()) {
            item(key = "row_emotional_movies") {
              AnimeSectionRow(
                title = "Emotional & Heartfelt Anime Movies",
                items = emotionalMovies,
                onMediaClick = onMediaClick,
                isBookmarked = isBookmarked,
                onToggleMyList = onToggleMyList
              )
            }
          }
        }
      }
    }
  }
}

private data class Tuple6<A, B, C, D, E, F>(
  val first: A,
  val second: B,
  val third: C,
  val fourth: D,
  val fifth: E,
  val sixth: F
)

@Composable
private fun AnimeSectionRow(
  title: String,
  items: List<MediaItem>,
  onMediaClick: (MediaItem) -> Unit,
  isBookmarked: (String) -> Boolean,
  onToggleMyList: (MediaItem) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 10.dp)
  ) {
    Text(
      text = title,
      color = CinemaWhite,
      fontSize = 17.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = (-0.2).sp,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    )

    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
        MediaCard(
          mediaItem = item,
          onClick = { onMediaClick(item) },
          cardWidth = 124,
          staggerIndex = index,
          showTopTenBadge = false,
          showMetaInfo = true,
          isBookmarked = isBookmarked(item.id),
          onBookmarkClick = { onToggleMyList(item) }
        )
      }
    }
  }
}
