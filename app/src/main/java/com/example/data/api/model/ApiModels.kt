package com.example.data.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BaseApiResponse<T>(
  @Json(name = "success") val success: Boolean = false,
  @Json(name = "data") val data: T? = null,
  @Json(name = "message") val message: String? = null,
  @Json(name = "timestamp") val timestamp: Long? = null
)

@JsonClass(generateAdapter = true)
data class CastMemberDto(
  @Json(name = "id") val id: Any? = null,
  @Json(name = "name") val name: String? = null,
  @Json(name = "character") val character: String? = null,
  @Json(name = "profileImage") val profileImage: String? = null
)

@JsonClass(generateAdapter = true)
data class SeasonSummaryDto(
  @Json(name = "id") val id: Any? = null,
  @Json(name = "seasonNumber") val seasonNumber: Int = 1,
  @Json(name = "name") val name: String? = null,
  @Json(name = "overview") val overview: String? = null,
  @Json(name = "poster") val poster: String? = null,
  @Json(name = "episodeCount") val episodeCount: Int? = null
)

@JsonClass(generateAdapter = true)
data class MediaDto(
  @Json(name = "id") val id: String,
  @Json(name = "tmdbId") val tmdbId: Any? = null,
  @Json(name = "imdbId") val imdbId: String? = null,
  @Json(name = "type") val type: String? = "movie",
  @Json(name = "title") val title: String? = "",
  @Json(name = "originalTitle") val originalTitle: String? = null,
  @Json(name = "overview") val overview: String? = "",
  @Json(name = "poster") val poster: String? = null,
  @Json(name = "backdrop") val backdrop: String? = null,
  @Json(name = "logo") val logo: String? = null,
  @Json(name = "releaseDate") val releaseDate: String? = null,
  @Json(name = "year") val year: Any? = null,
  @Json(name = "rating") val rating: String? = null,
  @Json(name = "voteAverage") val voteAverage: Double? = null,
  @Json(name = "genres") val genres: List<String>? = emptyList(),
  @Json(name = "duration") val duration: String? = null,
  @Json(name = "runtime") val runtime: Any? = null,
  @Json(name = "language") val language: String? = null,
  @Json(name = "director") val director: String? = null,
  @Json(name = "cast") val cast: List<CastMemberDto>? = emptyList(),
  @Json(name = "isSeries") val isSeries: Boolean? = false,
  @Json(name = "releaseStatus") val releaseStatus: String? = "RELEASED",
  @Json(name = "isUpcoming") val isUpcoming: Boolean? = false,
  @Json(name = "platform") val platform: String? = "netflix",
  @Json(name = "category") val category: String? = null,
  @Json(name = "serversCount") val serversCount: Int? = null,
  @Json(name = "available") val available: Boolean? = true,
  @Json(name = "seasons") val seasons: List<SeasonSummaryDto>? = emptyList(),
  @Json(name = "related") val related: List<MediaDto>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class SectionDto(
  @Json(name = "id") val id: String,
  @Json(name = "title") val title: String,
  @Json(name = "items") val items: List<MediaDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PlatformDto(
  @Json(name = "id") val id: String,
  @Json(name = "name") val name: String,
  @Json(name = "logo") val logo: String? = null
)

@JsonClass(generateAdapter = true)
data class GenreDto(
  @Json(name = "id") val id: String? = null,
  @Json(name = "name") val name: String? = null
)

@JsonClass(generateAdapter = true)
data class CategoryDto(
  @Json(name = "id") val id: String? = null,
  @Json(name = "name") val name: String? = null
)

@JsonClass(generateAdapter = true)
data class HomeDataDto(
  @Json(name = "hero") val hero: List<MediaDto> = emptyList(),
  @Json(name = "trending") val trending: List<MediaDto> = emptyList(),
  @Json(name = "popularMovies") val popularMovies: List<MediaDto> = emptyList(),
  @Json(name = "popularSeries") val popularSeries: List<MediaDto> = emptyList(),
  @Json(name = "topRated") val topRated: List<MediaDto> = emptyList(),
  @Json(name = "newReleases") val newReleases: List<MediaDto> = emptyList(),
  @Json(name = "comingSoon") val comingSoon: List<MediaDto> = emptyList(),
  @Json(name = "categories") val categories: List<CategoryDto> = emptyList(),
  @Json(name = "genres") val genres: List<GenreDto> = emptyList(),
  @Json(name = "platforms") val platforms: List<PlatformDto> = emptyList(),
  @Json(name = "sections") val sections: List<SectionDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class EpisodeDto(
  @Json(name = "id") val id: Any? = null,
  @Json(name = "episodeNumber") val episodeNumber: Int = 1,
  @Json(name = "seasonNumber") val seasonNumber: Int = 1,
  @Json(name = "name") val name: String = "Episode",
  @Json(name = "overview") val overview: String? = "",
  @Json(name = "thumbnail") val thumbnail: String? = null,
  @Json(name = "runtime") val runtime: Any? = null,
  @Json(name = "rating") val rating: Any? = null,
  @Json(name = "airDate") val airDate: String? = null,
  @Json(name = "airedDate") val airedDate: String? = null
)

@JsonClass(generateAdapter = true)
data class SeasonEpisodesDto(
  @Json(name = "seriesId") val seriesId: String? = null,
  @Json(name = "seasonNumber") val seasonNumber: Int = 1,
  @Json(name = "name") val name: String = "Season",
  @Json(name = "episodeCount") val episodeCount: Int = 0,
  @Json(name = "episodes") val episodes: List<EpisodeDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ServerItemDto(
  @Json(name = "id") val id: String,
  @Json(name = "name") val name: String,
  @Json(name = "type") val type: String? = "embed",
  @Json(name = "quality") val quality: String? = "1080p",
  @Json(name = "language") val language: String? = "Multi-Audio",
  @Json(name = "url") val url: String? = null,
  @Json(name = "securePlayerUrl") val securePlayerUrl: String? = null,
  @Json(name = "available") val available: Boolean = true,
  @Json(name = "priority") val priority: Int = 1
)

@JsonClass(generateAdapter = true)
data class ServersResponseDto(
  @Json(name = "contentId") val contentId: String? = null,
  @Json(name = "seriesId") val seriesId: String? = null,
  @Json(name = "type") val type: String? = null,
  @Json(name = "totalServers") val totalServers: Int = 0,
  @Json(name = "servers") val servers: List<ServerItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PlaybackResolveRequest(
  @Json(name = "contentId") val contentId: String,
  @Json(name = "type") val type: String, // "movie" or "tv"
  @Json(name = "serverId") val serverId: String,
  @Json(name = "season") val season: Int? = null,
  @Json(name = "episode") val episode: Int? = null
)

@JsonClass(generateAdapter = true)
data class PlaybackResolvedDataDto(
  @Json(name = "contentId") val contentId: String,
  @Json(name = "type") val type: String? = "movie",
  @Json(name = "serverId") val serverId: String,
  @Json(name = "serverName") val serverName: String? = null,
  @Json(name = "embedUrl") val embedUrl: String? = null,
  @Json(name = "streamUrl") val streamUrl: String? = null,
  @Json(name = "securePlayerUrl") val securePlayerUrl: String? = null,
  @Json(name = "quality") val quality: String? = "1080p",
  @Json(name = "language") val language: String? = null,
  @Json(name = "season") val season: Int? = null,
  @Json(name = "episode") val episode: Int? = null,
  @Json(name = "available") val available: Boolean = true
)

@JsonClass(generateAdapter = true)
data class LiveTvChannelDto(
  @Json(name = "id") val id: String,
  @Json(name = "name") val name: String,
  @Json(name = "category") val category: String? = null,
  @Json(name = "logo") val logo: String? = null,
  @Json(name = "embed") val embed: String? = null,
  @Json(name = "watch") val watch: String? = null
)

@JsonClass(generateAdapter = true)
data class LiveTvDataDto(
  @Json(name = "count") val count: Int? = 0,
  @Json(name = "channels") val channels: List<LiveTvChannelDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class DownloadOptionDto(
  @Json(name = "name") val name: String? = null,
  @Json(name = "quality") val quality: String? = null,
  @Json(name = "size") val size: String? = null,
  @Json(name = "download_url") val downloadUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class DownloadsDataDto(
  @Json(name = "success") val success: Boolean = false,
  @Json(name = "type") val type: String? = null,
  @Json(name = "tmdb_id") val tmdbId: Any? = null,
  @Json(name = "downloads") val downloads: List<DownloadOptionDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TrailerDataDto(
  @Json(name = "query") val query: String? = null,
  @Json(name = "videoId") val videoId: String? = null
)

