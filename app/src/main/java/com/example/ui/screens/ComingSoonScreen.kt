package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaItem
import com.example.ui.theme.AmberGoldPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DividerColor
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.WhiteTextPrimary

@Composable
fun ComingSoonScreen(
  items: List<MediaItem>,
  onBackClick: () -> Unit,
  onMediaClick: (MediaItem) -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBackClick() }

  val reminders = remember { mutableStateMapOf<String, Boolean>() }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .statusBarsPadding()
      .testTag("coming_soon_screen")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onBackClick,
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(DarkSurface)
          .testTag("coming_soon_back_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = WhiteTextPrimary,
          modifier = Modifier.size(20.dp)
        )
      }

      Text(
        text = "Coming Soon",
        color = WhiteTextPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 12.dp)
      )
    }

    LazyColumn(
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(items, key = { it.id }) { item ->
        val hasReminder = reminders[item.id] == true

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DividerColor, RoundedCornerShape(12.dp))
            .clickable { onMediaClick(item) }
            .padding(14.dp)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(16f / 9f)
              .clip(RoundedCornerShape(8.dp))
              .background(DarkSurfaceVariant)
          ) {
            AsyncImage(
              model = item.backdropUrl,
              contentDescription = item.title,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )

            Box(
              modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(AmberGoldPrimary)
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "COMING SOON",
                color = DarkBackground,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Event,
                contentDescription = null,
                tint = AmberGoldPrimary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Arriving ${item.releaseDate ?: "Late 2025"}",
                color = WhiteTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
            }

            Button(
              onClick = {
                reminders[item.id] = !hasReminder
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (hasReminder) DarkSurfaceVariant else AmberGoldPrimary,
                contentColor = if (hasReminder) AmberGoldPrimary else DarkBackground
              ),
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier.height(34.dp)
            ) {
              Icon(
                imageVector = if (hasReminder) Icons.Default.Check else Icons.Default.NotificationsNone,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (hasReminder) "Reminded" else "Remind Me",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = item.title,
            color = WhiteTextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
          )

          if (item.genres.isNotEmpty()) {
            Text(
              text = item.genres.joinToString(" • "),
              color = AmberGoldPrimary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              modifier = Modifier.padding(vertical = 2.dp)
            )
          }

          Text(
            text = item.overview,
            color = MutedSlate,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            modifier = Modifier.padding(top = 4.dp)
          )
        }
      }
    }
  }
}
