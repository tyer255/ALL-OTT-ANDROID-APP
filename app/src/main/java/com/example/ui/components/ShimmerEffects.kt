package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Animated Shimmer modifier providing smooth 2026 skeleton loading without flickering.
 */
fun Modifier.shimmerLoading(
  shape: Shape = RoundedCornerShape(8.dp),
  highlightColor: Color = Color(0xFF2A3242),
  baseColor: Color = Color(0xFF141822)
): Modifier = this
  .clip(shape)
  .then(
    Modifier.background(baseColor)
  )

@Composable
fun ShimmerBrush(): Brush {
  val transition = rememberInfiniteTransition(label = "shimmer_transition")
  val translateAnim = transition.animateFloat(
    initialValue = 0f,
    targetValue = 1000f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "shimmer_anim"
  )

  return Brush.linearGradient(
    colors = listOf(
      Color(0xFF131722),
      Color(0xFF242C3D),
      Color(0xFF131722)
    ),
    start = Offset(translateAnim.value - 400f, translateAnim.value - 400f),
    end = Offset(translateAnim.value, translateAnim.value)
  )
}

@Composable
fun ShimmerBox(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(8.dp)
) {
  val brush = ShimmerBrush()
  Box(
    modifier = modifier
      .clip(shape)
      .background(brush)
      .border(0.5.dp, Color(0x1AFFFFFF), shape)
  )
}

/**
 * Skeleton Media Poster Card (2:3 aspect ratio).
 */
@Composable
fun MovieCardSkeleton(
  cardWidth: Dp = 118.dp,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.width(cardWidth)
  ) {
    ShimmerBox(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(2f / 3f),
      shape = RoundedCornerShape(8.dp)
    )
    Spacer(modifier = Modifier.height(6.dp))
    ShimmerBox(
      modifier = Modifier
        .width(80.dp)
        .height(10.dp),
      shape = RoundedCornerShape(4.dp)
    )
    Spacer(modifier = Modifier.height(4.dp))
    ShimmerBox(
      modifier = Modifier
        .width(48.dp)
        .height(8.dp),
      shape = RoundedCornerShape(4.dp)
    )
  }
}

/**
 * Skeleton horizontal content row with title placeholder and 4-5 cards.
 */
@Composable
fun ContentRowSkeleton(
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 12.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      ShimmerBox(
        modifier = Modifier
          .width(140.dp)
          .height(18.dp),
        shape = RoundedCornerShape(4.dp)
      )
      ShimmerBox(
        modifier = Modifier
          .width(50.dp)
          .height(12.dp),
        shape = RoundedCornerShape(4.dp)
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(5) {
        MovieCardSkeleton(cardWidth = 118.dp)
      }
    }
  }
}

/**
 * Skeleton hero banner placeholder.
 */
@Composable
fun HeroBannerSkeleton(
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .aspectRatio(16f / 11f)
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    ShimmerBox(
      modifier = Modifier.fillMaxSize(),
      shape = RoundedCornerShape(16.dp)
    )

    Column(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(16.dp)
    ) {
      ShimmerBox(
        modifier = Modifier
          .width(90.dp)
          .height(12.dp),
        shape = RoundedCornerShape(4.dp)
      )
      Spacer(modifier = Modifier.height(8.dp))
      ShimmerBox(
        modifier = Modifier
          .width(200.dp)
          .height(24.dp),
        shape = RoundedCornerShape(6.dp)
      )
      Spacer(modifier = Modifier.height(12.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ShimmerBox(
          modifier = Modifier
            .width(110.dp)
            .height(38.dp),
          shape = RoundedCornerShape(8.dp)
        )
        ShimmerBox(
          modifier = Modifier
            .width(100.dp)
            .height(38.dp),
          shape = RoundedCornerShape(8.dp)
        )
      }
    }
  }
}

/**
 * Skeleton cast profile avatar + labels.
 */
@Composable
fun CastCardSkeleton(
  modifier: Modifier = Modifier
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.width(80.dp)
  ) {
    ShimmerBox(
      modifier = Modifier.size(68.dp),
      shape = RoundedCornerShape(20.dp)
    )
    Spacer(modifier = Modifier.height(6.dp))
    ShimmerBox(
      modifier = Modifier
        .width(60.dp)
        .height(10.dp),
      shape = RoundedCornerShape(3.dp)
    )
    Spacer(modifier = Modifier.height(4.dp))
    ShimmerBox(
      modifier = Modifier
        .width(44.dp)
        .height(8.dp),
      shape = RoundedCornerShape(3.dp)
    )
  }
}

/**
 * Skeleton episode item.
 */
@Composable
fun EpisodeCardSkeleton(
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .background(Color(0xFF141924))
      .border(0.8.dp, Color(0x1AFFFFFF), RoundedCornerShape(10.dp))
      .padding(10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    ShimmerBox(
      modifier = Modifier
        .width(120.dp)
        .aspectRatio(16f / 9f),
      shape = RoundedCornerShape(6.dp)
    )
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      ShimmerBox(
        modifier = Modifier
          .fillMaxWidth(0.8f)
          .height(14.dp),
        shape = RoundedCornerShape(4.dp)
      )
      Spacer(modifier = Modifier.height(6.dp))
      ShimmerBox(
        modifier = Modifier
          .width(60.dp)
          .height(10.dp),
        shape = RoundedCornerShape(3.dp)
      )
      Spacer(modifier = Modifier.height(8.dp))
      ShimmerBox(
        modifier = Modifier
          .fillMaxWidth()
          .height(20.dp),
        shape = RoundedCornerShape(4.dp)
      )
    }
  }
}

/**
 * Skeleton server card for Streaming Server Selection.
 */
@Composable
fun ServerCardSkeleton(
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(Color(0xFF151A25))
      .border(0.8.dp, Color(0x26FFFFFF), RoundedCornerShape(14.dp))
      .padding(14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.weight(1f)
    ) {
      ShimmerBox(
        modifier = Modifier.size(38.dp),
        shape = CircleShape
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        ShimmerBox(
          modifier = Modifier
            .width(150.dp)
            .height(14.dp),
          shape = RoundedCornerShape(4.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        ShimmerBox(
          modifier = Modifier
            .width(90.dp)
            .height(10.dp),
          shape = RoundedCornerShape(3.dp)
        )
      }
    }
    ShimmerBox(
      modifier = Modifier
        .width(55.dp)
        .height(26.dp),
      shape = RoundedCornerShape(6.dp)
    )
  }
}

/**
 * Skeleton grid for search results or catalog browsing.
 */
@Composable
fun SearchGridSkeleton(
  modifier: Modifier = Modifier
) {
  LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 100.dp),
    contentPadding = PaddingValues(16.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = modifier.fillMaxSize()
  ) {
    items(9) {
      ShimmerBox(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(2f / 3f),
        shape = RoundedCornerShape(8.dp)
      )
    }
  }
}

/**
 * Full DetailScreen skeleton loader.
 */
@Composable
fun DetailScreenSkeleton(
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black)
  ) {
    // Backdrop skeleton
    ShimmerBox(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(16f / 10f),
      shape = RoundedCornerShape(0.dp)
    )

    Column(modifier = Modifier.padding(16.dp)) {
      ShimmerBox(
        modifier = Modifier
          .fillMaxWidth(0.6f)
          .height(24.dp),
        shape = RoundedCornerShape(6.dp)
      )
      Spacer(modifier = Modifier.height(8.dp))
      ShimmerBox(
        modifier = Modifier
          .width(180.dp)
          .height(14.dp),
        shape = RoundedCornerShape(4.dp)
      )
      Spacer(modifier = Modifier.height(16.dp))
      ShimmerBox(
        modifier = Modifier
          .fillMaxWidth()
          .height(46.dp),
        shape = RoundedCornerShape(10.dp)
      )
      Spacer(modifier = Modifier.height(12.dp))
      ShimmerBox(
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        shape = RoundedCornerShape(8.dp)
      )
      Spacer(modifier = Modifier.height(20.dp))
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        repeat(4) {
          CastCardSkeleton()
        }
      }
    }
  }
}
