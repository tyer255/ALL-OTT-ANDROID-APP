package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.model.MediaItem
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.NetflixBlack
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

@Composable
fun HeroBanner(
  items: List<MediaItem>,
  currentIndex: Int,
  onIndexSelect: (Int) -> Unit,
  onPlayClick: (MediaItem) -> Unit,
  onDetailClick: (MediaItem) -> Unit,
  onToggleMyList: (MediaItem) -> Unit,
  isInMyList: Boolean,
  modifier: Modifier = Modifier
) {
  if (items.isEmpty()) return

  var activeIndex by remember { mutableIntStateOf(currentIndex.coerceIn(0, items.size - 1)) }
  val scope = rememberCoroutineScope()
  val animOffset = remember { Animatable(0f) }
  var dragOffsetPx by remember { mutableFloatStateOf(0f) }
  var isDragging by remember { mutableStateOf(false) }

  LaunchedEffect(currentIndex) {
    if (currentIndex != activeIndex && currentIndex in items.indices) {
      activeIndex = currentIndex
    }
  }

  // Pre-calculate the 3 visible stacked notebook pages
  val frontItem = items[activeIndex]
  val secondIndex = (activeIndex + 1) % items.size
  val secondItem = items[secondIndex]
  val thirdIndex = (activeIndex + 2) % items.size
  val thirdItem = items[thirdIndex]

  val context = LocalContext.current
  val frontImageRequest = remember(frontItem.posterUrl) {
    ImageRequest.Builder(context)
      .data(frontItem.posterUrl)
      .crossfade(100)
      .memoryCachePolicy(CachePolicy.ENABLED)
      .diskCachePolicy(CachePolicy.ENABLED)
      .build()
  }
  val secondImageRequest = remember(secondItem.posterUrl) {
    ImageRequest.Builder(context)
      .data(secondItem.posterUrl)
      .crossfade(100)
      .memoryCachePolicy(CachePolicy.ENABLED)
      .diskCachePolicy(CachePolicy.ENABLED)
      .build()
  }
  val thirdImageRequest = remember(thirdItem.posterUrl) {
    ImageRequest.Builder(context)
      .data(thirdItem.posterUrl)
      .crossfade(100)
      .memoryCachePolicy(CachePolicy.ENABLED)
      .diskCachePolicy(CachePolicy.ENABLED)
      .build()
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(top = 4.dp, bottom = 8.dp)
      .testTag("hero_banner")
  ) {
    // Notebook Layered Stacked Deck Container
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(490.dp)
        .padding(start = 14.dp, end = 14.dp)
        .pointerInput(Unit) {
          detectHorizontalDragGestures(
            onDragStart = {
              isDragging = true
              dragOffsetPx = animOffset.value
            },
            onDragCancel = {
              isDragging = false
              scope.launch {
                animOffset.snapTo(dragOffsetPx)
                animOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                dragOffsetPx = 0f
              }
            },
            onDragEnd = {
              isDragging = false
              scope.launch {
                val threshold = 120f
                animOffset.snapTo(dragOffsetPx)
                if (dragOffsetPx < -threshold) {
                  // Swiped left -> animate out and advance to next page
                  animOffset.animateTo(-600f, tween(160))
                  val nextIdx = (activeIndex + 1) % items.size
                  activeIndex = nextIdx
                  onIndexSelect(nextIdx)
                  animOffset.snapTo(0f)
                  dragOffsetPx = 0f
                } else if (dragOffsetPx > threshold) {
                  // Swiped right -> go to previous page
                  animOffset.animateTo(600f, tween(160))
                  val prevIdx = if (activeIndex > 0) activeIndex - 1 else items.size - 1
                  activeIndex = prevIdx
                  onIndexSelect(prevIdx)
                  animOffset.snapTo(0f)
                  dragOffsetPx = 0f
                } else {
                  // Return back to center
                  animOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                  dragOffsetPx = 0f
                }
              }
            },
            onHorizontalDrag = { _, dragAmount ->
              // Synchronous float update: zero coroutines launched during gesture tracking
              dragOffsetPx += dragAmount
            }
          )
        }
    ) {
      // ----------------------------------------------------
      // PAGE 3 (Furthest Behind - 3rd Notebook Layer on right)
      // ----------------------------------------------------
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(end = 0.dp)
          .offset(x = 36.dp, y = 0.dp)
          .graphicsLayer {
            scaleX = 0.90f
            scaleY = 0.90f
            alpha = 0.65f
          }
          .zIndex(1f)
          .clip(RoundedCornerShape(26.dp))
          .background(Color(0xFF0C0E14))
          .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(26.dp))
          .clickable {
            scope.launch {
              animOffset.snapTo(0f)
              dragOffsetPx = 0f
              activeIndex = thirdIndex
              onIndexSelect(thirdIndex)
            }
          }
      ) {
        AsyncImage(
          model = thirdImageRequest,
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
        )
      }

      // ----------------------------------------------------
      // PAGE 2 (Middle Layer - 2nd Notebook Layer on right)
      // ----------------------------------------------------
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(end = 0.dp)
          .offset(x = 18.dp, y = 0.dp)
          .graphicsLayer {
            scaleX = 0.95f
            scaleY = 0.95f
            alpha = 0.88f
          }
          .zIndex(2f)
          .clip(RoundedCornerShape(26.dp))
          .background(Color(0xFF10131A))
          .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(26.dp))
          .clickable {
            scope.launch {
              animOffset.snapTo(0f)
              dragOffsetPx = 0f
              activeIndex = secondIndex
              onIndexSelect(secondIndex)
            }
          }
      ) {
        AsyncImage(
          model = secondImageRequest,
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
        )
      }

      // ----------------------------------------------------
      // PAGE 1 (Front Active Card - Fully interactive)
      // Hardware-accelerated graphicsLayer translation: bypasses relayout/recomposition
      // ----------------------------------------------------
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(end = 36.dp) // Reserves breathing room so pages behind peek on the right edge!
          .graphicsLayer {
            translationX = if (isDragging) dragOffsetPx else animOffset.value
          }
          .zIndex(3f)
          .shadow(16.dp, RoundedCornerShape(26.dp))
          .clip(RoundedCornerShape(26.dp))
          .background(Color(0xFF14171E))
          .border(
            width = 1.dp,
            brush = Brush.linearGradient(
              listOf(
                Color(0x40FFFFFF),
                Color(0x1AFFFFFF),
                Color(0x05FFFFFF)
              )
            ),
            shape = RoundedCornerShape(26.dp)
          )
          .clickable { onDetailClick(frontItem) }
      ) {
        // Poster image
        AsyncImage(
          model = frontImageRequest,
          contentDescription = frontItem.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        // Dark Vignette Gradient
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color.Black.copy(alpha = 0.4f),
                  Color.Transparent,
                  Color.Black.copy(alpha = 0.35f),
                  Color.Black.copy(alpha = 0.85f),
                  Color.Black.copy(alpha = 0.98f)
                ),
                startY = 0f,
                endY = Float.POSITIVE_INFINITY
              )
            )
        )

        // Top Left: Golden Medal Rank Badge (#3 in Hindi Today from screenshot)
        val rankNumber = (activeIndex + 1)
        Box(
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(start = 14.dp, top = 14.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x99121620))
            .border(0.8.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🏅", fontSize = 13.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "#$rankNumber in Hindi Today",
              color = CinemaWhite,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.2.sp
            )
          }
        }

        // Bottom Content Section: Title, Meta, and Right-Aligned Liquid Glass Action Buttons
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Bottom
        ) {
          // Left Content Info
          Column(
            modifier = Modifier
              .weight(1f)
              .padding(end = 10.dp)
          ) {
            // Brand Prefix: "aether specials"
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "aether ",
                color = CinemaWhite.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "specials",
                color = Color(0xFF00D9F5),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.3.sp
              )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Large Stylized Cinema Title
            Text(
              text = frontItem.title.uppercase(),
              color = CinemaWhite,
              fontSize = 22.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 1.sp,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Year • Languages • Genre
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = frontItem.releaseYear.toString(),
                color = CinemaWhite.copy(alpha = 0.9f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
              Text(text = "•", color = TextMuted, fontSize = 11.sp)
              Text(
                text = "8 Languages",
                color = CinemaWhite.copy(alpha = 0.9f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
              Text(text = "•", color = TextMuted, fontSize = 11.sp)
              Text(
                text = frontItem.genres.firstOrNull() ?: "Crime",
                color = CinemaWhite.copy(alpha = 0.9f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Social Proof Hook with Fire Emoji
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(top = 2.dp)
            ) {
              Text(text = "🔥", fontSize = 12.sp)
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = "Popular in India Today",
                color = CinemaWhite.copy(alpha = 0.95f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          // Right-Stacked Liquid Glass Action Buttons
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Plus Button (+) in Liquid Glass Frosted Shell
            Box(
              modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(
                  Brush.verticalGradient(
                    listOf(
                      Color(0x80354153),
                      Color(0x661E2632)
                    )
                  )
                )
                .border(
                  width = 1.2.dp,
                  brush = Brush.linearGradient(
                    listOf(
                      Color(0x66FFFFFF),
                      Color(0x26FFFFFF)
                    )
                  ),
                  shape = CircleShape
                )
                .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = null,
                  onClick = { onToggleMyList(frontItem) }
                )
                .testTag("hero_my_list_button"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (isInMyList) Icons.Default.Check else Icons.Outlined.Add,
                contentDescription = "Add to Watchlist",
                tint = if (isInMyList) Color(0xFF00E5FF) else CinemaWhite,
                modifier = Modifier.size(24.dp)
              )
            }

            // Play Button (▶) in Liquid Chrome Pearl Gradient
            Box(
              modifier = Modifier
                .size(60.dp)
                .shadow(elevation = 8.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(
                  Brush.linearGradient(
                    listOf(
                      Color(0xFFE8EEF5),
                      Color(0xFFC8D3E0),
                      Color(0xFFA2B0C2)
                    )
                  )
                )
                .border(
                  width = 1.5.dp,
                  brush = Brush.linearGradient(
                    listOf(
                      Color(0xFFFFFFFF),
                      Color(0x66FFFFFF),
                      Color(0x22FFFFFF)
                    )
                  ),
                  shape = CircleShape
                )
                .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = null,
                  onClick = { onPlayClick(frontItem) }
                )
                .testTag("hero_watch_now_button"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = NetflixBlack,
                modifier = Modifier
                  .size(32.dp)
                  .padding(start = 2.dp)
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Dots indicator showing current card in deck
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      items.forEachIndexed { idx, _ ->
        val isSelected = idx == activeIndex
        Box(
          modifier = Modifier
            .padding(horizontal = 3.dp)
            .height(3.dp)
            .width(if (isSelected) 18.dp else 5.dp)
            .clip(CircleShape)
            .background(if (isSelected) Color(0xFF00D9F5) else Color(0x33FFFFFF))
            .clickable {
              scope.launch {
                animOffset.snapTo(0f)
                dragOffsetPx = 0f
                activeIndex = idx
                onIndexSelect(idx)
              }
            }
        )
      }
    }
  }
}
