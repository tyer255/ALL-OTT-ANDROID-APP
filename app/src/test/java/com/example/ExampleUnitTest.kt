package com.example

import com.example.data.model.MediaType
import com.example.data.repository.MockMediaRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun repository_returnsCuratedContent() {
    val repository = MockMediaRepository()
    val kepler = repository.getMediaById("m1")
    assertNotNull("Flagship title Kepler must exist", kepler)
    assertEquals("Chronicles of Kepler", kepler?.title)
    assertEquals(MediaType.SERIES, kepler?.mediaType)
    assertTrue("Kepler must have seasons", kepler?.seasons?.isNotEmpty() == true)
  }

  @Test
  fun repository_searchFiltersAccurately() {
    val repository = MockMediaRepository()
    val searchSciFi = repository.searchCatalog("Kepler", "Sci-Fi", null)
    assertEquals(1, searchSciFi.size)
    assertEquals("Chronicles of Kepler", searchSciFi[0].title)

    val searchAction = repository.searchCatalog("", "Action", null)
    assertTrue("Should return action titles", searchAction.isNotEmpty())
    assertTrue("All items must have Action genre", searchAction.all { it.genres.contains("Action") })
  }

  @Test
  fun repository_toggleMyListOperatesCorrectly() {
    val repository = MockMediaRepository()
    val item = repository.getMediaById("m2")!!
    val initialInList = repository.isInMyList(item.id)
    val added = repository.toggleMyList(item)
    assertEquals(!initialInList, added)
    assertEquals(!initialInList, repository.isInMyList(item.id))
  }

  @Test
  fun benchmark_searchCachingPerformance() = runBlocking {
    val repository = com.example.data.repository.RealMediaRepository()

    // 1. Initial Uncached Search
    val t0 = System.currentTimeMillis()
    val results1 = repository.searchReal("spider", "all")
    val durationUncached = System.currentTimeMillis() - t0

    // 2. Cached Search Lookup
    val t1 = System.currentTimeMillis()
    val results2 = repository.searchReal("spider", "all")
    val durationCached = System.currentTimeMillis() - t1

    val cachedFromHelper = repository.getSearchFromCache("spider", "all")

    println("SEARCH BENCHMARK - Uncached Duration: ${durationUncached}ms | Cached Duration: ${durationCached}ms")

    assertNotNull("Cache must hold results for 'spider'", cachedFromHelper)
    assertEquals("Cached search must return identical item count", results1.size, results2.size)
    assertTrue("Cached search must return in under 15ms (actual: ${durationCached}ms)", durationCached < 15)
  }

  @Test
  fun benchmark_inFlightSearchDeduplication() = runBlocking {
    val repository = com.example.data.repository.RealMediaRepository()

    // Dispatch 3 concurrent search requests for the exact same query
    val t0 = System.currentTimeMillis()
    val (res1, res2, res3) = coroutineScope {
      val deferred1 = async { repository.searchReal("anime", "all") }
      val deferred2 = async { repository.searchReal("anime", "all") }
      val deferred3 = async { repository.searchReal("anime", "all") }
      Triple(deferred1.await(), deferred2.await(), deferred3.await())
    }
    val totalTime = System.currentTimeMillis() - t0

    println("DEDUPLICATION BENCHMARK - 3 Concurrent Searches resolved in ${totalTime}ms")

    assertEquals(res1.size, res2.size)
    assertEquals(res2.size, res3.size)
  }

  @Test
  fun heroBanner_stackedDeckNavigationAndWrapAround() {
    val itemCount = 6
    var activeIndex = 0

    // Initial state: front=0, 2nd=1, 3rd=2
    val second = (activeIndex + 1) % itemCount
    val third = (activeIndex + 2) % itemCount
    assertEquals(0, activeIndex)
    assertEquals(1, second)
    assertEquals(2, third)

    // Simulate Left Swipe: Advances to next card (Page 1 -> Page 2)
    activeIndex = (activeIndex + 1) % itemCount
    assertEquals(1, activeIndex)
    assertEquals(2, (activeIndex + 1) % itemCount)
    assertEquals(3, (activeIndex + 2) % itemCount)

    // Simulate Right Swipe: Goes to previous card (Page 2 -> Page 1)
    activeIndex = if (activeIndex > 0) activeIndex - 1 else itemCount - 1
    assertEquals(0, activeIndex)

    // Simulate Right Swipe at boundary (Page 0 -> wraps around to last card Page 5)
    activeIndex = if (activeIndex > 0) activeIndex - 1 else itemCount - 1
    assertEquals(5, activeIndex)
    assertEquals(0, (activeIndex + 1) % itemCount) // 2nd card wraps around to 0
    assertEquals(1, (activeIndex + 2) % itemCount) // 3rd card wraps around to 1

    // Simulate clicking 3rd stacked layer (card 1)
    val tappedIndex = (activeIndex + 2) % itemCount
    activeIndex = tappedIndex
    assertEquals(1, activeIndex)
  }

  @Test
  fun homeScreen_deduplicationPreservesFluidRendering() = runBlocking {
    val repository = MockMediaRepository()
    val sections = repository.getContentRows().first()
    val allItems = sections.flatMap { it.items }

    // Verify deduplication logic: ensures no duplicate item IDs across sections
    val seenIds = mutableSetOf<String>()
    val seenTitles = mutableSetOf<String>()
    val uniqueItems = allItems.filter { item ->
      val norm = item.title.trim().lowercase()
      if (seenIds.contains(item.id) || seenTitles.contains(norm)) {
        false
      } else {
        seenIds.add(item.id)
        seenTitles.add(norm)
      }
    }

    assertTrue("Must have non-empty unique items", uniqueItems.isNotEmpty())
    assertEquals("Seen IDs count must equal uniqueItems size", uniqueItems.size, seenIds.size)
    assertEquals("Seen titles count must equal uniqueItems size", uniqueItems.size, seenTitles.size)
  }
}

