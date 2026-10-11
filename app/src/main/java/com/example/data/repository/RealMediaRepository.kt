package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.api.AnyMovieApiService
import com.example.data.api.ApiClient
import com.example.data.api.model.CastMemberDto
import com.example.data.api.model.EpisodeDto
import com.example.data.api.model.MediaDto
import com.example.data.api.model.PlaybackResolveRequest
import com.example.data.model.CastMember
import com.example.data.model.ContentRowSection
import com.example.data.model.DownloadOption
import com.example.data.model.DownloadsInfo
import com.example.data.model.Episode
import com.example.data.model.LiveTvChannel
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.PlaybackResolved
import com.example.data.model.Season
import com.example.data.model.ServerItem
import com.example.data.model.TrailerInfo
import com.example.data.util.MediaEnrichmentRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

class RealMediaRepository(
  private val context: Context? = null,
  private val apiService: AnyMovieApiService = ApiClient.apiService
) : MediaRepository {

  private val tag = "RealMediaRepository"
  private val prefs: SharedPreferences? = context?.getSharedPreferences("all_ott_user_data", Context.MODE_PRIVATE)
  private val episodeCache = ConcurrentHashMap<String, List<Episode>>()

  // In-memory cache for fast, instant lookups across navigation
  private val memoryCache = ConcurrentHashMap<String, MediaItem>()

  // Reactive StateFlows for UI observing
  private val heroFlow = MutableStateFlow<List<MediaItem>>(emptyList())
  private val contentRowsFlow = MutableStateFlow<List<ContentRowSection>>(emptyList())
  private val comingSoonFlow = MutableStateFlow<List<MediaItem>>(emptyList())
  private val continueWatchingFlow = MutableStateFlow<List<MediaItem>>(emptyList())
  private val myListFlow = MutableStateFlow<List<MediaItem>>(emptyList())
  private val myListIds = MutableStateFlow<MutableSet<String>>(mutableSetOf())

  // Anime & Live TV Flows
  private val animeFlow = MutableStateFlow<List<MediaItem>>(emptyList())
  private val liveTvFlow = MutableStateFlow<List<LiveTvChannel>>(emptyList())

  // Dynamic Genres and Platforms from API
  private val genresFlow = MutableStateFlow<List<String>>(listOf("All", "Movies", "TV Series", "Action", "Drama", "Anime"))

  init {
    com.example.data.util.CuratedAnimeCatalog.items.forEach { item ->
      memoryCache[item.id] = item
    }
    loadPersistedWatchlist()
    loadPersistedContinueWatching()
    CoroutineScope(Dispatchers.IO).launch {
      refreshHome()
      refreshAnime()
      refreshLiveTv()
    }
  }

  private fun loadPersistedWatchlist() {
    val saved = prefs?.getStringSet("watchlist_ids", emptySet()) ?: emptySet()
    myListIds.value = saved.toMutableSet()
  }

  private fun savePersistedWatchlist() {
    prefs?.edit()?.putStringSet("watchlist_ids", myListIds.value)?.apply()
  }

  private fun loadPersistedContinueWatching() {
    val jsonString = prefs?.getString("continue_watching_data", null)
    if (!jsonString.isNullOrBlank()) {
      try {
        val entries = jsonString.split(";;;").filter { it.isNotBlank() }
        val items = entries.mapNotNull { entry ->
          val parts = entry.split("|||")
          if (parts.size >= 2) {
            val id = parts[0]
            val progress = parts[1].toFloatOrNull() ?: 0.4f
            val cached = memoryCache[id]
            cached?.copy(progressPercent = progress) ?: MediaItem(
              id = id,
              title = parts.getOrNull(2) ?: "Media",
              overview = "",
              backdropUrl = parts.getOrNull(4) ?: "",
              posterUrl = parts.getOrNull(3) ?: "",
              mediaType = if (parts.getOrNull(5) == "SERIES") MediaType.SERIES else MediaType.MOVIE,
              rating = parts.getOrNull(6)?.toDoubleOrNull() ?: 8.5,
              releaseYear = parts.getOrNull(7)?.toIntOrNull() ?: 2024,
              duration = parts.getOrNull(8) ?: "2h",
              genres = listOf("Entertainment"),
              progressPercent = progress
            )
          } else null
        }
        if (items.isNotEmpty()) {
          continueWatchingFlow.value = items
        }
      } catch (e: Exception) {
        Log.e(tag, "Error restoring continue watching items", e)
      }
    }
  }

  private fun savePersistedContinueWatching() {
    val items = continueWatchingFlow.value
    val serialized = items.joinToString(";;;") { item ->
      "${item.id}|||${item.progressPercent ?: 0.35f}|||${item.title}|||${item.posterUrl}|||${item.backdropUrl}|||${item.mediaType.name}|||${item.rating}|||${item.releaseYear}|||${item.duration}"
    }
    prefs?.edit()?.putString("continue_watching_data", serialized)?.apply()
  }

  override fun getFeaturedHero(): Flow<List<MediaItem>> = heroFlow.asStateFlow()
  override fun getContinueWatching(): Flow<List<MediaItem>> = continueWatchingFlow.asStateFlow()
  override fun getContentRows(): Flow<List<ContentRowSection>> = contentRowsFlow.asStateFlow()
  override fun getComingSoon(): Flow<List<MediaItem>> = comingSoonFlow.asStateFlow()
  override fun getMyList(): Flow<List<MediaItem>> = myListFlow.asStateFlow()
  override fun getAnimeContent(): Flow<List<MediaItem>> = animeFlow.asStateFlow()
  override fun getLiveTvChannelsFlow(): Flow<List<LiveTvChannel>> = liveTvFlow.asStateFlow()

  override suspend fun refreshHome(): Result<Unit> {
    return try {
      val response = apiService.getHome(limit = 20)
      if (response.success && response.data != null) {
        val home = response.data

        // 1. Featured Hero
        val heroItems = home.hero.map { it.toMediaItem() }
        heroItems.forEach { memoryCache[it.id] = it }
        heroFlow.value = heroItems

        // 2. Sections from API - ordered by user's exact specification:
        //    Trending Now -> Top on Netflix -> Popular on Prime Video -> JioHotstar Specials ->
        //    Blockbuster Bollywood & Regional -> Hollywood Hits -> Trending Anime -> Coming Soon to OTT & Theaters
        val targetOrder = listOf(
          "trending",
          "netflix",
          "prime",
          "jiostar",
          "bollywood",
          "hollywood",
          "anime",
          "comingSoon"
        )

        val rawSections = if (home.sections.isNotEmpty()) {
          home.sections.map { sectionDto ->
            val items = sectionDto.items.map { dto ->
              val item = dto.toMediaItem()
              memoryCache[item.id] = item
              item
            }
            ContentRowSection(
              id = sectionDto.id,
              title = sectionDto.title,
              subtitle = null,
              items = items
            )
          }
        } else {
          val fallback = mutableListOf<ContentRowSection>()
          if (home.trending.isNotEmpty()) {
            fallback.add(ContentRowSection("trending", "Trending Now", null, home.trending.map { it.toMediaItem() }))
          }
          if (home.popularSeries.isNotEmpty()) {
            fallback.add(ContentRowSection("netflix", "Top on Netflix", null, home.popularSeries.map { it.toMediaItem() }))
          }
          if (home.popularMovies.isNotEmpty()) {
            fallback.add(ContentRowSection("prime", "Popular on Prime Video", null, home.popularMovies.map { it.toMediaItem() }))
          }
          fallback
        }

        // Sort sections according to the exact target order specified
        val sortedSections = rawSections.sortedWith(compareBy { section ->
          val idx = targetOrder.indexOf(section.id)
          if (idx != -1) idx else 999
        })

        // CRITICAL REQUIREMENT: Strict deduplication across rows.
        // A movie in "Trending Now" will NEVER be repeated in "Top on Netflix", "Popular on Prime Video", "JioHotstar", etc.
        val seenMediaIds = mutableSetOf<String>()
        val seenTitles = mutableSetOf<String>()

        val deduplicatedSections = sortedSections.mapNotNull { section ->
          val uniqueItems = section.items.filter { item ->
            val normTitle = item.title.trim().lowercase()
            if (seenMediaIds.contains(item.id) || seenTitles.contains(normTitle)) {
              false
            } else {
              seenMediaIds.add(item.id)
              seenTitles.add(normTitle)
              true
            }
          }
          if (uniqueItems.isNotEmpty()) {
            section.copy(items = uniqueItems)
          } else {
            null
          }
        }
        contentRowsFlow.value = deduplicatedSections

        // Extract anime section if present to seed anime screen
        val animeSection = rawSections.find { it.id == "anime" }
        if (animeSection != null && animeSection.items.isNotEmpty()) {
          animeFlow.value = animeSection.items
        }

        // Continue watching reflects actual user watch history loaded from local storage
        val currentCw = continueWatchingFlow.value
        if (currentCw.isNotEmpty()) {
          val enrichedCw = currentCw.map { cwItem ->
            val cached = memoryCache[cwItem.id]
            if (cached != null) {
              cached.copy(progressPercent = cwItem.progressPercent ?: 0.35f)
            } else {
              cwItem
            }
          }
          continueWatchingFlow.value = enrichedCw
        }

        // 3. Coming Soon
        val comingSoon = home.comingSoon.map { it.toMediaItem() }
        comingSoon.forEach { memoryCache[it.id] = it }
        comingSoonFlow.value = comingSoon

        // 4. Genres
        val apiGenres = home.genres.mapNotNull { it.name }.filter { it.isNotBlank() }
        if (apiGenres.isNotEmpty()) {
          val distinctGenres = mutableListOf("All")
          distinctGenres.addAll(apiGenres.distinct())
          genresFlow.value = distinctGenres
        }

        // Update Watchlist items in memory
        updateMyListFlow()

        Result.success(Unit)
      } else {
        Log.e(tag, "Failed to load home: ${response.message}")
        Result.failure(Exception(response.message ?: "Failed to load home data"))
      }
    } catch (e: Exception) {
      Log.e(tag, "Exception during refreshHome", e)
      Result.failure(e)
    }
  }

  private suspend fun refreshAnime() {
    try {
      val response = apiService.discover(category = "anime", limit = 30)
      if (response.success && response.data != null && response.data.isNotEmpty()) {
        val items = response.data.map { dto ->
          val item = dto.toMediaItem()
          memoryCache[item.id] = item
          item
        }
        animeFlow.value = items
      }
    } catch (e: Exception) {
      Log.w(tag, "Could not fetch discover anime: ${e.message}")
    }
  }

  private suspend fun refreshLiveTv() {
    try {
      val channels = getLiveTvChannels()
      if (channels.isNotEmpty()) {
        liveTvFlow.value = channels
      }
    } catch (e: Exception) {
      Log.w(tag, "Could not fetch live tv: ${e.message}")
    }
  }

  override suspend fun getMediaDetails(id: String, type: String?): MediaItem? {
    val curatedMatch = com.example.data.util.CuratedAnimeCatalog.items.firstOrNull { it.id == id }
    if (curatedMatch != null) {
      memoryCache[id] = curatedMatch
      return curatedMatch
    }

    val cached = memoryCache[id]
    val isSeriesHint = cached?.mediaType == MediaType.SERIES || type in listOf("tv", "series")

    return try {
      val response = if (isSeriesHint) {
        apiService.getSeries(id)
      } else {
        apiService.getMovie(id)
      }

      val baseItem = if (response.success && response.data != null) {
        response.data.toMediaItem()
      } else {
        cached
      }

      if (baseItem == null) return null

      if (baseItem.mediaType == MediaType.SERIES) {
        // Ensure all seasons are represented
        val availableSeasons = if (baseItem.seasons.isNotEmpty()) {
          baseItem.seasons
        } else {
          MediaEnrichmentRegistry.getKnownSeasons(baseItem.id, baseItem.title)
        }

        // Fetch Season 1 episodes eagerly
        val s1Episodes = getSeasonEpisodes(baseItem.id, 1)
        val updatedSeasons = availableSeasons.map { s ->
          if (s.seasonNumber == 1 && s1Episodes.isNotEmpty()) {
            s.copy(episodes = s1Episodes)
          } else {
            s
          }
        }
        val fullItem = baseItem.copy(seasons = updatedSeasons)
        memoryCache[fullItem.id] = fullItem
        fullItem
      } else {
        memoryCache[baseItem.id] = baseItem
        baseItem
      }
    } catch (e: Exception) {
      Log.e(tag, "Error fetching media details for $id", e)
      cached
    }
  }

  override suspend fun getSeasonEpisodes(seriesId: String, seasonNumber: Int): List<Episode> {
    val cacheKey = "${seriesId}_s${seasonNumber}"
    episodeCache[cacheKey]?.let { return it }

    return try {
      val response = apiService.getSeasonEpisodes(seriesId, seasonNumber)
      if (response.success && response.data != null && response.data.episodes.isNotEmpty()) {
        val seriesTitle = memoryCache[seriesId]?.title ?: "Series"
        val backdrop = memoryCache[seriesId]?.backdropUrl ?: ""
        val list = response.data.episodes.map { epDto ->
          epDto.toEpisode(seriesTitle, backdrop)
        }
        episodeCache[cacheKey] = list
        list
      } else {
        val seriesTitle = memoryCache[seriesId]?.title ?: "Series"
        val backdrop = memoryCache[seriesId]?.backdropUrl ?: ""
        val fallbackList = MediaEnrichmentRegistry.generateFallbackEpisodes(seriesId, seasonNumber, seriesTitle, backdrop)
        episodeCache[cacheKey] = fallbackList
        fallbackList
      }
    } catch (e: Exception) {
      Log.e(tag, "Error fetching season $seasonNumber episodes for $seriesId", e)
      val seriesTitle = memoryCache[seriesId]?.title ?: "Series"
      val backdrop = memoryCache[seriesId]?.backdropUrl ?: ""
      val fallbackList = MediaEnrichmentRegistry.generateFallbackEpisodes(seriesId, seasonNumber, seriesTitle, backdrop)
      episodeCache[cacheKey] = fallbackList
      fallbackList
    }
  }

  override suspend fun searchReal(query: String, type: String?): List<MediaItem> {
    if (query.isBlank()) return emptyList()
    return try {
      val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
      val response = apiService.search(query = encodedQuery, limit = 30, type = type)
      if (response.success && response.data != null) {
        response.data.map { dto ->
          val item = dto.toMediaItem()
          memoryCache[item.id] = item
          item
        }
      } else {
        emptyList()
      }
    } catch (e: kotlinx.coroutines.CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(tag, "Error searching for '$query'", e)
      emptyList()
    }
  }

  override suspend fun getServers(
    contentId: String,
    type: String,
    season: Int?,
    episode: Int?,
    tmdbId: Int?,
    title: String?
  ): List<ServerItem> {
    val cleanId = MediaEnrichmentRegistry.getCleanId(contentId, tmdbId, title)
    val isTv = type in listOf("series", "tv")
    val s = season ?: 1
    val e = episode ?: 1

    return try {
      val response = if (isTv) {
        apiService.getSeriesEpisodeServers(
          id = cleanId,
          season = s,
          episode = e
        )
      } else {
        apiService.getMovieServers(cleanId)
      }

      val rawServers = if (response.success && response.data != null) {
        response.data.servers.map { srv ->
          val directUrl = MediaEnrichmentRegistry.constructDirectServerUrl(srv.id, cleanId, isTv, s, e)
          ServerItem(
            id = srv.id,
            name = srv.name,
            type = srv.type ?: "embed",
            quality = srv.quality ?: "1080p",
            language = srv.language ?: "Multi-Audio",
            url = srv.url?.ifBlank { directUrl } ?: directUrl,
            securePlayerUrl = srv.securePlayerUrl?.ifBlank { directUrl } ?: directUrl,
            available = srv.available,
            priority = srv.priority
          )
        }
      } else {
        emptyList()
      }

      val defaultServers = listOf(
        ServerItem(id = "srv_tgvid", name = "Server 1 (TGVid Stream)", type = "embed", quality = "1080p FHD", language = "Multi-Audio (Hindi/Eng)", url = MediaEnrichmentRegistry.constructDirectServerUrl("srv_tgvid", cleanId, isTv, s, e), securePlayerUrl = null, available = true, priority = 0),
        ServerItem(id = "srv_vidsrc", name = "Server 2 (VidSrc / Vidstack)", type = "embed", quality = "1080p FHD", language = "Multi-Audio (Hindi/Eng)", url = MediaEnrichmentRegistry.constructDirectServerUrl("srv_vidsrc", cleanId, isTv, s, e), securePlayerUrl = null, available = true, priority = 1),
        ServerItem(id = "srv_videasy", name = "Server 3 (Videasy HD)", type = "embed", quality = "1080p FHD", language = "Multi-Language", url = MediaEnrichmentRegistry.constructDirectServerUrl("srv_videasy", cleanId, isTv, s, e), securePlayerUrl = null, available = true, priority = 2),
        ServerItem(id = "srv_2embed", name = "Server 4 (2Embed)", type = "embed", quality = "1080p FHD", language = "Multi-Subtitles", url = MediaEnrichmentRegistry.constructDirectServerUrl("srv_2embed", cleanId, isTv, s, e), securePlayerUrl = null, available = true, priority = 3),
        ServerItem(id = "srv_vidsrcicu", name = "Server 5 (VidSrc ICU)", type = "embed", quality = "1080p FHD", language = "Original Audio", url = MediaEnrichmentRegistry.constructDirectServerUrl("srv_vidsrcicu", cleanId, isTv, s, e), securePlayerUrl = null, available = true, priority = 4),
        ServerItem(id = "srv_embedsu", name = "Server 6 (EmbedSu)", type = "embed", quality = "1080p FHD", language = "Multi-Language", url = MediaEnrichmentRegistry.constructDirectServerUrl("srv_embedsu", cleanId, isTv, s, e), securePlayerUrl = null, available = true, priority = 5),
        ServerItem(id = "srv_smashy", name = "Server 7 (SmashyStream)", type = "embed", quality = "1080p FHD", language = "Multi-Audio", url = MediaEnrichmentRegistry.constructDirectServerUrl("srv_smashy", cleanId, isTv, s, e), securePlayerUrl = null, available = true, priority = 6),
        ServerItem(id = "srv_autoembed", name = "Server 8 (AutoEmbed)", type = "embed", quality = "1080p FHD", language = "Multi-Language", url = MediaEnrichmentRegistry.constructDirectServerUrl("srv_autoembed", cleanId, isTv, s, e), securePlayerUrl = null, available = true, priority = 7)
      )

      val merged = (rawServers + defaultServers).distinctBy { it.id }.sortedBy { it.priority }
      merged
    } catch (err: Exception) {
      Log.e(tag, "Error getting servers for $cleanId ($type)", err)
      listOf(
        ServerItem(id = "srv_tgvid", name = "Server 1 (TGVid Stream)", type = "embed", quality = "1080p FHD", language = "Multi-Audio (Hindi/Eng)", url = MediaEnrichmentRegistry.constructDirectServerUrl("srv_tgvid", cleanId, isTv, s, e), securePlayerUrl = null, available = true, priority = 0),
        ServerItem(id = "srv_vidsrc", name = "Server 2 (VidSrc / Vidstack)", type = "embed", quality = "1080p FHD", language = "Multi-Audio (Hindi/Eng)", url = MediaEnrichmentRegistry.constructDirectServerUrl("srv_vidsrc", cleanId, isTv, s, e), securePlayerUrl = null, available = true, priority = 1),
        ServerItem(id = "srv_videasy", name = "Server 3 (Videasy HD)", type = "embed", quality = "1080p FHD", language = "Multi-Language", url = MediaEnrichmentRegistry.constructDirectServerUrl("srv_videasy", cleanId, isTv, s, e), securePlayerUrl = null, available = true, priority = 2),
        ServerItem(id = "srv_2embed", name = "Server 4 (2Embed)", type = "embed", quality = "1080p FHD", language = "Multi-Subtitles", url = MediaEnrichmentRegistry.constructDirectServerUrl("srv_2embed", cleanId, isTv, s, e), securePlayerUrl = null, available = true, priority = 3)
      )
    }
  }

  override suspend fun resolvePlayback(
    contentId: String,
    type: String,
    serverId: String,
    season: Int?,
    episode: Int?,
    tmdbId: Int?,
    title: String?
  ): PlaybackResolved? {
    val cleanId = MediaEnrichmentRegistry.getCleanId(contentId, tmdbId, title)
    val isTv = type in listOf("series", "tv")
    val apiType = if (isTv) "tv" else "movie"
    val s = season ?: 1
    val e = episode ?: 1

    val fallbackUrl = MediaEnrichmentRegistry.constructDirectServerUrl(serverId, cleanId, isTv, s, e)

    return try {
      val request = PlaybackResolveRequest(
        contentId = cleanId,
        type = apiType,
        serverId = serverId,
        season = if (isTv) s else null,
        episode = if (isTv) e else null
      )
      val response = apiService.resolvePlayback(request)
      if (response.success && response.data != null) {
        val data = response.data
        val rawSec = data.securePlayerUrl?.trim()
        val rawEmb = data.embedUrl?.trim()
        val rawStr = data.streamUrl?.trim()

        val resolvedUrl = when {
          !rawSec.isNullOrBlank() -> rawSec
          !rawEmb.isNullOrBlank() -> rawEmb
          !rawStr.isNullOrBlank() -> rawStr
          else -> fallbackUrl
        }

        PlaybackResolved(
          contentId = cleanId,
          type = apiType,
          serverId = serverId,
          serverName = sanitizeServerName(serverId, data.serverName),
          streamUrl = resolvedUrl,
          securePlayerUrl = resolvedUrl,
          quality = data.quality ?: "1080p",
          language = data.language ?: "Multi-Audio",
          season = if (isTv) s else null,
          episode = if (isTv) e else null,
          available = true
        )
      } else {
        PlaybackResolved(
          contentId = cleanId,
          type = apiType,
          serverId = serverId,
          serverName = sanitizeServerName(serverId, null),
          streamUrl = fallbackUrl,
          securePlayerUrl = fallbackUrl,
          quality = "1080p FHD",
          language = "Multi-Audio",
          season = if (isTv) s else null,
          episode = if (isTv) e else null,
          available = true
        )
      }
    } catch (err: Exception) {
      Log.e(tag, "Error resolving playback for $cleanId ($serverId)", err)
      PlaybackResolved(
        contentId = cleanId,
        type = apiType,
        serverId = serverId,
        serverName = sanitizeServerName(serverId, null),
        streamUrl = fallbackUrl,
        securePlayerUrl = fallbackUrl,
        quality = "1080p FHD",
        language = "Multi-Audio",
        season = if (isTv) s else null,
        episode = if (isTv) e else null,
        available = true
      )
    }
  }

  private fun sanitizeServerName(serverId: String, rawName: String?): String {
    val clean = rawName?.trim().orEmpty()
    val sId = serverId.lowercase().trim()
    if (sId == "srv_tgvid" || sId == "srv_1" || sId.contains("tgvid")) {
      return "Server 1 (TGVid Stream)"
    }
    if (clean.contains("VidStuck", ignoreCase = true) || clean.contains("VidStack", ignoreCase = true)) {
      if (sId == "srv_tgvid" || sId == "srv_1" || sId.contains("tgvid")) {
        return "Server 1 (TGVid Stream)"
      }
      return "Server 2 (VidSrc Stream)"
    }
    if (clean.isNotBlank()) {
      return clean
    }
    return getServerNameById(serverId)
  }

  private fun getServerNameById(serverId: String): String {
    val sId = serverId.lowercase().trim()
    return when {
      sId.contains("tgvid") || sId == "srv_tgvid" || sId == "srv_1" -> "Server 1 (TGVid Stream)"
      sId.contains("vidsrc") || sId.contains("vidstack") || sId.contains("vidstuck") || sId == "srv_vidsrc" -> "Server 2 (VidSrc Stream)"
      sId.contains("videasy") || sId == "srv_videasy" || sId == "srv_2" -> "Server 3 (Videasy HD)"
      sId.contains("2embed") || sId == "srv_2embed" || sId == "srv_3" -> "Server 4 (2Embed)"
      sId.contains("vidsrcicu") || sId == "srv_vidsrcicu" || sId == "srv_4" -> "Server 5 (VidSrc ICU)"
      sId.contains("embedsu") || sId == "srv_embedsu" || sId == "srv_5" -> "Server 6 (EmbedSu)"
      sId.contains("smashy") || sId == "srv_smashy" || sId == "srv_6" -> "Server 7 (SmashyStream)"
      sId.contains("autoembed") || sId == "srv_autoembed" || sId == "srv_7" -> "Server 8 (AutoEmbed)"
      else -> "Server 1 (TGVid Stream)"
    }
  }

  override suspend fun getLiveTvChannels(): List<LiveTvChannel> {
    return try {
      val response = apiService.getLiveTvChannels()
      if (response.success && response.data != null) {
        val channels = response.data.channels.map { dto ->
          val isHd = dto.name.contains("HD", ignoreCase = true)
          LiveTvChannel(
            id = dto.id,
            name = dto.name,
            category = dto.category ?: "Entertainment",
            logoUrl = dto.logo ?: "",
            embedUrl = dto.embed,
            watchUrl = dto.watch,
            isLive = true,
            isHd = isHd
          )
        }
        liveTvFlow.value = channels
        channels
      } else {
        emptyList()
      }
    } catch (e: Exception) {
      Log.e(tag, "Error getting live tv channels", e)
      emptyList()
    }
  }

  override suspend fun getDownloads(
    id: String,
    type: String,
    season: Int?,
    episode: Int?
  ): DownloadsInfo? {
    return try {
      val apiType = if (type in listOf("tv", "series")) "tv" else "movie"
      val response = apiService.getDownloads(id = id, type = apiType, season = season, episode = episode)
      if (response.success && response.data != null && response.data.downloads.isNotEmpty()) {
        val data = response.data
        DownloadsInfo(
          type = data.type ?: apiType,
          tmdbId = data.tmdbId?.toString(),
          downloads = data.downloads.map { opt ->
            DownloadOption(
              name = opt.name ?: "Standard Download",
              quality = opt.quality ?: "1080p",
              size = opt.size ?: "Unknown",
              downloadUrl = opt.downloadUrl ?: ""
            )
          }
        )
      } else {
        null
      }
    } catch (e: Exception) {
      Log.e(tag, "Error getting downloads for $id", e)
      null
    }
  }

  override suspend fun getTrailer(query: String): TrailerInfo? {
    return try {
      val response = apiService.getTrailer(URLEncoder.encode(query, "UTF-8"))
      if (response.success && response.data != null && !response.data.videoId.isNullOrBlank()) {
        TrailerInfo(
          query = response.data.query ?: query,
          videoId = response.data.videoId!!
        )
      } else {
        null
      }
    } catch (e: Exception) {
      Log.e(tag, "Error fetching trailer for $query", e)
      null
    }
  }

  override suspend fun discoverContent(
    platform: String?,
    category: String?,
    genre: String?,
    status: String?
  ): List<MediaItem> {
    return try {
      val response = apiService.discover(
        platform = platform,
        category = category,
        genre = genre,
        status = status,
        limit = 30
      )
      if (response.success && response.data != null) {
        response.data.map { dto ->
          val item = dto.toMediaItem()
          memoryCache[item.id] = item
          item
        }
      } else {
        emptyList()
      }
    } catch (e: Exception) {
      Log.e(tag, "Error in discoverContent", e)
      emptyList()
    }
  }

  override fun getMediaById(id: String): MediaItem? {
    return memoryCache[id] ?: com.example.data.util.CuratedAnimeCatalog.items.firstOrNull { it.id == id }
  }

  override fun searchCatalog(query: String, genre: String?, mediaType: MediaType?): List<MediaItem> {
    return memoryCache.values.filter { item ->
      val matchesQuery = query.isBlank() ||
          item.title.contains(query, ignoreCase = true) ||
          item.genres.any { it.contains(query, ignoreCase = true) } ||
          item.overview.contains(query, ignoreCase = true)

      val matchesGenre = genre == null || genre == "All" || item.genres.any { it.equals(genre, ignoreCase = true) }
      val matchesType = mediaType == null || item.mediaType == mediaType

      matchesQuery && matchesGenre && matchesType
    }
  }

  override fun getTrendingKeywords(): List<String> {
    return listOf("Spider-Man", "Anime", "Pushpa 2", "Stranger Things", "Extraction 2", "Demon Slayer", "Wednesday", "Toxic")
  }

  override fun getRecentSearches(): List<String> {
    return listOf("Spider-Man", "Pushpa 2", "Anime", "Stranger Things")
  }

  override fun getAllGenres(): List<String> {
    return genresFlow.value
  }

  override fun toggleMyList(mediaItem: MediaItem): Boolean {
    val set = myListIds.value
    val added = if (set.contains(mediaItem.id)) {
      set.remove(mediaItem.id)
      false
    } else {
      set.add(mediaItem.id)
      true
    }
    savePersistedWatchlist()
    updateMyListFlow()
    return added
  }

  override fun isInMyList(id: String): Boolean {
    return myListIds.value.contains(id)
  }

  private fun updateMyListFlow() {
    val set = myListIds.value
    myListFlow.value = memoryCache.values.filter { set.contains(it.id) }
  }

  override fun getWatchHistory(): List<MediaItem> {
    return continueWatchingFlow.value
  }

  override fun recordWatchProgress(mediaItem: MediaItem, progressPercent: Float) {
    memoryCache[mediaItem.id] = mediaItem
    val current = continueWatchingFlow.value.toMutableList()
    val index = current.indexOfFirst { it.id == mediaItem.id }
    val safeProgress = progressPercent.coerceIn(0.05f, 0.95f)
    val updatedItem = mediaItem.copy(progressPercent = safeProgress)
    if (index >= 0) {
      current.removeAt(index)
    }
    current.add(0, updatedItem)
    val trimmed = current.take(12)
    continueWatchingFlow.value = trimmed
    savePersistedContinueWatching()
  }

  override fun removeFromContinueWatching(mediaId: String) {
    val current = continueWatchingFlow.value.toMutableList()
    val removed = current.removeAll { it.id == mediaId }
    if (removed) {
      continueWatchingFlow.value = current
      savePersistedContinueWatching()
    }
  }

  // --- DTO Mapping Helpers ---

  private fun MediaDto.toMediaItem(): MediaItem {
    val isSeriesType = isSeries == true || type in listOf("tv", "series")
    val parsedRating = when {
      voteAverage != null && voteAverage > 0.0 -> voteAverage
      rating != null -> {
        val numStr = rating.replace("/10", "").trim()
        numStr.toDoubleOrNull() ?: 8.2
      }
      else -> 8.2
    }

    val parsedYear = when (year) {
      is Number -> year.toInt()
      is String -> year.toIntOrNull() ?: 2024
      else -> {
        if (!releaseDate.isNullOrBlank() && releaseDate.length >= 4) {
          releaseDate.substring(0, 4).toIntOrNull() ?: 2024
        } else {
          2024
        }
      }
    }

    // Critical Coming Soon business logic:
    // Only true when isUpcoming == true OR releaseStatus == "COMING_SOON"
    // Released titles must NEVER receive the Coming Soon badge
    val statusUpper = releaseStatus?.trim()?.uppercase()
    val isUpcomingClean = (isUpcoming == true || statusUpper == "COMING_SOON") && statusUpper != "RELEASED"

    val rawCast = cast?.mapNotNull { it.toCastMember() } ?: emptyList()
    val enrichedCast = MediaEnrichmentRegistry.resolveCast(id, title ?: "", rawCast)

    val parsedSeasons = if (isSeriesType) {
      if (!seasons.isNullOrEmpty()) {
        seasons.map { sDto ->
          Season(
            seasonNumber = sDto.seasonNumber,
            title = sDto.name?.ifBlank { "Season ${sDto.seasonNumber}" } ?: "Season ${sDto.seasonNumber}",
            episodes = emptyList()
          )
        }
      } else {
        MediaEnrichmentRegistry.getKnownSeasons(id, title ?: "")
      }
    } else {
      emptyList()
    }

    val rawPoster = poster?.trim()
    val rawBackdrop = backdrop?.trim()

    val resolvedPoster = when {
      !rawPoster.isNullOrBlank() && rawPoster.startsWith("http") -> rawPoster
      !rawPoster.isNullOrBlank() && rawPoster.startsWith("/") -> "https://image.tmdb.org/t/p/w500$rawPoster"
      !rawPoster.isNullOrBlank() -> "https://image.tmdb.org/t/p/w500/$rawPoster"
      !rawBackdrop.isNullOrBlank() && rawBackdrop.startsWith("http") -> rawBackdrop
      !rawBackdrop.isNullOrBlank() && rawBackdrop.startsWith("/") -> "https://image.tmdb.org/t/p/w500$rawBackdrop"
      else -> "https://image.tmdb.org/t/p/w500/74xTEgt7R36Fpooo50r9T25onhq.jpg"
    }

    val resolvedBackdrop = when {
      !rawBackdrop.isNullOrBlank() && rawBackdrop.startsWith("http") -> rawBackdrop
      !rawBackdrop.isNullOrBlank() && rawBackdrop.startsWith("/") -> "https://image.tmdb.org/t/p/w1280$rawBackdrop"
      !rawBackdrop.isNullOrBlank() -> "https://image.tmdb.org/t/p/w1280/$rawBackdrop"
      !rawPoster.isNullOrBlank() && rawPoster.startsWith("http") -> rawPoster
      !rawPoster.isNullOrBlank() && rawPoster.startsWith("/") -> "https://image.tmdb.org/t/p/w1280$rawPoster"
      else -> "https://image.tmdb.org/t/p/w1280/56v2KjBlU4XaOv9rVYEQypROD7P.jpg"
    }

    val resolvedTitle = title?.ifBlank { originalTitle ?: "Untitled" } ?: "Untitled"
    val parsedTmdbId: Int? = when (tmdbId) {
      is Number -> tmdbId.toInt()
      is String -> tmdbId.replace("tmdb-", "").replace("imdb-", "").toIntOrNull()
      else -> id.replace("tmdb-", "").replace("imdb-", "").toIntOrNull()
    } ?: MediaEnrichmentRegistry.getTmdbIdForMedia(id, resolvedTitle)

    return MediaItem(
      id = id,
      tmdbId = parsedTmdbId,
      title = resolvedTitle,
      tagline = "",
      overview = overview?.ifBlank { "Stream in high-definition with high-speed multi-server streaming." } ?: "",
      backdropUrl = resolvedBackdrop,
      posterUrl = resolvedPoster,
      mediaType = if (isSeriesType) MediaType.SERIES else MediaType.MOVIE,
      rating = parsedRating,
      releaseYear = parsedYear,
      duration = duration ?: if (isSeriesType) "Series" else "${runtime ?: 120} min",
      genres = genres?.ifEmpty { listOf("Drama", "Action") } ?: listOf("Drama", "Action"),
      maturityRating = if (isSeriesType) "TV-MA" else "PG-13",
      audioFormats = "FHD 1080p",
      isTrending = true,
      isTopRated = parsedRating >= 8.5,
      isComingSoon = isUpcomingClean,
      releaseDate = releaseDate,
      cast = enrichedCast,
      seasons = parsedSeasons,
      director = director ?: "Official Studio",
      streamingPlatform = platform?.replaceFirstChar { it.uppercase() } ?: "Netflix"
    )
  }

  private fun CastMemberDto.toCastMember(): CastMember? {
    val actorName = name?.trim() ?: return null
    val rawImage = profileImage?.trim()
    val portrait = when {
      !rawImage.isNullOrBlank() && rawImage.startsWith("http") -> rawImage
      !rawImage.isNullOrBlank() && rawImage.startsWith("/") -> "https://image.tmdb.org/t/p/w300$rawImage"
      !rawImage.isNullOrBlank() -> "https://image.tmdb.org/t/p/w300/$rawImage"
      else -> MediaEnrichmentRegistry.getActorPortrait(actorName)
    }
    return CastMember(
      id = id?.toString() ?: actorName.hashCode().toString(),
      name = actorName,
      character = character?.ifBlank { "Star" } ?: "Star",
      profileImageUrl = portrait
    )
  }

  private fun EpisodeDto.toEpisode(seriesTitle: String, fallbackBackdrop: String): Episode {
    val num = episodeNumber
    val sNum = seasonNumber
    val epDuration = when (runtime) {
      is Number -> "${runtime} min"
      is String -> if (runtime.contains("min")) runtime else "${runtime} min"
      else -> "45 min"
    }
    val epRating = when (val r = rating) {
      is Number -> r.toDouble()
      is String -> {
        val clean = r.replace("/10", "").trim()
        clean.toDoubleOrNull() ?: 8.4
      }
      else -> 8.4
    }
    val rawThumb = thumbnail?.trim()
    val cleanThumb = when {
      !rawThumb.isNullOrBlank() && rawThumb.startsWith("http") -> rawThumb
      !rawThumb.isNullOrBlank() && rawThumb.startsWith("/") -> "https://image.tmdb.org/t/p/w500$rawThumb"
      !rawThumb.isNullOrBlank() -> "https://image.tmdb.org/t/p/w500/$rawThumb"
      else -> fallbackBackdrop
    }
    return Episode(
      id = id?.toString() ?: "ep_${sNum}_${num}",
      seasonNumber = sNum,
      episodeNumber = num,
      title = name.ifBlank { "Chapter $num" },
      overview = overview?.ifBlank { "Stream Chapter $num of Season $sNum in full HD quality." } ?: "",
      duration = epDuration,
      thumbnailUrl = cleanThumb,
      rating = epRating,
      airedDate = airDate ?: airedDate ?: "2024-01-15"
    )
  }
}

