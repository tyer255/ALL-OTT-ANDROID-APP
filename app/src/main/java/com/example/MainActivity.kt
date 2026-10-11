package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AetherBottomNavigation
import com.example.ui.components.AetherToastBanner
import com.example.ui.components.DownloadsBottomSheet
import com.example.ui.components.ServerSelectionBottomSheet
import com.example.ui.screens.AnimeScreen
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveTvScreen
import com.example.ui.screens.MyListScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.AetherStreamTheme
import com.example.ui.theme.NetflixBlack
import com.example.ui.viewmodel.AetherViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      AetherStreamTheme {
        AetherStreamApp()
      }
    }
  }
}

@Composable
fun AetherStreamApp(
  viewModel: AetherViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  BackHandler(enabled = uiState.currentScreen != Screen.Home) {
    viewModel.navigateBack()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(NetflixBlack)
  ) {
    when (val screen = uiState.currentScreen) {
      is Screen.Home -> {
        HomeScreen(
          uiState = uiState,
          onHeroIndexSelect = { viewModel.setHeroIndex(it) },
          onMediaClick = { viewModel.openDetail(it) },
          onPlayClick = { viewModel.openPlayer(it.id) },
          onToggleMyList = { viewModel.toggleMyList(it) },
          onSearchClick = { viewModel.navigateTo(Screen.Search) },
          onProfileClick = { viewModel.navigateTo(Screen.Profile) },
          onSelectProvider = { viewModel.selectProvider(it) },
          onRemoveFromContinueWatching = { viewModel.removeFromContinueWatching(it) }
        )
      }

      is Screen.Anime -> {
        AnimeScreen(
          animeItems = uiState.animeItems,
          isLoading = uiState.isLoadingAnime,
          onMediaClick = { viewModel.openDetail(it) },
          onPlayClick = { viewModel.openPlayer(it.id) },
          onToggleMyList = { viewModel.toggleMyList(it) },
          isBookmarked = { viewModel.isMediaInMyList(it) }
        )
      }

      is Screen.LiveTv -> {
        LiveTvScreen(
          channels = uiState.liveTvChannels,
          isLoading = uiState.isLoadingLiveTv,
          onChannelClick = { viewModel.openLiveTvPlayer(it) },
          onRetry = { viewModel.retryLoadLiveTv() }
        )
      }

      is Screen.Search -> {
        SearchScreen(
          uiState = uiState,
          onQueryChange = { viewModel.onSearchQueryChanged(it) },
          onTypeFilterSelect = { viewModel.onSearchTypeSelected(it) },
          onClearQuery = { viewModel.clearSearch() },
          onMediaClick = { viewModel.openDetail(it) },
          onToggleMyList = { viewModel.toggleMyList(it) },
          isBookmarked = { viewModel.isMediaInMyList(it) }
        )
      }

      is Screen.MyList -> {
        MyListScreen(
          items = uiState.myListItems,
          onBackClick = { viewModel.navigateBack() },
          onMediaClick = { viewModel.openDetail(it) }
        )
      }

      is Screen.Profile -> {
        ProfileScreen(
          uiState = uiState,
          onMediaClick = { viewModel.openDetail(it) },
          onPlayClick = { viewModel.openPlayer(it.id) },
          onOpenMyList = { viewModel.navigateTo(Screen.MyList) },
          onBackClick = { viewModel.navigateBack() },
          onRemoveFromContinueWatching = { viewModel.removeFromContinueWatching(it) }
        )
      }

      is Screen.Detail -> {
        val detailId = screen.mediaId
        val selectedMedia = uiState.selectedDetailMedia?.takeIf { it.id == detailId }
          ?: com.example.data.util.CuratedAnimeCatalog.items.firstOrNull { it.id == detailId }
          ?: uiState.searchResults.firstOrNull { it.id == detailId }
          ?: uiState.animeItems.firstOrNull { it.id == detailId }
          ?: uiState.continueWatchingItems.firstOrNull { it.id == detailId }
          ?: uiState.myListItems.firstOrNull { it.id == detailId }
          ?: uiState.contentRows.flatMap { it.items }.firstOrNull { it.id == detailId }
          ?: uiState.featuredHeroItems.firstOrNull { it.id == detailId }
          ?: uiState.selectedDetailMedia
        if (selectedMedia != null) {
          val similar = uiState.searchResults.filter { it.id != selectedMedia.id }.take(6)
          DetailScreen(
            mediaItem = selectedMedia,
            isInMyList = viewModel.isMediaInMyList(selectedMedia.id),
            similarItems = similar,
            onBackClick = { viewModel.navigateBack() },
            onPlayClick = { media, episodeTitle -> viewModel.preparePlayback(media, null, null, episodeTitle) },
            onPlayTeaserClick = { title -> viewModel.playTeaser(title) },
            onDownloadClick = { media -> viewModel.openDownloads(media) },
            onPlayEpisodeClick = { media, season, epNum, epTitle -> viewModel.preparePlayback(media, season, epNum, epTitle) },
            onSeasonChange = { seriesId, seasonNum -> viewModel.loadSeasonEpisodes(seriesId, seasonNum) },
            onToggleMyList = { viewModel.toggleMyList(it) },
            onSimilarClick = { viewModel.openDetail(it.id) },
            isLoading = uiState.isLoadingDetails
          )
        }
      }

      is Screen.Player -> {
        val media = uiState.selectedDetailMedia ?: uiState.featuredHeroItems.firstOrNull()
        PlayerScreen(
          mediaTitle = media?.title ?: "Stream Player",
          subtitle = uiState.playbackState.episodeTitle ?: media?.duration,
          streamUrl = uiState.playbackState.streamUrl,
          serverName = uiState.playbackState.serverName,
          onBackClick = { viewModel.navigateBack() },
          onRetryOrSwitchServer = if (media != null) { { viewModel.preparePlayback(media) } } else null
        )
      }

      is Screen.LivePlayer -> {
        val channel = uiState.selectedLiveTvChannel
        PlayerScreen(
          mediaTitle = channel?.name ?: "Live Channel",
          subtitle = "Live • ${channel?.category ?: "Broadcast"}",
          streamUrl = channel?.embedUrl ?: channel?.watchUrl,
          serverName = "Live Feed",
          onBackClick = { viewModel.navigateBack() }
        )
      }

      is Screen.TrailerPlayer -> {
        PlayerScreen(
          mediaTitle = screen.title,
          subtitle = "Official Teaser Trailer",
          streamUrl = "https://www.youtube.com/embed/${screen.videoId}?autoplay=1",
          serverName = "YouTube Embed",
          onBackClick = { viewModel.navigateBack() }
        )
      }
    }

    val showBottomNav = uiState.currentScreen is Screen.Home ||
        uiState.currentScreen is Screen.Anime ||
        uiState.currentScreen is Screen.LiveTv ||
        uiState.currentScreen is Screen.Search

    if (showBottomNav) {
      AetherBottomNavigation(
        currentScreen = uiState.currentScreen,
        onNavigate = { viewModel.navigateTo(it) },
        modifier = Modifier.align(Alignment.BottomCenter)
      )
    }

    AetherToastBanner(
      message = uiState.toastMessage,
      onDismiss = { viewModel.dismissToast() },
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .navigationBarsPadding()
        .padding(bottom = if (showBottomNav) 80.dp else 20.dp)
    )

    if (uiState.showServerSelection && uiState.pendingPlayMedia != null) {
      ServerSelectionBottomSheet(
        mediaItem = uiState.pendingPlayMedia!!,
        servers = uiState.availableServers,
        isLoading = uiState.isResolvingPlayback && uiState.availableServers.isEmpty(),
        isResolving = uiState.isResolvingPlayback && uiState.availableServers.isNotEmpty(),
        episodeTitle = uiState.pendingPlayEpisodeTitle,
        onSelectServer = { viewModel.selectServerAndPlay(it) },
        onDismiss = { viewModel.dismissServerSelection() },
        onRetry = { viewModel.preparePlayback(uiState.pendingPlayMedia!!) }
      )
    }

    if (uiState.showDownloadsSheet && uiState.downloadTargetMedia != null) {
      DownloadsBottomSheet(
        mediaItem = uiState.downloadTargetMedia!!,
        downloadsInfo = uiState.availableDownloads,
        isLoading = uiState.isLoadingDownloads,
        onDismiss = { viewModel.dismissDownloadsSheet() }
      )
    }
  }
}
