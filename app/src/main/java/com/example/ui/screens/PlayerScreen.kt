package com.example.ui.screens

import android.app.Activity
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FitScreen
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

enum class VideoFitMode(val label: String) {
  FIT("Fit (16:9)"),
  FILL("Fill"),
  STRETCH("Stretch")
}

private data class FitConfig(
  val landscapeWidth: String,
  val landscapeHeight: String,
  val objectFit: String,
  val videoWidth: String,
  val videoHeight: String,
  val aspectRatio: String
)

private fun getFitInjectionScript(fitMode: VideoFitMode): String {
  val cfg = when (fitMode) {
    VideoFitMode.FIT -> FitConfig(
      landscapeWidth = "calc(100vh * (16 / 9))",
      landscapeHeight = "100vh",
      objectFit = "contain",
      videoWidth = "100%",
      videoHeight = "100%",
      aspectRatio = "16 / 9"
    )
    VideoFitMode.FILL -> FitConfig(
      landscapeWidth = "100vw",
      landscapeHeight = "100vh",
      objectFit = "cover",
      videoWidth = "100vw",
      videoHeight = "100vh",
      aspectRatio = "auto"
    )
    VideoFitMode.STRETCH -> FitConfig(
      landscapeWidth = "100vw",
      landscapeHeight = "100vh",
      objectFit = "fill",
      videoWidth = "100vw",
      videoHeight = "100vh",
      aspectRatio = "auto"
    )
  }

  return """
    (function() {
      try {
        var style = document.getElementById('aether-fit-style');
        if (!style) {
          style = document.createElement('style');
          style.id = 'aether-fit-style';
          document.head.appendChild(style);
        }
        style.textContent = `
          html, body {
            width: 100vw !important;
            height: 100vh !important;
            max-width: 100vw !important;
            max-height: 100vh !important;
            margin: 0 !important;
            padding: 0 !important;
            overflow: hidden !important;
            background-color: #000000 !important;
          }

          /* Live TV flex wrappers: prevent overflowing container height */
          div[class*="overflow-hidden bg-player-surface"],
          .flex.h-full.w-full.items-center,
          div.flex.h-full.w-full.items-center {
            width: 100vw !important;
            height: 100vh !important;
            max-width: 100vw !important;
            max-height: 100vh !important;
            margin: 0 !important;
            padding: 0 !important;
            overflow: hidden !important;
            display: flex !important;
            align-items: center !important;
            justify-content: center !important;
            background-color: #000000 !important;
          }

          /* Fit .tg-player container perfectly within screen boundaries */
          @media (orientation: landscape) {
            .tg-player,
            div.tg-player,
            [class*="tg-player"] {
              height: ${cfg.landscapeHeight} !important;
              max-height: 100vh !important;
              width: ${cfg.landscapeWidth} !important;
              max-width: 100vw !important;
              aspect-ratio: ${cfg.aspectRatio} !important;
              margin: 0 auto !important;
              position: relative !important;
              display: flex !important;
              flex-direction: column !important;
              justify-content: center !important;
              align-items: center !important;
              overflow: hidden !important;
            }
          }

          @media (orientation: portrait) {
            .tg-player,
            div.tg-player,
            [class*="tg-player"] {
              width: 100vw !important;
              max-width: 100vw !important;
              height: ${if (fitMode == VideoFitMode.FIT) "calc(100vw * (9 / 16))" else "100vh"} !important;
              max-height: 100vh !important;
              aspect-ratio: ${cfg.aspectRatio} !important;
              margin: auto 0 !important;
              position: relative !important;
              display: flex !important;
              flex-direction: column !important;
              justify-content: center !important;
              align-items: center !important;
              overflow: hidden !important;
            }
          }

          /* Ensure video element does not cut off subtitles, news tickers, or logos */
          video,
          .tg-player video,
          iframe {
            max-width: 100vw !important;
            max-height: 100vh !important;
            width: ${cfg.videoWidth} !important;
            height: ${cfg.videoHeight} !important;
            object-fit: ${cfg.objectFit} !important;
          }

          /* Dock in-player controls comfortably at visible bottom */
          .player-controls,
          div.player-controls,
          [class*="player-controls"] {
            bottom: 8px !important;
            inset-inline-start: 12px !important;
            inset-inline-end: 12px !important;
            max-width: calc(100vw - 24px) !important;
            z-index: 99999 !important;
          }

          /* Hide unwanted third-party badges covering in-player controls */
          #lovable-badge {
            display: none !important;
          }
        `;

        // Ensure responsive viewport meta
        var meta = document.querySelector('meta[name="viewport"]');
        if (!meta) {
          meta = document.createElement('meta');
          meta.name = 'viewport';
          document.head.appendChild(meta);
        }
        meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover';

        // Apply object-fit to all video elements
        var vids = document.querySelectorAll('video');
        for (var i = 0; i < vids.length; i++) {
          vids[i].style.objectFit = '${cfg.objectFit}';
        }

        // Fullscreen API polyfill for in-player maximize button
        if (!Element.prototype.requestFullscreen && Element.prototype.webkitRequestFullscreen) {
          Element.prototype.requestFullscreen = Element.prototype.webkitRequestFullscreen;
        }
        if (!document.exitFullscreen && document.webkitExitFullscreen) {
          document.exitFullscreen = document.webkitExitFullscreen;
        }
      } catch (e) {
        console.error('Player fit injection error', e);
      }
    })();
  """.trimIndent()
}

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
  var fitMode by remember { mutableStateOf(VideoFitMode.FIT) }
  var webViewRef by remember { mutableStateOf<WebView?>(null) }
  var customView by remember { mutableStateOf<View?>(null) }
  var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }
  var isPageLoading by remember { mutableStateOf(true) }
  var isControlsVisible by remember { mutableStateOf(true) }
  var touchResetTrigger by remember { mutableStateOf(0L) }

  // Immediately apply aspect-ratio fit mode changes dynamically to active WebView
  LaunchedEffect(fitMode) {
    webViewRef?.evaluateJavascript(getFitInjectionScript(fitMode), null)
  }

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
    val activity = (context as? Activity) ?: (context as? ContextWrapper)?.baseContext as? Activity
    val window = activity?.window
    if (window != null) {
      val insetsController = WindowCompat.getInsetsController(window, window.decorView)
      insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
  }

  // 1. Manage Immersive Sticky Fullscreen (Hide Both Status and Navigation Bars), Display Cutout & Keep Screen On
  DisposableEffect(Unit) {
    val activity = (context as? Activity) ?: (context as? ContextWrapper)?.baseContext as? Activity
    val window = activity?.window
    val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    val originalCutoutMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      window?.attributes?.layoutInDisplayCutoutMode ?: WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
    } else {
      0
    }

    if (window != null) {
      // Extend into cutout / display notch in landscape mode to eliminate white or black system gaps
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val params = window.attributes
        params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        window.attributes = params
      }

      // Hide BOTH status bars and navigation bars using WindowInsetsControllerCompat with immersive sticky behavior
      WindowCompat.setDecorFitsSystemWindows(window, false)
      val insetsController = WindowCompat.getInsetsController(window, window.decorView)
      insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
      insetsController.hide(WindowInsetsCompat.Type.systemBars())

      // Keep screen on during active playback
      window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    onDispose {
      activity?.requestedOrientation = originalOrientation
      if (window != null) {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        insetsController.show(WindowInsetsCompat.Type.systemBars())
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          val params = window.attributes
          params.layoutInDisplayCutoutMode = originalCutoutMode
          window.attributes = params
        }
      }
    }
  }

  // 2. Manage Orientation Switching (Landscape <-> Portrait) without disturbing immersive sticky mode
  DisposableEffect(isLandscape) {
    val activity = (context as? Activity) ?: (context as? ContextWrapper)?.baseContext as? Activity
    if (isLandscape) {
      activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    } else {
      activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    onDispose { }
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

            webViewRef = this

            val cookieManager = android.webkit.CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(this, true)

            settings.apply {
              javaScriptEnabled = true
              domStorageEnabled = true
              mediaPlaybackRequiresUserGesture = false
              loadWithOverviewMode = true
              useWideViewPort = true
              textZoom = 100
              javaScriptCanOpenWindowsAutomatically = true
              mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
              allowFileAccess = true
              allowContentAccess = true
              setSupportZoom(false)
              displayZoomControls = false
              builtInZoomControls = false
              databaseEnabled = true
            }

            webChromeClient = object : WebChromeClient() {
              override fun onPermissionRequest(request: android.webkit.PermissionRequest?) {
                request?.grant(request.resources)
              }

              override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress >= 70) {
                  isPageLoading = false
                }
              }

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
              override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                view?.evaluateJavascript(getFitInjectionScript(fitMode), null)
              }

              override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                isPageLoading = false
                view?.evaluateJavascript(getFitInjectionScript(fitMode), null)
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
          webViewRef = webView
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
            val v = customView!!
            (v.parent as? ViewGroup)?.removeView(v)
            v.layoutParams = ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT
            )
            v
          },
          update = { v ->
            v.layoutParams = ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT
            )
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

      // 5. Floating Top Bar with Back, Title, Server Badge & Screen Rotation Toggle (Attached to Top Edge)
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
            .padding(start = 12.dp, top = 4.dp, end = 12.dp, bottom = 0.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .liquidGlassSurface(
                shape = RoundedCornerShape(14.dp),
                elevation = 4.dp,
                backgroundColor = Color(0xCC0D1017),
                borderColor = GlassTokens.BorderHighlight
              )
              .padding(horizontal = 12.dp, vertical = 6.dp),
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

              // Video Fit Mode Toggle Button (Fit 16:9 -> Fill -> Stretch)
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0x26FFFFFF))
                  .border(0.5.dp, Color(0x4DFFFFFF), RoundedCornerShape(8.dp))
                  .clickable {
                    showControlsAndResetTimer()
                    fitMode = when (fitMode) {
                      VideoFitMode.FIT -> VideoFitMode.FILL
                      VideoFitMode.FILL -> VideoFitMode.STRETCH
                      VideoFitMode.STRETCH -> VideoFitMode.FIT
                    }
                  }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
                  .testTag("player_fit_mode_button")
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = when (fitMode) {
                      VideoFitMode.FIT -> Icons.Default.FitScreen
                      VideoFitMode.FILL -> Icons.Default.CropFree
                      VideoFitMode.STRETCH -> Icons.Default.AspectRatio
                    },
                    contentDescription = "Aspect Ratio Fit Mode",
                    tint = CinemaWhite,
                    modifier = Modifier.size(14.dp)
                  )
                  Text(
                    text = fitMode.label,
                    color = CinemaWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
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

              // Server Badge Text (Live TV vs Movies & Shows)
              val isLive = serverName.orEmpty().contains("Live", ignoreCase = true) ||
                  subtitle.orEmpty().contains("Live", ignoreCase = true)

              val cleanServerName = when {
                isLive -> "Live TV"
                serverName.orEmpty().contains("TGVid", ignoreCase = true) -> "Server 1 (TGVid Stream)"
                serverName.orEmpty().contains("VidStuck", ignoreCase = true) -> "Server 2 (VidStuck Stream)"
                serverName.isNullOrBlank() -> "Server 1 (TGVid Stream)"
                else -> serverName
              }

              val badgeBgColor = if (isLive) Color(0x33E50914) else Color(0x3300D9F5)
              val badgeBorderColor = if (isLive) Color(0x80E50914) else Color(0x6600D9F5)
              val badgeTextColor = if (isLive) Color(0xFFFF5252) else Color(0xFF00D9F5)

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(badgeBgColor)
                  .border(0.5.dp, badgeBorderColor, RoundedCornerShape(6.dp))
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  if (isLive) {
                    Box(
                      modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE50914))
                    )
                  }
                  Text(
                    text = cleanServerName,
                    color = badgeTextColor,
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
}
