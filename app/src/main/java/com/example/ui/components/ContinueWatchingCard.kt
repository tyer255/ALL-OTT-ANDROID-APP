package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.NetflixRed
import com.example.ui.theme.ProgressBarTrack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.liquidGlassSurface

/**
 * Premium 2026 Liquid Glass Continue Watching Card with correct 16:9 landscape scaling
 * and three-dot overflow removal menu.
 */
@Composable
fun ContinueWatchingCard(
  mediaItem: MediaItem,
  onPlayClick: () -> Unit,
  onDetailClick: () -> Unit,
  onRemoveClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val progress = mediaItem.progressPercent ?: 0.35f
  var showMenu by remember { mutableStateOf(false) }

  val imageUrl = remember(mediaItem.backdropUrl, mediaItem.posterUrl) {
    if (mediaItem.backdropUrl.isNotBlank()) mediaItem.backdropUrl else mediaItem.posterUrl
  }

  Column(
    modifier = modifier
      .width(172.dp)
      .liquidGlassSurface(
        shape = RoundedCornerShape(12.dp),
        elevation = 3.dp,
        backgroundColor = Color(0xCC131824),
        borderColor = GlassTokens.BorderHighlight
      )
      .testTag("continue_watching_${mediaItem.id}")
  ) {
    // 16:9 Landscape Thumbnail Container
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(16f / 9f)
        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
        .clickable(onClick = onPlayClick)
    ) {
      // Neutral background placeholder
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            remember {
              Brush.verticalGradient(
                listOf(Color(0xFF1E2638), Color(0xFF121722))
              )
            }
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Tv,
          contentDescription = null,
          tint = Color(0x33FFFFFF),
          modifier = Modifier.size(28.dp)
        )
      }

      if (imageUrl.isNotBlank()) {
        val context = LocalContext.current
        val imageRequest = remember(imageUrl) {
          ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(100)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
        }
        AsyncImage(
          model = imageRequest,
          contentDescription = mediaItem.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      }

      // Subtle gradient scrim overlay for contrast
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(Color.Transparent, Color(0x99000000))
            )
          )
      )

      // Centered play button
      Box(
        modifier = Modifier
          .align(Alignment.Center)
          .size(34.dp)
          .clip(CircleShape)
          .background(Color(0xCC000000))
          .liquidGlassSurface(
            shape = CircleShape,
            elevation = 2.dp,
            backgroundColor = Color(0x99000000),
            borderColor = GlassTokens.BorderHighlight
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.PlayArrow,
          contentDescription = "Resume",
          tint = CinemaWhite,
          modifier = Modifier.size(20.dp)
        )
      }

      // Progress bar at bottom of thumbnail
      LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
          .fillMaxWidth()
          .height(3.dp)
          .align(Alignment.BottomCenter),
        color = NetflixRed,
        trackColor = ProgressBarTrack
      )
    }

    // Bottom Action & Information Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onDetailClick)
        .padding(start = 10.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = mediaItem.title,
          color = CinemaWhite,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        val epSubtitle = mediaItem.lastWatchedEpisode
          ?: if (mediaItem.mediaType == MediaType.SERIES) "Chapter 1 • HD" else "${"%.0f".format(progress * 100)}% completed"
        Text(
          text = epSubtitle,
          color = TextMuted,
          fontSize = 10.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      // Three-Dot Overflow Menu Button
      Box {
        IconButton(
          onClick = { showMenu = true },
          modifier = Modifier
            .size(28.dp)
            .testTag("cw_menu_button_${mediaItem.id}")
        ) {
          Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More Options",
            tint = TextMuted,
            modifier = Modifier.size(16.dp)
          )
        }

        DropdownMenu(
          expanded = showMenu,
          onDismissRequest = { showMenu = false },
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xF0121722))
        ) {
          DropdownMenuItem(
            text = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.DeleteOutline,
                  contentDescription = null,
                  tint = Color(0xFFF87171),
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Remove from Continue Watching",
                  color = CinemaWhite,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            },
            onClick = {
              showMenu = false
              onRemoveClick?.invoke()
            },
            modifier = Modifier.testTag("cw_remove_${mediaItem.id}")
          )
        }
      }
    }
  }
}
