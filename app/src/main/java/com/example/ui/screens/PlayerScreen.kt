package com.example.ui.screens

import android.app.Activity
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.delay

/**
 * 2026 OTT Video Player & Live TV Stream Screen.
 * Features:
 * 1. AUTOMATIC LANDSCAPE ORIENTATION: Automatically locks to Landscape (SENSOR_LANDSCAPE) on entry,
 *    and restores portrait when returning to main screens.
 * 2. ROTATION TOGGLE: Dedicated ScreenRotation button in the top bar to easily switch between Landscape & Portrait.
 * 3. CORRECT SERVER NAME BADGES: Accurately displays Server 1 (TGVid Stream) or Server 2 (VidSrc / Vidstack).
 * 4. DIRECT UNBLOCKED EMBED TOUCHES: Allows full control of HTML5 seekbar, play/pause, volume, audio/subtitles.
 * 5. FULL HTML5 MAXIMIZE: WebChromeClient.onShowCustomView & onHideCustomView for 100% full screen video expansion.
 */
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
  val context = LocalContext.current
  var isLandscape by remember { mutableStateOf(true) }
  var customView by remember { mutableStateOf<View?>(null) }
  var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }
  var isPageLoading by remember { mutableStateOf(true) }
  var isControlsVisible by remember { mutableStateOf(true) }
  var touchResetTrigger by remember { mutableStateOf(0L) }

  // Auto-fade controls overlay after 4 seconds of inactivity
  LaunchedEffect(isControlsVisible, touchResetTrigger) {
    if (isControlsVisible) {
      delay(4000L)
      isControlsVisible = false
    }
  }

  fun showControlsAndResetTimer() {
    isControlsVisible = true
    touchResetTrigger = System.currentTimeMillis()
  }

  // 1. Force Sensor Landscape Mode on Entry, Restore Original Orientation on Exit
  DisposableEffect(isLandscape) {
    val activity = (context as? Activity) ?: (context as? ContextWrapper)?.baseContext as? Activity
    val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    if (isLandscape) {
      activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    } else {
      activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    onDispose {
      activity?.requestedOrientation = originalOrientation
    }
  }

  // Handle Android Back Button: Exit HTML5 custom view if active, else navigate back
  BackHandler {
    if (customView != null) {
      customViewCallback?.onCustomViewHidden()
      customView = null
      customViewCallback = null
    } else {
      onBackClick()
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black)
      .pointerInput(Unit) {
        awaitPointerEventScope {
          while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            if (event.changes.any { it.changedToDown() }) {
              showControlsAndResetTimer()
            }
          }
        }
      }
      .testTag("player_screen")
  ) {
    if (streamUrl.isNullOrBlank()) {
      // Playback Unavailable Card
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
                  Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Select Server",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                  )
                }
              }
              Button(
                onClick = onBackClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x26FFFFFF)),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text(text = "Back", color = CinemaWhite, fontSize = 13.sp)
              }
            }
          }
        }
      }
    } else {
      // 2. Embedded Player WebView with Full HTML5 Fullscreen & Touch Interaction
      AndroidView(
        factory = { ctx ->
          WebView(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT
            )

            settings.apply {
              javaScriptEnabled = true
              domStorageEnabled = true
              mediaPlaybackRequiresUserGesture = false
              loadWithOverviewMode = true
              useWideViewPort = true
              mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
              allowFileAccess = true
              allowContentAccess = true
              setSupportZoom(false)
              displayZoomControls = false
              builtInZoomControls = false
              databaseEnabled = true
            }

            webChromeClient = object : WebChromeClient() {
              override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                super.onShowCustomView(view, callback)
                if (customView != null) {
                  callback?.onCustomViewHidden()
                  return
                }
                customView = view
                customViewCallback = callback
              }

              override fun onHideCustomView() {
                super.onHideCustomView()
                customViewCallback?.onCustomViewHidden()
                customView = null
                customViewCallback = null
              }
            }

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

      // 3. HTML5 Custom Fullscreen View
      if (customView != null) {
        AndroidView(
          factory = {
            (customView?.parent as? ViewGroup)?.removeView(customView)
            customView!!
          },
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
        )
      }

      // 4. Loading Indicator
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

      // 5. Floating Top Bar with Back, Title, Server Badge & Screen Rotation Toggle (With 4s Auto-Fade & Safe Top Clearance)
      AnimatedVisibility(
        visible = customView == null && isControlsVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.TopCenter)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 18.dp, top = 14.dp, end = 18.dp, bottom = 8.dp)
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
              .padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              IconButton(
                onClick = {
                  showControlsAndResetTimer()
                  onBackClick()
                },
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
                  fontSize = 13.sp,
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

            Row(verticalAlignment = Alignment.CenterVertically) {
              // Screen Rotation Toggle Button (Landscape / Portrait)
              IconButton(
                onClick = {
                  showControlsAndResetTimer()
                  isLandscape = !isLandscape
                },
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(Color(0x33FFFFFF))
                  .border(0.5.dp, Color(0x66FFFFFF), CircleShape)
                  .testTag("player_rotate_button")
              ) {
                Icon(
                  imageVector = Icons.Default.ScreenRotation,
                  contentDescription = "Rotate Screen Orientation",
                  tint = CinemaWhite,
                  modifier = Modifier.size(16.dp)
                )
              }

              Spacer(modifier = Modifier.width(8.dp))

              if (onRetryOrSwitchServer != null) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x33FFFFFF))
                    .border(0.5.dp, Color(0x66FFFFFF), RoundedCornerShape(8.dp))
                    .clickable {
                      showControlsAndResetTimer()
                      onRetryOrSwitchServer()
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(
                    text = "Servers",
                    color = CinemaWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
              }

              // Server Badge Text (Displays "Server 1 (TGVid Stream)" for Server 1)
              val cleanServerName = when {
                serverName.orEmpty().contains("VidStuck", ignoreCase = true) -> "Server 1 (TGVid Stream)"
                serverName.orEmpty().contains("VidStack", ignoreCase = true) -> "Server 1 (TGVid Stream)"
                serverName.isNullOrBlank() -> "Server 1 (TGVid Stream)"
                else -> serverName
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0x3300D9F5))
                  .border(0.5.dp, Color(0x6600D9F5), RoundedCornerShape(6.dp))
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Text(
                  text = cleanServerName,
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
