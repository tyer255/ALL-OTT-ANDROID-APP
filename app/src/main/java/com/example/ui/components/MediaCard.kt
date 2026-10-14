package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.NetflixCardSurface
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Static cached placeholder brush to avoid per-card brush re-allocations
private val CardPlaceholderBrush = Brush.verticalGradient(
  listOf(Color(0xFF1E2638), Color(0xFF121722))
)

@Composable
fun MediaCard(
  mediaItem: MediaItem,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  cardWidth: Int = 118,
  staggerIndex: Int = 0,
  showTopTenBadge: Boolean = true,
  showMetaInfo: Boolean = false,
  isBookmarked: Boolean = false,
  onBookmarkClick: (() -> Unit)? = null
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  var imageLoadFailed by remember { mutableStateOf(false) }

  // Smooth entrance animation only if staggerIndex > 0; instant rendering for high-performance scrolling
  val animAlpha = remember(staggerIndex) { if (staggerIndex > 0) Animatable(0f) else null }
  val animScale = remember(staggerIndex) { if (staggerIndex > 0) Animatable(0.95f) else null }
  val animSlideX = remember(staggerIndex) { if (staggerIndex > 0) Animatable(15f) else null }

  if (staggerIndex > 0 && animAlpha != null && animScale != null && animSlideX != null) {
    LaunchedEffect(mediaItem.id) {
      val delayMs = (staggerIndex.coerceAtMost(5) * 25).toLong()
      delay(delayMs)
      launch { animAlpha.animateTo(1f, tween(180)) }
      launch { animScale.animateTo(1f, spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) }
      launch { animSlideX.animateTo(0f, spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) }
    }
  }

  val pressScale = if (isPressed) 0.96f else 1.0f

  Column(
    modifier = modifier
      .then(if (cardWidth > 0) Modifier.width(cardWidth.dp) else Modifier.fillMaxWidth())
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(2f / 3f)
        .graphicsLayer {
          alpha = animAlpha?.value ?: 1f
          scaleX = (animScale?.value ?: 1f) * pressScale
          scaleY = (animScale?.value ?: 1f) * pressScale
          translationX = animSlideX?.value ?: 0f
        }
        .clip(RoundedCornerShape(8.dp))
        .background(NetflixCardSurface)
        .border(0.8.dp, Color(0x1FFFFFFF), RoundedCornerShape(8.dp))
        .clickable(
          interactionSource = interactionSource,
          indication = null,
          onClick = onClick
        )
        .testTag("media_card_${mediaItem.id}")
    ) {
      // Neutral Background Placeholder (used when loading or if image is unavailable)
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(CardPlaceholderBrush),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Tv,
            contentDescription = null,
            tint = Color(0x33FFFFFF),
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = mediaItem.title,
            color = CinemaWhite.copy(alpha = 0.7f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      // Real Poster Image from API with caching and error handling
      if (mediaItem.posterUrl.isNotBlank() && !imageLoadFailed) {
        val context = LocalContext.current
        val imageRequest = remember(mediaItem.posterUrl) {
          ImageRequest.Builder(context)
            .data(mediaItem.posterUrl)
            .crossfade(100)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
        }
        AsyncImage(
          model = imageRequest,
          contentDescription = mediaItem.title,
          contentScale = ContentScale.Crop,
          onError = { imageLoadFailed = true },
          modifier = Modifier.fillMaxSize()
        )
      }

      // Quality / Platform tag in top-left
      val qualityTag = when {
        mediaItem.id.startsWith("a") -> "CR"
        mediaItem.audioFormats.contains("IMAX") -> "IMAX"
        mediaItem.mediaType == MediaType.SERIES -> "SERIES"
        else -> "4K"
      }

      Box(
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(5.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(Color(0xD9080A0F))
          .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(4.dp))
          .padding(horizontal = 5.dp, vertical = 2.dp)
      ) {
        Text(
          text = qualityTag,
          color = if (qualityTag == "CR") Color(0xFFF47521) else CinemaWhite,
          fontSize = 8.sp,
          fontWeight = FontWeight.Bold
        )
      }

      // Top Right: Bookmark button if supplied
      if (onBookmarkClick != null) {
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(4.dp)
            .size(28.dp)
            .clip(CircleShape)
            .background(Color(0xCC000000))
            .clickable { onBookmarkClick() },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
            contentDescription = "Bookmark",
            tint = if (isBookmarked) Color(0xFFF59E0B) else CinemaWhite,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      // Coming Soon Badge
      if (mediaItem.isComingSoon) {
        Box(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            .background(Color(0xFFF59E0B))
            .padding(vertical = 3.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "COMING SOON",
            color = Color(0xFF0A0C10),
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.6.sp
          )
        }
      }
    }

    // Optional metadata row (used in Search grid, My List, Anime catalog)
    if (showMetaInfo) {
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = mediaItem.title,
        color = CinemaWhite,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(11.dp)
          )
          Text(
            text = "%.1f".format(mediaItem.rating),
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 2.dp)
          )
        }
        Text(
          text = if (mediaItem.isComingSoon) "Soon" else mediaItem.releaseYear.toString(),
          color = if (mediaItem.isComingSoon) Color(0xFFF59E0B) else TextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}
