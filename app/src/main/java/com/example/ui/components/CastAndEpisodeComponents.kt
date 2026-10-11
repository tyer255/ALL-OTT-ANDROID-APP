package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
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
import coil.request.ImageRequest
import com.example.data.model.CastMember
import com.example.data.model.Episode
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WhiteTextPrimary
import com.example.ui.theme.liquidGlassSurface
import com.example.ui.theme.neumorphicDepth

/**
 * Premium 2026 Liquid Glass Cast Member Card.
 * Uses verified actor portrait images with a clean, neutral initials/person placeholder fallback.
 * NEVER displays an unrelated stock photo.
 */
@Composable
fun CastCard(
  castMember: CastMember,
  modifier: Modifier = Modifier
) {
  val initials = remember(castMember.name) {
    val parts = castMember.name.trim().split(" ")
    if (parts.size >= 2) {
      "${parts[0].firstOrNull()?.uppercase() ?: ""}${parts[1].firstOrNull()?.uppercase() ?: ""}"
    } else {
      castMember.name.take(2).uppercase()
    }
  }

  var imageLoadFailed by remember { mutableStateOf(false) }

  Column(
    modifier = modifier.width(80.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(68.dp)
        .neumorphicDepth(
          shape = RoundedCornerShape(20.dp),
          elevation = 3.dp,
          baseColor = Color(0xFF151A24)
        ),
      contentAlignment = Alignment.Center
    ) {
      // Neutral placeholder with initials & person icon
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.linearGradient(
              listOf(Color(0xFF222B3D), Color(0xFF141924))
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = Color(0x66FFFFFF),
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = initials,
            color = CinemaWhite.copy(alpha = 0.8f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      // Load verified portrait if URL is non-empty and hasn't failed
      if (castMember.profileImageUrl.isNotBlank() && !imageLoadFailed) {
        AsyncImage(
          model = ImageRequest.Builder(LocalContext.current)
            .data(castMember.profileImageUrl)
            .crossfade(200)
            .build(),
          contentDescription = castMember.name,
          contentScale = ContentScale.Crop,
          onError = { imageLoadFailed = true },
          modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = castMember.name,
      color = WhiteTextPrimary,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
    Text(
      text = castMember.character,
      color = TextMuted,
      fontSize = 10.sp,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

/**
 * Premium 2026 Liquid Glass Episode Card.
 */
@Composable
fun EpisodeCard(
  episode: Episode,
  onPlayEpisode: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .liquidGlassSurface(
        shape = RoundedCornerShape(12.dp),
        elevation = 2.dp,
        backgroundColor = Color(0x99151A25),
        borderColor = GlassTokens.BorderHighlight
      )
      .clickable(onClick = onPlayEpisode)
      .padding(12.dp)
      .testTag("episode_${episode.id}")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .width(120.dp)
          .aspectRatio(16f / 9f)
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFF0F131C))
      ) {
        AsyncImage(
          model = episode.thumbnailUrl,
          contentDescription = episode.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f)),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(Color(0xCC000000))
              .border(1.dp, Color(0x4DFFFFFF), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Play Episode",
              tint = CinemaWhite,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "E${episode.episodeNumber} • ${episode.title}",
          color = WhiteTextPrimary,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(Color(0x26FFFFFF))
              .padding(horizontal = 5.dp, vertical = 2.dp)
          ) {
            Text(
              text = episode.duration,
              color = CinemaWhite,
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
          if (episode.rating > 0.0) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "★ ${"%.1f".format(episode.rating)}",
              color = Color(0xFFF59E0B),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    if (episode.overview.isNotBlank()) {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = episode.overview,
        color = TextMuted,
        fontSize = 11.sp,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 16.sp
      )
    }
  }
}
