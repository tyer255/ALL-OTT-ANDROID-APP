package com.example.ui.screens

import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.WhiteTextPrimary
import com.example.ui.theme.liquidGlassSurface
import com.example.ui.theme.neumorphicDepth

@Composable
fun PlayerScreen(
  mediaTitle: String,
  subtitle: String? = null,
  streamUrl: String?,
  serverName: String? = null,
  onBackClick: () -> Unit,
  onRetryOrSwitchServer: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  BackHandler { onBackClick() }

  var controlsVisible by remember { mutableStateOf(true) }
  var isPageLoading by remember { mutableStateOf(true) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black)
      .testTag("player_screen")
  ) {
    if (streamUrl.isNullOrBlank()) {
      // Liquid Glass Playback Unavailable Card
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth(0.9f)
            .liquidGlassSurface(
              shape = RoundedCornerShape(20.dp),
              elevation = 6.dp,
              backgroundColor = Color(0xD910141E),
              borderColor = Color(0x33EF4444)
            )
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.ErrorOutline,
              contentDescription = null,
              tint = Color(0xFFF87171),
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = "Playback Resource Unavailable",
              color = CinemaWhite,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "The upstream API server did not expose an active stream URL or embedded player for this title. Please try another streaming mirror.",
              color = MutedSlate,
              fontSize = 12.sp,
              textAlign = TextAlign.Center,
              lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              if (onRetryOrSwitchServer != null) {
                Button(
                  onClick = onRetryOrSwitchServer,
                  colors = ButtonDefaults.buttonColors(containerColor = CinemaWhite),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Select Server", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
              }
              Button(
                onClick = onBackClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x26FFFFFF)),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("Back", color = CinemaWhite, fontSize = 13.sp)
              }
            }
          }
        }
      }
    } else {
      // Embedded Player WebView
      AndroidView(
        factory = { ctx ->
          WebView(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
              override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                isPageLoading = false
              }

              override fun onRenderProcessGone(view: WebView?, detail: android.webkit.RenderProcessGoneDetail?): Boolean {
                isPageLoading = false
                return true
              }

              override fun onReceivedError(
                view: WebView?,
                request: android.webkit.WebResourceRequest?,
                error: android.webkit.WebResourceError?
              ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                  isPageLoading = false
                }
              }
            }
            loadUrl(streamUrl)
          }
        },
        update = { webView ->
          if (webView.url != streamUrl && !streamUrl.isNullOrBlank()) {
            webView.loadUrl(streamUrl)
          }
        },
        modifier = Modifier.fillMaxSize()
      )

      if (isPageLoading) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator(
            color = CinemaWhite,
            modifier = Modifier.size(36.dp),
            strokeWidth = 3.dp
          )
        }
      }

      // Clickable overlay toggling controls
      Box(
        modifier = Modifier
          .fillMaxSize()
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
          ) {
            controlsVisible = !controlsVisible
          }
      )

      // Top Overlay Controls in Liquid Glass Pill
      AnimatedVisibility(
        visible = controlsVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.TopCenter)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .liquidGlassSurface(
                shape = RoundedCornerShape(16.dp),
                elevation = 4.dp,
                backgroundColor = Color(0xCC0D1017),
                borderColor = GlassTokens.BorderHighlight
              )
              .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              IconButton(
                onClick = onBackClick,
                modifier = Modifier
                  .size(34.dp)
                  .neumorphicDepth(shape = CircleShape, elevation = 1.dp, baseColor = Color(0xFF161B24))
                  .testTag("player_back_button")
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Exit Player",
                  tint = WhiteTextPrimary,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = mediaTitle,
                  color = WhiteTextPrimary,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                  Text(
                    text = subtitle,
                    color = MutedSlate,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }

            if (!serverName.isNullOrBlank()) {
              Spacer(modifier = Modifier.width(8.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0x3300D9F5))
                  .border(0.5.dp, Color(0x6600D9F5), RoundedCornerShape(6.dp))
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Text(
                  text = serverName,
                  color = Color(0xFF00D9F5),
                  fontSize = 10.sp,
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
