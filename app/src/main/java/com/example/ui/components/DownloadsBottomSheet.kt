package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DownloadsInfo
import com.example.data.model.MediaItem
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.TextMuted
import com.example.ui.theme.liquidGlassSurface
import com.example.ui.theme.neumorphicDepth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsBottomSheet(
  mediaItem: MediaItem,
  downloadsInfo: DownloadsInfo?,
  isLoading: Boolean,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val context = LocalContext.current

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color(0xF00A0D14),
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
    modifier = modifier.testTag("downloads_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Download Options",
            color = CinemaWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "${mediaItem.title} (${mediaItem.releaseYear})",
            color = TextMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp)
          )
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .size(32.dp)
            .neumorphicDepth(shape = CircleShape, elevation = 1.dp, baseColor = Color(0xFF141924))
        ) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(16.dp))
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (isLoading) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .liquidGlassSurface(shape = RoundedCornerShape(14.dp), elevation = 2.dp)
            .padding(vertical = 32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = CinemaWhite, modifier = Modifier.size(30.dp), strokeWidth = 2.5.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Verifying fast CDN download mirrors...", color = TextMuted, fontSize = 12.sp)
          }
        }
      } else if (downloadsInfo == null || downloadsInfo.downloads.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .liquidGlassSurface(shape = RoundedCornerShape(14.dp), elevation = 2.dp)
            .padding(vertical = 24.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No downloadable resources exposed for this title yet.",
            color = TextMuted,
            fontSize = 13.sp
          )
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          downloadsInfo.downloads.forEach { option ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .liquidGlassSurface(
                  shape = RoundedCornerShape(12.dp),
                  elevation = 2.dp,
                  backgroundColor = Color(0x99151A25),
                  borderColor = GlassTokens.BorderHighlight
                )
                .clickable {
                  if (option.downloadUrl.isNotBlank()) {
                    try {
                      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(option.downloadUrl))
                      context.startActivity(intent)
                    } catch (_: Exception) {}
                  }
                }
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x2600D9F5))
                    .border(0.8.dp, Color(0x4D00D9F5), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    tint = Color(0xFF00D9F5),
                    modifier = Modifier.size(18.dp)
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                  Text(
                    text = option.name,
                    color = CinemaWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = "${option.quality} • Size: ${option.size}",
                    color = TextMuted,
                    fontSize = 11.sp
                  )
                }
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0x2600D9F5))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "GET",
                  color = Color(0xFF00D9F5),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    }
  }
}
