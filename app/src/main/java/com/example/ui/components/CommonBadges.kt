package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGoldPrimary
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DividerColor
import com.example.ui.theme.WhiteTextPrimary

@Composable
fun RatingBadge(
  rating: Double,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .background(DarkSurface.copy(alpha = 0.88f), RoundedCornerShape(4.dp))
      .border(0.8.dp, DividerColor, RoundedCornerShape(4.dp))
      .padding(horizontal = 6.dp, vertical = 2.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = Icons.Default.Star,
      contentDescription = "Rating",
      tint = AmberGoldPrimary,
      modifier = Modifier.size(12.dp)
    )
    Text(
      text = "%.1f".format(rating),
      color = WhiteTextPrimary,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(start = 3.dp)
    )
  }
}

@Composable
fun FormatBadge(
  text: String,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .background(DarkSurfaceVariant, RoundedCornerShape(4.dp))
      .border(0.8.dp, DividerColor, RoundedCornerShape(4.dp))
      .padding(horizontal = 6.dp, vertical = 2.dp)
  ) {
    Text(
      text = text,
      color = WhiteTextPrimary,
      fontSize = 10.sp,
      fontWeight = FontWeight.SemiBold,
      letterSpacing = 0.5.sp
    )
  }
}

@Composable
fun MaturityBadge(
  text: String,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .background(Color.Transparent, RoundedCornerShape(3.dp))
      .border(1.dp, DividerColor, RoundedCornerShape(3.dp))
      .padding(horizontal = 5.dp, vertical = 1.dp)
  ) {
    Text(
      text = text,
      color = WhiteTextPrimary.copy(alpha = 0.85f),
      fontSize = 10.sp,
      fontWeight = FontWeight.Medium
    )
  }
}
