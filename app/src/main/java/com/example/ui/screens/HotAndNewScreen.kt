package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaItem
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.NetflixBlack
import com.example.ui.theme.NetflixCardSurface
import com.example.ui.theme.NetflixRed
import com.example.ui.theme.TextMuted

@Composable
fun HotAndNewScreen(
  items: List<MediaItem>,
  onMediaClick: (MediaItem) -> Unit,
  onPlayClick: (MediaItem) -> Unit,
  modifier: Modifier = Modifier
) {
  val reminders = remember { mutableStateMapOf<String, Boolean>() }
  var selectedTab by remember { mutableStateOf("Coming Soon") }
  val tabs = listOf("Coming Soon", "Everyone's Watching", "Top 10")

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(NetflixBlack)
      .statusBarsPadding()
      .padding(bottom = 70.dp)
      .testTag("hot_and_new_screen")
  ) {
    // Screen Title
    Text(
      text = "New & Hot",
      color = CinemaWhite,
      fontSize = 24.sp,
      fontWeight = FontWeight.Black,
      letterSpacing = (-0.5).sp,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )

    // Pill Tab Selectors
    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.padding(bottom = 12.dp)
    ) {
      items(tabs) { tab ->
        val isSelected = tab == selectedTab
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) CinemaWhite else Color(0xFF262626))
            .clickable { selectedTab = tab }
            .padding(horizontal = 14.dp, vertical = 7.dp)
        ) {
          Text(
            text = tab,
            color = if (isSelected) NetflixBlack else CinemaWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    LazyColumn(
      contentPadding = PaddingValues(bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(24.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(items, key = { it.id }) { item ->
        val hasReminder = reminders[item.id] == true
        var isMuted by remember { mutableStateOf(true) }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          verticalAlignment = Alignment.Top
        ) {
          // Left Date Column (Netflix signature)
          Column(
            modifier = Modifier.width(46.dp),
            horizontalAlignment = Alignment.Start
          ) {
            Text(
              text = "NOV",
              color = TextMuted,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "14",
              color = CinemaWhite,
              fontSize = 22.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = (-1).sp
            )
          }

          // Right Content Column
          Column(
            modifier = Modifier
              .weight(1f)
              .clickable { onMediaClick(item) }
          ) {
            // Video / Trailer Preview Card
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(6.dp))
                .background(NetflixCardSurface)
            ) {
              AsyncImage(
                model = item.backdropUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )

              // Centered Play Button
              Box(
                modifier = Modifier
                  .align(Alignment.Center)
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(Color.Black.copy(alpha = 0.6f))
                  .clickable { onPlayClick(item) },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.PlayArrow,
                  contentDescription = "Play Trailer",
                  tint = CinemaWhite,
                  modifier = Modifier.size(28.dp)
                )
              }

              // Sound Mute Toggle Button
              Box(
                modifier = Modifier
                  .align(Alignment.BottomEnd)
                  .padding(8.dp)
                  .size(28.dp)
                  .clip(CircleShape)
                  .background(Color.Black.copy(alpha = 0.7f))
                  .clickable { isMuted = !isMuted },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                  contentDescription = "Mute",
                  tint = CinemaWhite,
                  modifier = Modifier.size(16.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row: Remind Me + Info
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = item.title,
                color = CinemaWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.weight(1f)
              )

              Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier.clickable { reminders[item.id] = !hasReminder }
                ) {
                  Icon(
                    imageVector = if (hasReminder) Icons.Default.Check else Icons.Default.NotificationsNone,
                    contentDescription = null,
                    tint = if (hasReminder) NetflixRed else CinemaWhite,
                    modifier = Modifier.size(20.dp)
                  )
                  Text(
                    text = if (hasReminder) "Reminded" else "Remind Me",
                    color = TextMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 2.dp)
                  )
                }

                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier.clickable { onMediaClick(item) }
                ) {
                  Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = CinemaWhite,
                    modifier = Modifier.size(20.dp)
                  )
                  Text(
                    text = "Info",
                    color = TextMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 2.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = "Coming Friday",
              color = CinemaWhite.copy(alpha = 0.9f),
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )

            Text(
              text = item.overview,
              color = TextMuted,
              fontSize = 12.sp,
              lineHeight = 17.sp,
              maxLines = 3,
              modifier = Modifier.padding(top = 4.dp)
            )

            if (item.genres.isNotEmpty()) {
              Text(
                text = item.genres.joinToString(" • "),
                color = CinemaWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 6.dp)
              )
            }
          }
        }
      }
    }
  }
}
