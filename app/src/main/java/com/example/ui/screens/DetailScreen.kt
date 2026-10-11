package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.ui.components.CastCard
import com.example.ui.components.DetailScreenSkeleton
import com.example.ui.components.EpisodeCardSkeleton
import com.example.ui.components.MediaCard
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.NetflixBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.liquidGlassSurface
import com.example.ui.theme.neumorphicDepth

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
  mediaItem: MediaItem,
  isInMyList: Boolean,
  similarItems: List<MediaItem>,
  onBackClick: () -> Unit,
  onPlayClick: (MediaItem, String?) -> Unit,
  onToggleMyList: (MediaItem) -> Unit,
  onSimilarClick: (MediaItem) -> Unit,
  modifier: Modifier = Modifier,
  onPlayTeaserClick: ((String) -> Unit)? = null,
  onDownloadClick: ((MediaItem) -> Unit)? = null,
  onPlayEpisodeClick: ((MediaItem, Int, Int, String) -> Unit)? = null,
  onSeasonChange: ((String, Int) -> Unit)? = null,
  isLoading: Boolean = false
) {
  BackHandler { onBackClick() }

  if (isLoading) {
    DetailScreenSkeleton(modifier = modifier)
    return
  }

  var selectedSeasonIndex by remember { mutableIntStateOf(0) }

  // Enriched cast with leading actresses and actors
  val displayCast = remember(mediaItem.cast, mediaItem.id) {
    if (mediaItem.cast.isNotEmpty()) {
      mediaItem.cast
    } else {
      com.example.data.util.MediaEnrichmentRegistry.resolveCast(mediaItem.id, mediaItem.title, emptyList())
    }
  }

  // All series seasons with immediate availability
  val displaySeasons = remember(mediaItem.seasons, mediaItem.id) {
    if (mediaItem.seasons.isNotEmpty()) {
      mediaItem.seasons
    } else if (mediaItem.mediaType == MediaType.SERIES) {
      com.example.data.util.MediaEnrichmentRegistry.getKnownSeasons(mediaItem.id, mediaItem.title)
    } else {
      emptyList()
    }
  }

  // Auto-fetch episodes for current selected season if not yet loaded
  androidx.compose.runtime.LaunchedEffect(mediaItem.id, selectedSeasonIndex, displaySeasons) {
    val cur = displaySeasons.getOrNull(selectedSeasonIndex)
    if (cur != null && cur.episodes.isEmpty()) {
      onSeasonChange?.invoke(mediaItem.id, cur.seasonNumber)
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(NetflixBlack)
      .testTag("detail_screen")
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(bottom = 100.dp)
    ) {
      // -----------------------------------------------------------------------
      // 1. TOP APP BAR & BACKDROP BANNER WITH CENTRAL PLAY BUTTON
      // -----------------------------------------------------------------------
      item(key = "detail_backdrop") {
        val bannerImage = remember(mediaItem.backdropUrl, mediaItem.posterUrl) {
          if (mediaItem.backdropUrl.isNotBlank()) mediaItem.backdropUrl else mediaItem.posterUrl
        }

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 230.dp, max = 320.dp)
            .background(Color(0xFF0D0F14))
        ) {
          // 1. Ambient Blurred Background Fill (prevents empty gaps while matching image tones)
          AsyncImage(
            model = bannerImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .fillMaxSize()
              .graphicsLayer {
                alpha = 0.32f
              }
          )

          // 2. Main Hero Banner - Sharp, 90-100% fully visible artwork with ContentScale.Fit
          AsyncImage(
            model = bannerImage,
            contentDescription = mediaItem.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier
              .fillMaxSize()
              .align(Alignment.Center)
          )

          // 3. Subtle, non-intrusive gradient overlay (top control contrast + smooth bottom fade)
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors = listOf(
                    Color.Black.copy(alpha = 0.35f),
                    Color.Transparent,
                    Color.Transparent,
                    NetflixBlack.copy(alpha = 0.7f),
                    NetflixBlack
                  )
                )
              )
          )

          // Top Header Row with Back Button, Brand, Search, Profile
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .statusBarsPadding()
              .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Circular Back Button
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f))
                .border(0.8.dp, Color(0x33FFFFFF), CircleShape)
                .clickable { onBackClick() }
                .testTag("detail_back_button"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = CinemaWhite,
                modifier = Modifier.size(20.dp)
              )
            }

            // Top Brand Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(26.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF1E232E))
                  .border(0.8.dp, Color(0x4DFFFFFF), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "OTT",
                  color = CinemaWhite,
                  fontSize = 8.sp,
                  fontWeight = FontWeight.Black
                )
              }
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "ALL OTT",
                color = CinemaWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
              )
            }

            // Right icons
            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                onClick = { /* Search */ },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = "Search",
                  tint = CinemaWhite,
                  modifier = Modifier.size(20.dp)
                )
              }
              IconButton(
                onClick = { /* Profile */ },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = "Profile",
                  tint = CinemaWhite,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }

          // Large Central Play Button
          Box(
            modifier = Modifier
              .align(Alignment.Center)
              .size(60.dp)
              .clip(CircleShape)
              .background(Color.Black.copy(alpha = 0.65f))
              .border(1.5.dp, Color(0x66FFFFFF), CircleShape)
              .clickable { onPlayClick(mediaItem, null) }
              .testTag("detail_center_play_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Play",
              tint = CinemaWhite,
              modifier = Modifier.size(38.dp)
            )
          }
        }
      }

      // -----------------------------------------------------------------------
      // 2. METADATA BADGES, TITLE, GENRES & ACTION BUTTONS
      // -----------------------------------------------------------------------
      item(key = "detail_meta_and_actions") {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        ) {
          // Metadata Badges Row: [Netflix] [⭐ IMDb 8.1/10] [2024] [Series] [FHD 1080p]
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Platform Badge
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF14171F))
                .border(0.8.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = mediaItem.streamingPlatform,
                color = CinemaWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }

            // IMDb Rating Badge
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF14171F))
                .border(0.8.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "⭐ IMDb ${mediaItem.rating}/10",
                color = Color(0xFFFFC107),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }

            // Year Badge
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF14171F))
                .border(0.8.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "${mediaItem.releaseYear}",
                color = CinemaWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
            }

            // Type Badge
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF14171F))
                .border(0.8.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = mediaItem.mediaType.label,
                color = CinemaWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Quality Badge (e.g. FHD 1080p in green pill)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(Color(0x2600C853))
              .border(0.8.dp, Color(0x8000E676), RoundedCornerShape(6.dp))
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = mediaItem.audioFormats,
              color = Color(0xFF00E676),
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Main Big Title
          Text(
            text = mediaItem.title,
            color = CinemaWhite,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            lineHeight = 32.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Genre Chips Row
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            mediaItem.genres.forEach { genre ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(16.dp))
                  .background(Color(0x1AFFFFFF))
                  .border(0.8.dp, Color(0x26FFFFFF), RoundedCornerShape(16.dp))
                  .padding(horizontal = 12.dp, vertical = 5.dp)
              ) {
                Text(
                  text = genre,
                  color = CinemaWhite.copy(alpha = 0.9f),
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          if (mediaItem.isComingSoon) {
            // Yellow Coming Soon banner
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF59E0B))
                .padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "COMING SOON TO OTT & THEATERS",
                color = Color(0xFF0F1218),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Button: PLAY TEASER (never treat as playable movie)
            Button(
              onClick = { onPlayTeaserClick?.invoke(mediaItem.title) ?: onPlayClick(mediaItem, "Teaser") },
              colors = ButtonDefaults.buttonColors(
                containerColor = CinemaWhite,
                contentColor = Color(0xFF0B0D13)
              ),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("detail_play_teaser_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Default.PlayArrow,
                  contentDescription = null,
                  tint = Color(0xFF0B0D13),
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Play Teaser",
                  color = Color(0xFF0B0D13),
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          } else {
            // Released content: PLAY NOW
            Button(
              onClick = { onPlayClick(mediaItem, null) },
              colors = ButtonDefaults.buttonColors(
                containerColor = CinemaWhite,
                contentColor = Color(0xFF0B0D13)
              ),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("detail_play_now_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Default.PlayArrow,
                  contentDescription = null,
                  tint = Color(0xFF0B0D13),
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Play Now",
                  color = Color(0xFF0B0D13),
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // WATCH TRAILER
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0x1F232D3F))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp))
                .clickable { onPlayTeaserClick?.invoke(mediaItem.title) ?: onPlayClick(mediaItem, "Trailer") }
                .testTag("detail_watch_trailer_button"),
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Tv,
                  contentDescription = null,
                  tint = CinemaWhite,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Watch Trailer",
                  color = CinemaWhite,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // -------------------------------------------------------------------
          // QUICK ACTION ICONS ROW (My List, Download, Share, 1080p)
          // -------------------------------------------------------------------
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // My List
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier
                .clickable { onToggleMyList(mediaItem) }
                .padding(8.dp)
            ) {
              Icon(
                imageVector = if (isInMyList) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                contentDescription = "My List",
                tint = if (isInMyList) Color(0xFFF59E0B) else TextMuted,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "My List",
                color = if (isInMyList) CinemaWhite else TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
            }

            // Download
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier
                .clickable { onDownloadClick?.invoke(mediaItem) }
                .padding(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Download",
                tint = Color(0xFF00D9F5),
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Download",
                color = CinemaWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
            }

            // Share
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier
                .clickable { /* Share */ }
                .padding(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = TextMuted,
                modifier = Modifier.size(22.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Share",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
            }

            // Quality
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.HighQuality,
                contentDescription = "Quality",
                tint = TextMuted,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "1080p",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // -------------------------------------------------------------------
          // 3. STORYLINE SECTION
          // -------------------------------------------------------------------
          Text(
            text = "STORYLINE",
            color = CinemaWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = mediaItem.overview,
            color = CinemaWhite.copy(alpha = 0.82f),
            fontSize = 13.sp,
            lineHeight = 20.sp
          )

          Spacer(modifier = Modifier.height(20.dp))

          // -------------------------------------------------------------------
          // 4. STARRING CAST SECTION (Horizontal Row of Avatars + Names)
          // -----------------------------------------------------------------------
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "STARRING CAST",
              color = CinemaWhite,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp
            )
            Text(
              text = "${displayCast.size} Stars & Actresses",
              color = TextMuted,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(end = 16.dp)
          ) {
            items(displayCast) { castMember ->
              CastCard(castMember = castMember)
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Director Info
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Director: ",
              color = TextMuted,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium
            )
            Text(
              text = mediaItem.director,
              color = CinemaWhite,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(24.dp))
        }
      }

      // -----------------------------------------------------------------------
      // 5. EPISODES & SEASONS SECTION (For Series & Anime)
      // -----------------------------------------------------------------------
      if (displaySeasons.isNotEmpty()) {
        item(key = "detail_episodes_header") {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp)
          ) {
            // Header with Icon
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFF1A202C))
                  .border(0.8.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.VideoLibrary,
                  contentDescription = null,
                  tint = CinemaWhite,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Episodes & Seasons",
                  color = CinemaWhite,
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Select any season and episode to play instantly in HD",
                  color = TextMuted,
                  fontSize = 11.sp
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Season Selector Tabs: [Season 1 (8 eps)] [Season 2 (8 eps)]
            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              itemsIndexed(displaySeasons) { index, season ->
                val isSelected = selectedSeasonIndex == index
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                      if (isSelected) CinemaWhite // Pure White background when selected
                      else Color(0x1AFFFFFF)
                    )
                    .border(
                      width = 1.dp,
                      color = if (isSelected) CinemaWhite else Color(0x26FFFFFF),
                      shape = RoundedCornerShape(20.dp)
                    )
                    .clickable {
                      selectedSeasonIndex = index
                      onSeasonChange?.invoke(mediaItem.id, season.seasonNumber)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("season_tab_$index")
                ) {
                  val epCountText = if (season.episodes.isNotEmpty()) " (${season.episodes.size} eps)" else ""
                  Text(
                    text = "${season.title}$epCountText",
                    color = if (isSelected) Color(0xFF0B0D13) else CinemaWhite, // Black text when active
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))
          }
        }

        // List of Episode Cards for Selected Season
        val currentSeason = displaySeasons.getOrNull(selectedSeasonIndex) ?: displaySeasons.first()

        if (currentSeason.episodes.isEmpty()) {
          item(key = "episodes_loading_hint") {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = CinemaWhite,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "Loading ${currentSeason.title} episodes in HD...",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }

          items(3) {
            EpisodeCardSkeleton(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
          }
        } else {
          items(currentSeason.episodes, key = { it.id }) { episode ->
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF131720))
                .border(0.8.dp, Color(0x26FFFFFF), RoundedCornerShape(16.dp))
                .clickable {
                  if (onPlayEpisodeClick != null) {
                    onPlayEpisodeClick(mediaItem, currentSeason.seasonNumber, episode.episodeNumber, episode.title)
                  } else {
                    onPlayClick(mediaItem, episode.title)
                  }
                }
                .testTag("episode_card_${episode.id}")
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp)
              ) {
                // 16:9 Episode Thumbnail with Badges
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0A0C10))
                ) {
                  AsyncImage(
                    model = episode.thumbnailUrl,
                    contentDescription = episode.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                  )

                  // Top Left: S1 : E1 Tag
                  Box(
                    modifier = Modifier
                      .align(Alignment.TopStart)
                      .padding(8.dp)
                      .clip(RoundedCornerShape(6.dp))
                      .background(Color.Black.copy(alpha = 0.75f))
                      .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                      .padding(horizontal = 8.dp, vertical = 4.dp)
                  ) {
                    Text(
                      text = "S${currentSeason.seasonNumber} : E${episode.episodeNumber}",
                      color = CinemaWhite,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }

                  // Central Play Circle
                  Box(
                    modifier = Modifier
                      .align(Alignment.Center)
                      .size(44.dp)
                      .clip(CircleShape)
                      .background(Color.Black.copy(alpha = 0.6f))
                      .border(1.dp, Color(0x66FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.PlayArrow,
                      contentDescription = "Play Episode",
                      tint = CinemaWhite,
                      modifier = Modifier.size(28.dp)
                    )
                  }

                  // Bottom Right: Duration Tag
                  Box(
                    modifier = Modifier
                      .align(Alignment.BottomEnd)
                      .padding(8.dp)
                      .clip(RoundedCornerShape(6.dp))
                      .background(Color.Black.copy(alpha = 0.75f))
                      .padding(horizontal = 6.dp, vertical = 3.dp)
                  ) {
                    Text(
                      text = "⏱ ${episode.duration}",
                      color = CinemaWhite,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Medium
                    )
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Episode Title & Rating Row
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = episode.title,
                    color = CinemaWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "⭐ ${episode.rating}",
                    color = Color(0xFFFFC107),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                  )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Episode Overview Text
                Text(
                  text = episode.overview,
                  color = TextMuted,
                  fontSize = 12.sp,
                  lineHeight = 17.sp,
                  maxLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Episode Footer: Aired Date & Play Episode Action
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "Aired: ${episode.airedDate}",
                    color = TextMuted.copy(alpha = 0.7f),
                    fontSize = 11.sp
                  )

                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                      if (onPlayEpisodeClick != null) {
                        onPlayEpisodeClick(mediaItem, currentSeason.seasonNumber, episode.episodeNumber, episode.title)
                      } else {
                        onPlayClick(mediaItem, episode.title)
                      }
                    }
                  ) {
                    Text(
                      text = "Play Episode",
                      color = CinemaWhite,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                      imageVector = Icons.Default.PlayArrow,
                      contentDescription = null,
                      tint = CinemaWhite,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }
            }
          }
        }
      } else {
        // -----------------------------------------------------------------------
        // 6. SIMILAR TITLES SECTION (For Movies)
        // -----------------------------------------------------------------------
        item(key = "detail_similar") {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 10.dp)
          ) {
            Text(
              text = "More Like This",
              color = CinemaWhite,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              items(similarItems) { item ->
                MediaCard(
                  mediaItem = item,
                  onClick = { onSimilarClick(item) },
                  cardWidth = 110,
                  showTopTenBadge = false
                )
              }
            }
          }
        }
      }
    }
  }
}
