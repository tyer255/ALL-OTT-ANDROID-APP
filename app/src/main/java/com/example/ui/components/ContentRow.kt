package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItem
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.WhiteTextPrimary
import kotlinx.coroutines.launch

@Composable
fun ContentRow(
  title: String,
  items: List<MediaItem>,
  onItemClick: (MediaItem) -> Unit,
  modifier: Modifier = Modifier,
  subtitle: String? = null
) {
  if (items.isEmpty()) return

  // Vertical scroll entrance animation (Row fades in and glides up smoothly as user scrolls)
  val rowAlpha = remember { Animatable(0f) }
  val rowSlideY = remember { Animatable(20f) }

  LaunchedEffect(title) {
    launch {
      rowAlpha.animateTo(1f, tween(320))
    }
    launch {
      rowSlideY.animateTo(0f, spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow))
    }
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .graphicsLayer {
        alpha = rowAlpha.value
        translationY = rowSlideY.value
      }
      .padding(vertical = 10.dp)
      .testTag("content_row_${title.lowercase().replace(" ", "_")}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = title,
          color = WhiteTextPrimary,
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.2.sp
        )
        if (subtitle != null) {
          Text(
            text = subtitle,
            color = MutedSlate,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Horizontal Swipeable Cards Row with Staggered Side-Swipe Animations
    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
        MediaCard(
          mediaItem = item,
          onClick = { onItemClick(item) },
          staggerIndex = index
        )
      }
    }
  }
}
