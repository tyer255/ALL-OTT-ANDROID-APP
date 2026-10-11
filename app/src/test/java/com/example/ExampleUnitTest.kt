package com.example

import com.example.data.model.MediaType
import com.example.data.repository.MockMediaRepository
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
}
