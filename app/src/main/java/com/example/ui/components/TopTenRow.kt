package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaItem
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.NetflixCardSurface
import com.example.ui.theme.NetflixRed

@Composable
fun TopTenRow(
  title: String,
  items: List<MediaItem>,
  onItemClick: (MediaItem) -> Unit,
  modifier: Modifier = Modifier
) {
  if (items.isEmpty()) return

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 12.dp)
      .testTag("top_ten_row")
  ) {
    Text(
      text = title,
      color = CinemaWhite,
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = (-0.2).sp,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )

    Spacer(modifier = Modifier.height(6.dp))

    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      itemsIndexed(items.take(10), key = { _, item -> "top10_${item.id}" }) { index, item ->
        val rankNumber = (index + 1).toString()

        Box(
          modifier = Modifier
            .width(165.dp)
            .height(200.dp)
            .clickable { onItemClick(item) }
            .testTag("top_ten_card_$rankNumber")
        ) {
          // Giant Stylized Outline/Filled Rank Number (Iconic Netflix typography)
          Text(
            text = rankNumber,
            style = TextStyle(
              fontSize = 110.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.SansSerif,
              letterSpacing = (-12).sp
            ),
            color = Color(0xFF4A4A4A),
            modifier = Modifier
              .align(Alignment.BottomStart)
              .offset(x = (-4).dp, y = 20.dp)
          )

          // Foreground rank number shadow overlay for depth
          Text(
            text = rankNumber,
            style = TextStyle(
              fontSize = 110.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.SansSerif,
              letterSpacing = (-12).sp
            ),
            color = Color(0xFF222222),
            modifier = Modifier
              .align(Alignment.BottomStart)
              .offset(x = (-6).dp, y = 18.dp)
          )

          // Poster shifted right to overlap the number
          Box(
            modifier = Modifier
              .align(Alignment.CenterEnd)
              .width(120.dp)
              .fillMaxHeight()
              .clip(RoundedCornerShape(4.dp))
              .background(NetflixCardSurface)
          ) {
            AsyncImage(
              model = item.posterUrl,
              contentDescription = item.title,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )

            // Top 10 Ribbon Tag
            Box(
              modifier = Modifier
                .align(Alignment.TopEnd)
                .background(
                  NetflixRed,
                  RoundedCornerShape(bottomStart = 4.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "TOP 10",
                color = CinemaWhite,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
              )
            }
          }
        }
      }
    }
  }
}
