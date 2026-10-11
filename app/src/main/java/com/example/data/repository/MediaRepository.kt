package com.example.data.repository

import com.example.data.model.ContentRowSection
import com.example.data.model.Episode
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.PlaybackResolved
import com.example.data.model.ServerItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Aether Stream content discovery, real API communication,
 * and playback resolution.
 */
interface MediaRepository {
  fun getFeaturedHero(): Flow<List<MediaItem>>
  fun getContinueWatching(): Flow<List<MediaItem>>
  fun getContentRows(): Flow<List<ContentRowSection>>
  fun getComingSoon(): Flow<List<MediaItem>>
  fun getMyList(): Flow<List<MediaItem>>
  fun getAnimeContent(): Flow<List<MediaItem>> = kotlinx.coroutines.flow.flowOf(emptyList())
  fun getLiveTvChannelsFlow(): Flow<List<com.example.data.model.LiveTvChannel>> = kotlinx.coroutines.flow.flowOf(emptyList())

  suspend fun refreshHome(): Result<Unit>
  suspend fun getMediaDetails(id: String, type: String? = null): MediaItem?
  suspend fun getSeasonEpisodes(seriesId: String, seasonNumber: Int): List<Episode>
  suspend fun searchReal(query: String, type: String? = null): List<MediaItem>
  suspend fun getServers(contentId: String, type: String, season: Int? = null, episode: Int? = null): List<ServerItem>
  suspend fun resolvePlayback(contentId: String, type: String, serverId: String, season: Int? = null, episode: Int? = null): PlaybackResolved?
  suspend fun getLiveTvChannels(): List<com.example.data.model.LiveTvChannel> = emptyList()
  suspend fun getDownloads(id: String, type: String, season: Int? = null, episode: Int? = null): com.example.data.model.DownloadsInfo? = null
  suspend fun getTrailer(query: String): com.example.data.model.TrailerInfo? = null
  suspend fun discoverContent(platform: String? = null, category: String? = null, genre: String? = null, status: String? = null): List<MediaItem> = emptyList()

  fun getMediaById(id: String): MediaItem?
  fun searchCatalog(query: String, genre: String?, mediaType: MediaType?): List<MediaItem>
  fun getTrendingKeywords(): List<String>
  fun getRecentSearches(): List<String>
  fun getAllGenres(): List<String>
  fun toggleMyList(mediaItem: MediaItem): Boolean
  fun isInMyList(id: String): Boolean
  fun getWatchHistory(): List<MediaItem>
  fun recordWatchProgress(mediaItem: MediaItem, progressPercent: Float = 0.35f)
  fun removeFromContinueWatching(mediaId: String) {}
}

