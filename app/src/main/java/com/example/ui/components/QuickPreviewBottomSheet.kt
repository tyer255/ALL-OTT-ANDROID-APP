package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaItem
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.NetflixBlack
import com.example.ui.theme.NetflixRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.liquidGlassSurface
import com.example.ui.theme.neumorphicDepth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickPreviewBottomSheet(
  mediaItem: MediaItem?,
  isInMyList: Boolean,
  onDismiss: () -> Unit,
  onPlayClick: (MediaItem) -> Unit,
  onToggleMyList: (MediaItem) -> Unit,
  onOpenFullDetail: (MediaItem) -> Unit,
  modifier: Modifier = Modifier
) {
  if (mediaItem == null) return

  var isLiked by remember { mutableStateOf(false) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    containerColor = Color(0xF00D1017),
    dragHandle = {
      Box(
        modifier = Modifier
          .padding(top = 10.dp, bottom = 4.dp)
          .width(36.dp)
          .height(4.dp)
          .clip(CircleShape)
          .background(Color(0x40FFFFFF))
      )
    },
    modifier = modifier.testTag("quick_preview_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 24.dp)
    ) {
      // Header: Poster Thumbnail + Metadata
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
      ) {
        Box(
          modifier = Modifier
            .width(100.dp)
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF161A24))
            .border(1.dp, GlassTokens.BorderHighlight, RoundedCornerShape(10.dp))
        ) {
          AsyncImage(
            model = mediaItem.posterUrl,
            contentDescription = mediaItem.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
          ) {
            Text(
              text = mediaItem.title,
              color = CinemaWhite,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f)
            )

            IconButton(
              onClick = onDismiss,
              modifier = Modifier
                .size(30.dp)
                .neumorphicDepth(shape = CircleShape, elevation = 1.dp, baseColor = Color(0xFF161B24))
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = CinemaWhite,
                modifier = Modifier.size(16.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = mediaItem.releaseYear.toString(),
              color = TextMuted,
              fontSize = 12.sp
            )
            MaturityBadge(text = mediaItem.maturityRating)
            Text(
              text = mediaItem.duration,
              color = TextMuted,
              fontSize = 12.sp
            )
            FormatBadge(text = "FHD")
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = mediaItem.overview,
            color = CinemaWhite.copy(alpha = 0.82f),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Action Buttons Row: Play & Details
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = {
            onDismiss()
            onPlayClick(mediaItem)
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = CinemaWhite,
            contentColor = NetflixBlack
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .testTag("preview_play_button")
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = NetflixBlack,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Play Now",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .liquidGlassSurface(
              shape = RoundedCornerShape(10.dp),
              elevation = 2.dp,
              backgroundColor = Color(0x66222B3D),
              borderColor = GlassTokens.BorderHighlight
            )
            .clickable {
              onDismiss()
              onOpenFullDetail(mediaItem)
            },
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Full Details",
              color = CinemaWhite,
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
              contentDescription = null,
              tint = TextMuted,
              modifier = Modifier.size(12.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Utility Row: My List, Rate, Share
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceAround
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.clickable { onToggleMyList(mediaItem) }
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(if (isInMyList) Color(0x33E50914) else Color(0x1AFFFFFF))
              .border(0.8.dp, if (isInMyList) NetflixRed else Color(0x26FFFFFF), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isInMyList) Icons.Default.Check else Icons.Outlined.Add,
              contentDescription = null,
              tint = if (isInMyList) NetflixRed else CinemaWhite,
              modifier = Modifier.size(20.dp)
            )
          }
          Text(
            text = if (isInMyList) "In Watchlist" else "Add Watchlist",
            color = TextMuted,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 4.dp)
          )
        }

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.clickable { isLiked = !isLiked }
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(if (isLiked) Color(0x33E50914) else Color(0x1AFFFFFF))
              .border(0.8.dp, if (isLiked) NetflixRed else Color(0x26FFFFFF), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
              contentDescription = null,
              tint = if (isLiked) NetflixRed else CinemaWhite,
              modifier = Modifier.size(18.dp)
            )
          }
          Text(
            text = if (isLiked) "Liked" else "Like",
            color = TextMuted,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 4.dp)
          )
        }

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.clickable { /* Share */ }
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(Color(0x1AFFFFFF))
              .border(0.8.dp, Color(0x26FFFFFF), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = null,
              tint = CinemaWhite,
              modifier = Modifier.size(18.dp)
            )
          }
          Text(
            text = "Share",
            color = TextMuted,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 4.dp)
          )
        }
      }
    }
  }
}
