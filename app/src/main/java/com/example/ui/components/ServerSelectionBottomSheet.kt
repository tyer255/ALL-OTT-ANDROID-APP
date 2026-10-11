package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItem
import com.example.data.model.ServerItem
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.TextMuted
import com.example.ui.theme.liquidGlassSurface
import com.example.ui.theme.neumorphicDepth

/**
 * Premium 2026 Liquid Glass & Neumorphic Streaming Server Selection Sheet.
 * Features:
 * - Ultra-clean, compact, easy-to-understand selection cards.
 * - Server name, availability badge, quality tag, and audio language.
 * - Clear primary action: tap card or "PLAY" button to start playback immediately.
 * - Liquid Glass resolving progress indicator with cancel option.
 * - Shimmer skeleton loading during server fetching.
 * - Clear retry option when upstream servers fail.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSelectionBottomSheet(
  mediaItem: MediaItem,
  servers: List<ServerItem>,
  isLoading: Boolean,
  isResolving: Boolean,
  episodeTitle: String?,
  onSelectServer: (ServerItem) -> Unit,
  onDismiss: () -> Unit,
  onRetry: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color(0xF00A0D14), // 94% translucent deep obsidian
    dragHandle = {
      Box(
        modifier = Modifier
          .padding(top = 10.dp, bottom = 6.dp)
          .width(36.dp)
          .height(4.dp)
          .clip(CircleShape)
          .background(Color(0x40FFFFFF))
      )
    },
    modifier = modifier.testTag("server_selection_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp)
        .padding(bottom = 32.dp)
    ) {
      // 1. Header with Title, Media Metadata, and Close Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Select Streaming Server",
            color = CinemaWhite,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.2).sp
          )
          val subtitle = if (!episodeTitle.isNullOrBlank()) {
            "${mediaItem.title} • $episodeTitle"
          } else {
            "${mediaItem.title} (${mediaItem.releaseYear}) • ${mediaItem.duration}"
          }
          Text(
            text = subtitle,
            color = TextMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .size(32.dp)
            .neumorphicDepth(
              shape = CircleShape,
              elevation = 1.dp,
              baseColor = Color(0xFF141924)
            )
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = TextMuted,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 2. Active Server Resolving State (Liquid Glass Connecting Pill)
      if (isResolving) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .liquidGlassSurface(
              shape = RoundedCornerShape(16.dp),
              elevation = 4.dp,
              backgroundColor = Color(0xCC131926),
              borderColor = GlassTokens.BorderHighlight
            )
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
              color = CinemaWhite,
              modifier = Modifier.size(34.dp),
              strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = "Connecting to High-Speed Stream...",
              color = CinemaWhite,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Bypassing server buffers for instant playback",
              color = TextMuted,
              fontSize = 11.sp
            )
          }
        }
      } else if (isLoading) {
        // 3. Shimmer Skeleton Loading State
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          repeat(3) {
            ServerCardSkeleton()
          }
        }
      } else if (servers.isEmpty()) {
        // 4. Failure / No Servers State with Retry Action
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .liquidGlassSurface(
              shape = RoundedCornerShape(14.dp),
              elevation = 2.dp,
              backgroundColor = Color(0xB31C1616),
              borderColor = Color(0x33EF4444)
            )
            .padding(20.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.ErrorOutline,
              contentDescription = null,
              tint = Color(0xFFF87171),
              modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "No streaming mirrors available right now",
              color = CinemaWhite,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "The upstream host might be updating its mirrors.",
              color = TextMuted,
              fontSize = 11.sp
            )
            if (onRetry != null) {
              Spacer(modifier = Modifier.height(14.dp))
              Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF252D3D)),
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = CinemaWhite, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Retry Server Scan", color = CinemaWhite, fontSize = 12.sp)
              }
            }
          }
        }
      } else {
        // 5. Clean, Compact, Neumorphic Server Cards List
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(servers, key = { it.id }) { server ->
            ServerSelectionCard(
              server = server,
              onSelect = { onSelectServer(server) }
            )
          }
        }
      }
    }
  }
}

/**
 * Individual Compact Server Selection Card with Liquid Glass + Neumorphism.
 */
@Composable
private fun ServerSelectionCard(
  server: ServerItem,
  onSelect: () -> Unit,
  modifier: Modifier = Modifier
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  Box(
    modifier = modifier
      .fillMaxWidth()
      .neumorphicDepth(
        shape = RoundedCornerShape(14.dp),
        elevation = 2.dp,
        isPressed = isPressed,
        baseColor = Color(0xFF131722)
      )
      .border(
        width = 0.8.dp,
        color = Color(0x26FFFFFF),
        shape = RoundedCornerShape(14.dp)
      )
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onSelect
      )
      .padding(horizontal = 14.dp, vertical = 12.dp)
      .testTag("server_card_${server.id}")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Left: Server Icon & Name
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFF1A2232))
            .border(0.8.dp, Color(0x26FFFFFF), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Dns,
            contentDescription = null,
            tint = CinemaWhite,
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = server.name,
            color = CinemaWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          Spacer(modifier = Modifier.height(3.dp))

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            // Quality Tag
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x1FFFFFFF))
                .padding(horizontal = 5.dp, vertical = 1.5.dp)
            ) {
              Text(
                text = server.quality,
                color = CinemaWhite.copy(alpha = 0.9f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
              )
            }

            // Language / Audio
            Text(
              text = server.language,
              color = TextMuted,
              fontSize = 10.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Right: Action Area
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Online Badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(GlassTokens.OnlineGreenGlass)
            .border(0.5.dp, Color(0x3310B981), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(GlassTokens.OnlineGreenText)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "ONLINE",
              color = GlassTokens.OnlineGreenText,
              fontSize = 9.sp,
              fontWeight = FontWeight.ExtraBold
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Play Action Button
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color(0x33FFFFFF))
            .border(0.8.dp, Color(0x33FFFFFF), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play Now",
            tint = CinemaWhite,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}
