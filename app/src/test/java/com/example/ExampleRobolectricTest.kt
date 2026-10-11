package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("All OTT", appName)
  }

  @Test
  fun `create AetherViewModel via AndroidViewModelFactory`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(app).create(com.example.ui.viewmodel.AetherViewModel::class.java)
    org.junit.Assert.assertNotNull(vm)
  }

  @Test
  fun `verify servers deduplication and sequential numbering`() = kotlinx.coroutines.test.runTest {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val repo = com.example.data.repository.RealMediaRepository(context = app)
    val servers: List<com.example.data.model.ServerItem> = repo.getServers(contentId = "550", type = "movie")
    org.junit.Assert.assertTrue("Servers list should not be empty", servers.isNotEmpty())

    // Server 1 must be srv_tgvid with name Server 1 (TGVid Stream)
    val server1 = servers.first()
    assertEquals("srv_tgvid", server1.id)
    assertEquals("Server 1 (TGVid Stream)", server1.name)

    // Check that there are at least 7 servers total
    org.junit.Assert.assertTrue("Should have 7 or more streaming servers", servers.size >= 7)

    // Check that there are no duplicate server IDs
    val ids = servers.map { it.id }
    assertEquals("Server IDs must be unique", ids.distinct().size, ids.size)

    // Verify sequential numbering Server 1, Server 2, Server 3...
    servers.forEachIndexed { index, server ->
      val expectedPrefix = "Server ${index + 1}"
      org.junit.Assert.assertTrue(
        "Server name '${server.name}' should start with $expectedPrefix",
        server.name.startsWith(expectedPrefix)
      )
    }
  }
}
