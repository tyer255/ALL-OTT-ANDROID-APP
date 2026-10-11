package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ContentRowSection
import com.example.data.model.DownloadsInfo
import com.example.data.model.Episode
import com.example.data.model.LiveTvChannel
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.PlaybackResolved
import com.example.data.model.Season
import com.example.data.model.ServerItem
import com.example.data.model.TrailerInfo
import com.example.data.repository.MediaRepository
import com.example.data.repository.RealMediaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface Screen {
  object Home : Screen
  object Anime : Screen
  object LiveTv : Screen
  object Search : Screen
  object MyList : Screen
  object Profile : Screen
  data class Detail(val mediaId: String) : Screen
  data class Player(val mediaId: String, val episodeTitle: String? = null) : Screen
  data class LivePlayer(val channelId: String) : Screen
  data class TrailerPlayer(val title: String, val videoId: String) : Screen
}

data class StreamingProvider(
  val id: String,
  val name: String,
  val shortName: String,
  val brandColor: Long,
  val logoUrl: String,
  val tagline: String
)

data class PlayerPlaybackState(
  val isPlaying: Boolean = true,
  val currentPositionSeconds: Int = 0,
  val totalDurationSeconds: Int = 7200,
  val isSubtitlesEnabled: Boolean = true,
  val selectedQuality: String = "4K Ultra HD",
  val isMuted: Boolean = false,
  val episodeTitle: String? = null,
  val streamUrl: String? = null,
  val securePlayerUrl: String? = null,
  val serverName: String? = null,
  val serverQuality: String? = null
)

data class AetherUiState(
  val currentScreen: Screen = Screen.Home,
  val featuredHeroItems: List<MediaItem> = emptyList(),
  val currentHeroIndex: Int = 0,
  val continueWatchingItems: List<MediaItem> = emptyList(),
  val contentRows: List<ContentRowSection> = emptyList(),
  val comingSoonItems: List<MediaItem> = emptyList(),
  val myListItems: List<MediaItem> = emptyList(),
  val animeItems: List<MediaItem> = emptyList(),
  val isLoadingAnime: Boolean = false,
  val liveTvChannels: List<LiveTvChannel> = emptyList(),
  val isLoadingLiveTv: Boolean = false,
  val selectedLiveTvChannel: LiveTvChannel? = null,
  val watchHistory: List<MediaItem> = emptyList(),
  val selectedDetailMedia: MediaItem? = null,
  val searchQuery: String = "",
  val selectedSearchType: String = "all", // "all", "movie", "tv", "anime"
  val selectedSearchGenre: String = "All",
  val searchResults: List<MediaItem> = emptyList(),
  val trendingKeywords: List<String> = emptyList(),
  val recentSearches: List<String> = emptyList(),
  val availableGenres: List<String> = emptyList(),
  val playbackState: PlayerPlaybackState = PlayerPlaybackState(),
  val toastMessage: String? = null,
  val providers: List<StreamingProvider> = emptyList(),
  val selectedProviderId: String? = null,
  val isLoadingHome: Boolean = false,
  val isSearching: Boolean = false,
  val isLoadingDetails: Boolean = false,
  val availableServers: List<ServerItem> = emptyList(),
  val isResolvingPlayback: Boolean = false,
  val showServerSelection: Boolean = false,
  val pendingPlayMedia: MediaItem? = null,
  val pendingPlaySeason: Int? = null,
  val pendingPlayEpisode: Int? = null,
  val pendingPlayEpisodeTitle: String? = null,
  val showDownloadsSheet: Boolean = false,
  val availableDownloads: DownloadsInfo? = null,
  val isLoadingDownloads: Boolean = false,
  val downloadTargetMedia: MediaItem? = null
)

class AetherViewModel(
  application: Application,
  private val repository: MediaRepository
) : AndroidViewModel(application) {

  constructor(application: Application) : this(
    application,
    RealMediaRepository(context = application)
  )


  private val _uiState = MutableStateFlow(AetherUiState())
  val uiState: StateFlow<AetherUiState> = _uiState.asStateFlow()

  private val screenBackStack = mutableListOf<Screen>(Screen.Home)
  private var searchDebounceJob: Job? = null

  // OTT platforms configured: Netflix, Prime Video, JioHotstar, Crunchyroll, Apple TV+
  private val ottProviders = listOf(
    StreamingProvider("netflix", "Netflix", "N", 0xFFE50914, "https://images.unsplash.com/photo-1574375927938-d5a98e8ffe85?w=200", "Originals & Blockbusters"),
    StreamingProvider("prime", "Prime Video", "prime", 0xFF00A8E1, "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=200", "Movies & TV Shows"),
    StreamingProvider("jiostar", "JioHotstar", "Jio", 0xFFFFCC00, "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=200", "Hotstar Specials & Cricket"),
    StreamingProvider("crunchyroll", "Crunchyroll", "CR", 0xFFF47521, "https://images.unsplash.com/photo-1563089145-599997674d42?w=200", "World's Largest Anime"),
    StreamingProvider("apple", "Apple TV+", "tv+", 0xFF333333, "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=200", "Award-Winning Originals")
  )

  init {
    loadInitialData()
    startHeroAutoRotation()
  }

  fun retryLoadHome() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoadingHome = true) }
      repository.refreshHome()
      _uiState.update { it.copy(isLoadingHome = false) }
    }
  }

  fun retryLoadLiveTv() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoadingLiveTv = true) }
      val channels = repository.getLiveTvChannels()
      _uiState.update { it.copy(liveTvChannels = channels, isLoadingLiveTv = false) }
    }
  }

  private fun loadInitialData() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoadingHome = true, isLoadingAnime = true, isLoadingLiveTv = true) }
      repository.refreshHome()
      _uiState.update { it.copy(isLoadingHome = false) }
    }

    viewModelScope.launch {
      repository.getContinueWatching().collect { items ->
        _uiState.update { it.copy(continueWatchingItems = items) }
      }
    }

    viewModelScope.launch {
      repository.getFeaturedHero().collect { items ->
        _uiState.update { it.copy(featuredHeroItems = items) }
      }
    }

    viewModelScope.launch {
      repository.getContentRows().collect { rows ->
        _uiState.update { it.copy(contentRows = rows) }
      }
    }

    viewModelScope.launch {
      repository.getComingSoon().collect { items ->
        _uiState.update { it.copy(comingSoonItems = items) }
      }
    }

    viewModelScope.launch {
      repository.getMyList().collect { items ->
        _uiState.update { it.copy(myListItems = items) }
      }
    }

    viewModelScope.launch {
      repository.getAnimeContent().collect { items ->
        _uiState.update { it.copy(animeItems = items, isLoadingAnime = false) }
      }
    }

    viewModelScope.launch {
      repository.getLiveTvChannelsFlow().collect { channels ->
        _uiState.update { it.copy(liveTvChannels = channels, isLoadingLiveTv = false) }
      }
    }

    _uiState.update {
      it.copy(
        providers = ottProviders,
        trendingKeywords = repository.getTrendingKeywords(),
        recentSearches = repository.getRecentSearches(),
        availableGenres = repository.getAllGenres(),
        watchHistory = repository.getWatchHistory()
      )
    }

    // Default initial search recommended picks
    viewModelScope.launch {
      val initial = repository.searchReal("a")
      if (initial.isNotEmpty()) {
        _uiState.update { it.copy(searchResults = initial) }
      }
    }
  }

  private fun startHeroAutoRotation() {
    viewModelScope.launch {
      while (true) {
        delay(6500)
        val currentItems = _uiState.value.featuredHeroItems
        if (currentItems.isNotEmpty() && _uiState.value.currentScreen == Screen.Home) {
          _uiState.update {
            it.copy(currentHeroIndex = (it.currentHeroIndex + 1) % currentItems.size)
          }
        }
      }
    }
  }

  fun selectProvider(providerId: String?) {
    val newId = if (_uiState.value.selectedProviderId == providerId) null else providerId
    _uiState.update { it.copy(selectedProviderId = newId) }
    val name = ottProviders.find { it.id == newId }?.name
    if (name != null) {
      showToast("Filtered by $name")
    } else {
      showToast("Showing All Streaming Hubs")
    }
  }

  fun setHeroIndex(index: Int) {
    if (index in 0 until _uiState.value.featuredHeroItems.size) {
      _uiState.update { it.copy(currentHeroIndex = index) }
    }
  }

  fun navigateTo(screen: Screen) {
    if (screen == _uiState.value.currentScreen) return
    screenBackStack.add(screen)
    _uiState.update { it.copy(currentScreen = screen) }
  }

  fun navigateBack(): Boolean {
    if (screenBackStack.size > 1) {
      screenBackStack.removeAt(screenBackStack.size - 1)
      val previousScreen = screenBackStack.last()
      _uiState.update { it.copy(currentScreen = previousScreen) }
      return true
    } else if (_uiState.value.currentScreen != Screen.Home) {
      screenBackStack.clear()
      screenBackStack.add(Screen.Home)
      _uiState.update { it.copy(currentScreen = Screen.Home) }
      return true
    }
    return false
  }

  // --- Real Movie / Series Detail Fetching ---

  fun openDetail(mediaItem: MediaItem) {
    _uiState.update {
      it.copy(
        selectedDetailMedia = mediaItem,
        isLoadingDetails = false
      )
    }
    navigateTo(Screen.Detail(mediaItem.id))

    viewModelScope.launch {
      val fullData = repository.getMediaDetails(mediaItem.id) ?: mediaItem
      _uiState.update {
        it.copy(
          selectedDetailMedia = fullData,
          isLoadingDetails = false
        )
      }
    }
  }

  fun openDetail(mediaId: String) {
    val cached = repository.getMediaById(mediaId)
    _uiState.update {
      it.copy(
        selectedDetailMedia = cached,
        isLoadingDetails = cached == null
      )
    }
    navigateTo(Screen.Detail(mediaId))

    viewModelScope.launch {
      val fullData = repository.getMediaDetails(mediaId) ?: cached
      if (fullData != null) {
        _uiState.update {
          it.copy(
            selectedDetailMedia = fullData,
            isLoadingDetails = false
          )
        }
      } else {
        _uiState.update { it.copy(isLoadingDetails = false) }
      }
    }
  }

  fun openPlayer(mediaId: String, episodeTitle: String? = null) {
    val media = repository.getMediaById(mediaId) ?: _uiState.value.selectedDetailMedia
    if (media != null) {
      preparePlayback(media, null, null, episodeTitle)
    }
  }

  fun openLiveTvPlayer(channel: LiveTvChannel) {
    val targetStreamUrl = channel.embedUrl ?: channel.watchUrl
    _uiState.update {
      it.copy(
        selectedLiveTvChannel = channel,
        playbackState = it.playbackState.copy(
          isPlaying = true,
          streamUrl = targetStreamUrl,
          serverName = "Live Feed (${channel.category})"
        )
      )
    }
    navigateTo(Screen.LivePlayer(channel.id))
  }

  fun playTeaser(title: String) {
    viewModelScope.launch {
      showToast("Searching official teaser...")
      val trailer = repository.getTrailer(title)
      if (trailer != null && trailer.videoId.isNotBlank()) {
        val embedUrl = "https://www.youtube.com/embed/${trailer.videoId}?autoplay=1"
        _uiState.update {
          it.copy(
            playbackState = it.playbackState.copy(
              isPlaying = true,
              streamUrl = embedUrl,
              serverName = "YouTube Official Teaser"
            )
          )
        }
        navigateTo(Screen.TrailerPlayer(title, trailer.videoId))
      } else {
        showToast("Teaser trailer is currently unavailable for this title.")
      }
    }
  }

  fun openDownloads(mediaItem: MediaItem, season: Int? = null, episode: Int? = null) {
    viewModelScope.launch {
      _uiState.update {
        it.copy(
          showDownloadsSheet = true,
          downloadTargetMedia = mediaItem,
          isLoadingDownloads = true,
          availableDownloads = null
        )
      }
      val type = if (mediaItem.mediaType == MediaType.SERIES) "tv" else "movie"
      val result = repository.getDownloads(mediaItem.id, type, season, episode)
      _uiState.update {
        it.copy(
          availableDownloads = result,
          isLoadingDownloads = false
        )
      }
    }
  }

  fun dismissDownloadsSheet() {
    _uiState.update { it.copy(showDownloadsSheet = false, downloadTargetMedia = null) }
  }

  fun loadSeasonEpisodes(seriesId: String, seasonNumber: Int) {
    viewModelScope.launch {
      val episodes = repository.getSeasonEpisodes(seriesId, seasonNumber)
      val currentMedia = _uiState.value.selectedDetailMedia
      if (currentMedia != null && currentMedia.id == seriesId) {
        val existingSeasons = currentMedia.seasons.toMutableList()
        val index = existingSeasons.indexOfFirst { it.seasonNumber == seasonNumber }
        val newSeason = Season(
          seasonNumber = seasonNumber,
          title = "Season $seasonNumber",
          episodes = episodes
        )
        if (index >= 0) {
          existingSeasons[index] = newSeason
        } else {
          existingSeasons.add(newSeason)
        }
        _uiState.update {
          it.copy(selectedDetailMedia = currentMedia.copy(seasons = existingSeasons))
        }
      }
    }
  }

  // --- Real Server Selection & Playback Resolve ---

  fun preparePlayback(mediaItem: MediaItem, season: Int? = null, episode: Int? = null, episodeTitle: String? = null) {
    viewModelScope.launch {
      _uiState.update {
        it.copy(
          pendingPlayMedia = mediaItem,
          pendingPlaySeason = season,
          pendingPlayEpisode = episode,
          pendingPlayEpisodeTitle = episodeTitle,
          isResolvingPlayback = true,
          showServerSelection = true
        )
      }

      val type = if (mediaItem.mediaType == MediaType.SERIES) "tv" else "movie"
      repository.recordWatchProgress(mediaItem, 0.25f)
      val servers = repository.getServers(mediaItem.id, type, season, episode)

      _uiState.update {
        it.copy(
          availableServers = servers,
          isResolvingPlayback = false
        )
      }
    }
  }

  fun selectServerAndPlay(server: ServerItem) {
    val media = _uiState.value.pendingPlayMedia ?: _uiState.value.selectedDetailMedia ?: return
    val season = _uiState.value.pendingPlaySeason
    val episode = _uiState.value.pendingPlayEpisode
    val episodeTitle = _uiState.value.pendingPlayEpisodeTitle

    viewModelScope.launch {
      _uiState.update { it.copy(isResolvingPlayback = true) }
      repository.recordWatchProgress(media, 0.35f)
      val type = if (media.mediaType == MediaType.SERIES) "tv" else "movie"
      val resolved: PlaybackResolved? = repository.resolvePlayback(
        contentId = media.id,
        type = type,
        serverId = server.id,
        season = season,
        episode = episode
      )

      val targetStreamUrl = resolved?.securePlayerUrl ?: resolved?.streamUrl ?: server.securePlayerUrl ?: server.url

      _uiState.update {
        it.copy(
          isResolvingPlayback = false,
          showServerSelection = false,
          playbackState = it.playbackState.copy(
            isPlaying = true,
            currentPositionSeconds = 0,
            totalDurationSeconds = if (media.mediaType == MediaType.MOVIE) 7800 else 3000,
            episodeTitle = episodeTitle,
            streamUrl = targetStreamUrl,
            securePlayerUrl = resolved?.securePlayerUrl ?: server.securePlayerUrl,
            serverName = resolved?.serverName ?: server.name,
            serverQuality = resolved?.quality ?: server.quality
          )
        )
      }

      navigateTo(Screen.Player(media.id, episodeTitle))
    }
  }

  fun dismissServerSelection() {
    _uiState.update {
      it.copy(
        showServerSelection = false,
        pendingPlayMedia = null,
        isResolvingPlayback = false
      )
    }
  }

  // --- Real Search with Debounce & Stale Request Cancellation ---

  fun onSearchQueryChanged(query: String) {
    _uiState.update { it.copy(searchQuery = query) }
    executeSearch(query, _uiState.value.selectedSearchType)
  }

  fun onSearchTypeSelected(typeKey: String) {
    _uiState.update { it.copy(selectedSearchType = typeKey) }
    executeSearch(_uiState.value.searchQuery, typeKey)
  }

  private fun executeSearch(query: String, typeKey: String) {
    searchDebounceJob?.cancel()

    val apiType = if (typeKey == "all") null else typeKey

    if (query.isBlank()) {
      viewModelScope.launch {
        val initial = repository.searchReal("a", apiType)
        _uiState.update { it.copy(searchResults = initial, isSearching = false) }
      }
      return
    }

    searchDebounceJob = viewModelScope.launch {
      delay(300) // 300ms debounce
      try {
        _uiState.update { it.copy(isSearching = true) }
        val results = repository.searchReal(query, apiType)
        _uiState.update {
          it.copy(
            searchResults = results,
            isSearching = false
          )
        }
      } catch (_: kotlinx.coroutines.CancellationException) {
        // Cancelled by typing newer query
      }
    }
  }

  fun clearSearch() {
    onSearchQueryChanged("")
  }

  fun toggleMyList(mediaItem: MediaItem) {
    val added = repository.toggleMyList(mediaItem)
    val message = if (added) "Added to Watchlist" else "Removed from Watchlist"
    showToast(message)
  }

  fun isMediaInMyList(id: String): Boolean {
    return repository.isInMyList(id)
  }

  fun removeFromContinueWatching(mediaId: String) {
    repository.removeFromContinueWatching(mediaId)
    _uiState.update { state ->
      val updated = state.continueWatchingItems.filter { it.id != mediaId }
      val updatedHistory = state.watchHistory.filter { it.id != mediaId }
      state.copy(
        continueWatchingItems = updated,
        watchHistory = updatedHistory
      )
    }
    showToast("Removed from Continue Watching")
  }

  fun showToast(message: String) {
    _uiState.update { it.copy(toastMessage = message) }
    viewModelScope.launch {
      delay(2400)
      if (_uiState.value.toastMessage == message) {
        _uiState.update { it.copy(toastMessage = null) }
      }
    }
  }

  fun dismissToast() {
    _uiState.update { it.copy(toastMessage = null) }
  }
}
