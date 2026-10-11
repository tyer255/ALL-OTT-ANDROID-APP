package com.example.data.model

enum class MediaType(val label: String) {
  MOVIE("Movie"),
  SERIES("Series")
}

data class CastMember(
  val id: String,
  val name: String,
  val character: String,
  val profileImageUrl: String
)

data class Episode(
  val id: String,
  val seasonNumber: Int,
  val episodeNumber: Int,
  val title: String,
  val overview: String,
  val duration: String,
  val thumbnailUrl: String,
  val rating: Double = 8.4,
  val airedDate: String = "2024-01-15"
)

data class Season(
  val seasonNumber: Int,
  val title: String,
  val episodes: List<Episode>
)

data class MediaItem(
  val id: String,
  val tmdbId: Int? = null,
  val title: String,
  val tagline: String = "",
  val overview: String,
  val backdropUrl: String,
  val posterUrl: String,
  val mediaType: MediaType,
  val rating: Double,
  val releaseYear: Int,
  val duration: String, // e.g. "2h 45m" or "3 Seasons"
  val genres: List<String>,
  val maturityRating: String = "PG-13",
  val audioFormats: String = "FHD 1080p",
  val isTrending: Boolean = false,
  val isTopRated: Boolean = false,
  val isComingSoon: Boolean = false,
  val releaseDate: String? = null,
  val progressPercent: Float? = null, // for continue watching (0.0 to 1.0)
  val lastWatchedEpisode: String? = null,
  val cast: List<CastMember> = emptyList(),
  val seasons: List<Season> = emptyList(),
  val director: String = "Christopher Nolan",
  val trailerTitle: String = "Official Teaser Trailer",
  val streamingPlatform: String = "Netflix"
)

data class ContentRowSection(
  val id: String,
  val title: String,
  val subtitle: String? = null,
  val items: List<MediaItem>
)

data class ServerItem(
  val id: String,
  val name: String,
  val type: String = "embed",
  val quality: String = "1080p",
  val language: String = "Multi-Audio",
  val url: String? = null,
  val securePlayerUrl: String? = null,
  val available: Boolean = true,
  val priority: Int = 1
)

data class PlaybackResolved(
  val contentId: String,
  val type: String = "movie",
  val serverId: String,
  val serverName: String? = null,
  val streamUrl: String? = null,
  val securePlayerUrl: String? = null,
  val quality: String = "1080p",
  val language: String? = null,
  val season: Int? = null,
  val episode: Int? = null,
  val available: Boolean = true
)

data class LiveTvChannel(
  val id: String,
  val name: String,
  val category: String,
  val logoUrl: String,
  val embedUrl: String?,
  val watchUrl: String?,
  val isLive: Boolean = true,
  val isHd: Boolean = true
)

data class DownloadOption(
  val name: String,
  val quality: String,
  val size: String,
  val downloadUrl: String
)

data class DownloadsInfo(
  val type: String,
  val tmdbId: String?,
  val downloads: List<DownloadOption>
)

data class TrailerInfo(
  val query: String,
  val videoId: String
)

