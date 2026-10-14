package com.example.data.util

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import java.io.ByteArrayInputStream
import java.util.Locale

/**
 * ALL OTT — Centralized Automatic Server-Specific Ad Blocker
 *
 * Rules:
 * 1. Friend-Owned Servers -> Ad Blocker ON
 *    - "TG Vid Stream" (srv_tgvid / tgvid.lovable.app)
 *    - "Ultra Proxy Fast Cloudflare Worker Proxy" (srv_proxy / srv_2 / workers.dev)
 * 2. All Other Servers -> Ad Blocker OFF
 *    - Automatically treated as external servers (no hardcoded list of external servers).
 *    - Disables all ad filtering, request interception, and redirect blocking.
 *    - Restores standard network and navigation behavior.
 * 3. Lifecycle & State:
 *    - Centralized classification: shouldEnableAdBlocker(serverId, serverName, streamUrl)
 *    - Configured during server initialization BEFORE loading the URL.
 *    - Clean state reset during server switches without race conditions or memory leaks.
 *    - State preserved during seeking, pause/resume, and UI control interactions.
 */
object ServerAdBlockerManager {

  data class AdBlockConfig(
    val isAdBlockerEnabled: Boolean,
    val serverId: String?,
    val serverName: String?,
    val streamUrl: String?,
    val timestamp: Long = System.currentTimeMillis()
  )

  @Volatile
  private var activeConfig: AdBlockConfig = AdBlockConfig(
    isAdBlockerEnabled = false,
    serverId = null,
    serverName = null,
    streamUrl = null
  )

  /**
   * Centralized Server Classification Helper
   * Checks if the server is one of the two designated friend-owned servers:
   * 1. "TG Vid Stream"
   * 2. "Ultra Proxy Fast Cloudflare Worker Proxy"
   */
  fun shouldEnableAdBlocker(
    serverId: String?,
    serverName: String?,
    streamUrl: String? = null
  ): Boolean {
    val id = serverId.orEmpty().lowercase(Locale.ROOT).trim()
    val name = serverName.orEmpty().lowercase(Locale.ROOT).trim()
    val url = streamUrl.orEmpty().lowercase(Locale.ROOT).trim()

    // 1. TG Vid Stream matching
    val isTgVidServer = id == "srv_tgvid" ||
        id.contains("tgvid") ||
        name.contains("tg vid") ||
        name.contains("tgvid") ||
        url.contains("tgvid.lovable.app")

    // 2. Ultra Proxy Fast Cloudflare Worker Proxy matching
    val isUltraProxyServer = id == "srv_proxy" ||
        (id == "srv_2" && (name.contains("proxy") || name.contains("ultra") || url.contains("workers.dev"))) ||
        name.contains("ultra proxy") ||
        name.contains("cloudflare worker") ||
        (name.contains("ultra") && name.contains("proxy")) ||
        url.contains("proxy.niggasoup45.workers.dev") ||
        url.contains("workers.dev")

    return isTgVidServer || isUltraProxyServer
  }

  /**
   * Configures active server state during server initialization, BEFORE loading URL.
   * Immediately updates or clears filtering state.
   */
  fun configureForServer(
    serverId: String?,
    serverName: String?,
    streamUrl: String?
  ): AdBlockConfig {
    val enabled = shouldEnableAdBlocker(serverId, serverName, streamUrl)
    val newConfig = AdBlockConfig(
      isAdBlockerEnabled = enabled,
      serverId = serverId,
      serverName = serverName,
      streamUrl = streamUrl
    )
    activeConfig = newConfig
    return newConfig
  }

  /**
   * Gets current active config
   */
  fun getActiveConfig(): AdBlockConfig = activeConfig

  /**
   * Clears state when switching away or stopping playback
   */
  fun clearState() {
    activeConfig = AdBlockConfig(
      isAdBlockerEnabled = false,
      serverId = null,
      serverName = null,
      streamUrl = null
    )
  }

  // --- Ad & Tracker Network Request Interception ---

  private val BLOCKED_DOMAINS = listOf(
    "popads", "popcash", "adsterra", "doubleclick", "googlesyndication",
    "juicyads", "exoclick", "adbrt", "bet365", "adservice", "adserver",
    "clck", "onclick", "popunder", "propellerads", "monetag", "hooligan",
    "admob", "yandex", "inmobi", "taboola", "outbrain", "adroll", "hilltopads"
  )

  private val BLOCKED_URL_KEYWORDS = listOf(
    "/ad.js", "/ads.js", "pop.js", "popup.js", "redirect.js",
    "ad_frame", "ad_banner", "ad_slot", "ad_partner", "banner_ad"
  )

  /**
   * Intercepts network requests in WebView when ad blocker is ON.
   * Returns empty WebResourceResponse if blocked, null if allowed or blocker is OFF.
   */
  fun shouldInterceptRequest(
    view: WebView?,
    request: WebResourceRequest?,
    isAdBlockerActive: Boolean = activeConfig.isAdBlockerEnabled
  ): WebResourceResponse? {
    if (!isAdBlockerActive || request == null) {
      return null
    }

    val requestUrl = request.url?.toString().orEmpty().lowercase(Locale.ROOT)

    // Check blocked ad/tracker domains & keywords
    val isBlocked = BLOCKED_DOMAINS.any { domain -> requestUrl.contains(domain) } ||
        BLOCKED_URL_KEYWORDS.any { keyword -> requestUrl.contains(keyword) }

    return if (isBlocked) {
      WebResourceResponse(
        "text/plain",
        "UTF-8",
        200,
        "OK",
        emptyMap(),
        ByteArrayInputStream(ByteArray(0))
      )
    } else {
      null
    }
  }

  /**
   * Evaluates redirect & navigation URL overrides in WebViewClient.
   * Returns true (BLOCK) if ad blocker is ON and request is an ad redirect/popup.
   * Returns false (ALLOW) if ad blocker is OFF or request is legitimate.
   */
  fun shouldOverrideUrlLoading(
    view: WebView?,
    request: WebResourceRequest?,
    isAdBlockerActive: Boolean = activeConfig.isAdBlockerEnabled
  ): Boolean {
    if (!isAdBlockerActive || request == null) {
      return false // Ad blocker OFF: allow standard navigation/playback
    }

    val url = request.url?.toString().orEmpty().lowercase(Locale.ROOT)
    val scheme = request.url?.scheme.orEmpty().lowercase(Locale.ROOT)

    // Block non-HTTP/HTTPS schemes (intent://, market://, tg://, etc. used by popups)
    if (scheme.isNotEmpty() && scheme != "http" && scheme != "https" && scheme != "about" && scheme != "javascript") {
      return true
    }

    // Block ad redirect domains
    if (BLOCKED_DOMAINS.any { domain -> url.contains(domain) }) {
      return true
    }

    // Allow legitimate main-frame navigation to the server origin / video stream
    return false
  }

  /**
   * Anti-ad, anti-popup JavaScript injection for designated servers.
   * Returns JS string when enabled, or empty string when disabled.
   */
  fun getAdBlockScript(isAdBlockerActive: Boolean = activeConfig.isAdBlockerEnabled): String {
    if (!isAdBlockerActive) {
      return ""
    }

    return """
      (function() {
        try {
          // 1. Prevent window.open popups & redirects
          window.open = function() {
            console.log('ALL OTT AdBlocker: Prevented window.open popup');
            return null;
          };
          window.alert = function() {};
          window.confirm = function() { return true; };

          // 2. Prevent transparent clickjacking ad overlays
          var removeOverlays = function() {
            var allElements = document.querySelectorAll('div, a, iframe, span');
            for (var i = 0; i < allElements.length; i++) {
              var el = allElements[i];
              var zIndex = window.getComputedStyle(el).zIndex;
              var pos = window.getComputedStyle(el).position;
              if ((pos === 'fixed' || pos === 'absolute') && parseInt(zIndex) > 99990 && el.id !== 'aether-fit-style') {
                if (el.tagName.toLowerCase() === 'a' || el.onclick || el.getAttribute('href')) {
                  var href = (el.getAttribute('href') || '').toLowerCase();
                  if (!href.includes('tgvid') && !href.includes('workers.dev') && !href.includes('player')) {
                    el.remove();
                  }
                }
              }
            }
          };

          if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', removeOverlays);
          } else {
            removeOverlays();
          }
          setInterval(removeOverlays, 2000);
        } catch (e) {
          console.error('AdBlocker JS error', e);
        }
      })();
    """.trimIndent()
  }
}
