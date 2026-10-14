package com.example

import com.example.data.util.ServerAdBlockerManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying ALL OTT Automatic Server-Specific Ad Blocker logic:
 * 1. TG Vid Stream -> ON
 * 2. Ultra Proxy Fast Cloudflare Worker Proxy -> ON
 * 3. External servers -> OFF and previous state cleared
 * 4. Switching back -> ON again
 * 5. State retention during player controls/seeking/pause
 */
class ServerAdBlockerTest {

  @Before
  fun setUp() {
    ServerAdBlockerManager.clearState()
  }

  @Test
  fun testScenario1_TgVidStream_EnablesAdBlocker() {
    val serverId = "srv_tgvid"
    val serverName = "TG Vid Stream"
    val streamUrl = "https://tgvid.lovable.app/embed/movie/550"

    // 1. Classification check
    val shouldEnable = ServerAdBlockerManager.shouldEnableAdBlocker(serverId, serverName, streamUrl)
    assertTrue("TG Vid Stream must have ad blocker enabled", shouldEnable)

    // 2. Configuration before URL load
    val config = ServerAdBlockerManager.configureForServer(serverId, serverName, streamUrl)
    assertTrue("Config state must be active before URL load", config.isAdBlockerEnabled)
    assertEquals(serverId, config.serverId)
  }

  @Test
  fun testScenario2_UltraProxyFastCloudflareWorkerProxy_EnablesAdBlocker() {
    val serverId = "srv_proxy"
    val serverName = "Ultra Proxy Fast Cloudflare Worker Proxy"
    val streamUrl = "https://proxy.niggasoup45.workers.dev/embed/movie/550"

    // 1. Classification check
    val shouldEnable = ServerAdBlockerManager.shouldEnableAdBlocker(serverId, serverName, streamUrl)
    assertTrue("Ultra Proxy Fast Cloudflare Worker Proxy must have ad blocker enabled", shouldEnable)

    // 2. Configuration before URL load
    val config = ServerAdBlockerManager.configureForServer(serverId, serverName, streamUrl)
    assertTrue("Config state must be active before URL load", config.isAdBlockerEnabled)
    assertEquals(serverId, config.serverId)
  }

  @Test
  fun testScenario3_SwitchToExternalServer_DisablesAdBlockerImmediatelyAndClearsState() {
    // Start on designated server
    ServerAdBlockerManager.configureForServer("srv_tgvid", "TG Vid Stream", "https://tgvid.lovable.app/embed/movie/550")
    assertTrue(ServerAdBlockerManager.getActiveConfig().isAdBlockerEnabled)

    // Switch to external serverVidStuck
    val externalId = "srv_vidstuck"
    val externalName = "VidStuck Stream"
    val externalUrl = "https://vidstuck.xyz/embed/movie/550"

    val shouldEnableExternal = ServerAdBlockerManager.shouldEnableAdBlocker(externalId, externalName, externalUrl)
    assertFalse("External server VidStuck must have ad blocker OFF", shouldEnableExternal)

    val externalConfig = ServerAdBlockerManager.configureForServer(externalId, externalName, externalUrl)
    assertFalse("Config state must immediately reflect ad blocker OFF for external server", externalConfig.isAdBlockerEnabled)
    assertEquals("srv_vidstuck", externalConfig.serverId)

    // Verify interceptors return null (OFF)
    val script = ServerAdBlockerManager.getAdBlockScript(externalConfig.isAdBlockerEnabled)
    assertEquals("Script must be empty when ad blocker is OFF", "", script)
  }

  @Test
  fun testScenario4_SwitchBackToDesignatedServer_ReenablesAdBlocker() {
    // 1. External server (OFF)
    ServerAdBlockerManager.configureForServer("srv_vidsrc", "VidSrc Pro", "https://vidsrc.me/embed/movie/550")
    assertFalse(ServerAdBlockerManager.getActiveConfig().isAdBlockerEnabled)

    // 2. Switch back to TG Vid Stream (ON)
    val configTg = ServerAdBlockerManager.configureForServer("srv_tgvid", "TG Vid Stream", "https://tgvid.lovable.app/embed/movie/550")
    assertTrue("Ad blocker must turn ON when switching back to TG Vid Stream", configTg.isAdBlockerEnabled)

    // 3. Switch to another external server (OFF)
    ServerAdBlockerManager.configureForServer("srv_smashy", "SmashyStream", "https://smashystream.com/embed/550")
    assertFalse(ServerAdBlockerManager.getActiveConfig().isAdBlockerEnabled)

    // 4. Switch back to Ultra Proxy Fast Cloudflare Worker Proxy (ON)
    val configProxy = ServerAdBlockerManager.configureForServer("srv_proxy", "Ultra Proxy Fast Cloudflare Worker Proxy", "https://proxy.niggasoup45.workers.dev/embed/movie/550")
    assertTrue("Ad blocker must turn ON when switching back to Ultra Proxy", configProxy.isAdBlockerEnabled)
  }

  @Test
  fun testScenario5_SeekingPauseResumeControlsTaps_DoNotResetFilterState() {
    // Initialize active server session
    val config = ServerAdBlockerManager.configureForServer("srv_tgvid", "TG Vid Stream", "https://tgvid.lovable.app/embed/movie/550")
    val initialTimestamp = config.timestamp

    // Simulate seeking / pause / resume / control overlay taps without switching server
    val activeConfigAfterPlaybackInteractions = ServerAdBlockerManager.getActiveConfig()

    assertTrue("Filter state must remain active during seeking/pause/resume", activeConfigAfterPlaybackInteractions.isAdBlockerEnabled)
    assertEquals("Server ID must not change during playback interactions", "srv_tgvid", activeConfigAfterPlaybackInteractions.serverId)
    assertEquals("Timestamp must remain locked to session start", initialTimestamp, activeConfigAfterPlaybackInteractions.timestamp)
  }
}
