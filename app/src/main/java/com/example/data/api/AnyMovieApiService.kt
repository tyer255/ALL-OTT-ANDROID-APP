package com.example.data.api

import com.example.data.api.model.BaseApiResponse
import com.example.data.api.model.HomeDataDto
import com.example.data.api.model.MediaDto
import com.example.data.api.model.PlaybackResolveRequest
import com.example.data.api.model.PlaybackResolvedDataDto
import com.example.data.api.model.SeasonEpisodesDto
import com.example.data.api.model.ServersResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AnyMovieApiService {

  @GET("health")
  suspend fun getHealth(): Map<String, Any>

  @GET("home")
  suspend fun getHome(
    @Query("limit") limit: Int = 20
  ): BaseApiResponse<HomeDataDto>

  @GET("search")
  suspend fun search(
    @Query("q") query: String,
    @Query("limit") limit: Int = 30,
    @Query("type") type: String? = null
  ): BaseApiResponse<List<MediaDto>>

  @GET("movie/{id}")
  suspend fun getMovie(
    @Path("id") id: String
  ): BaseApiResponse<MediaDto>

  @GET("series/{id}")
  suspend fun getSeries(
    @Path("id") id: String
  ): BaseApiResponse<MediaDto>

  @GET("series/{id}/season/{season}/episodes")
  suspend fun getSeasonEpisodes(
    @Path("id") id: String,
    @Path("season") season: Int
  ): BaseApiResponse<SeasonEpisodesDto>

  @GET("movie/{id}/servers")
  suspend fun getMovieServers(
    @Path("id") id: String
  ): BaseApiResponse<ServersResponseDto>

  @GET("series/{id}/season/{season}/episode/{episode}/servers")
  suspend fun getSeriesEpisodeServers(
    @Path("id") id: String,
    @Path("season") season: Int,
    @Path("episode") episode: Int
  ): BaseApiResponse<ServersResponseDto>

  @POST("playback/resolve")
  suspend fun resolvePlayback(
    @Body request: PlaybackResolveRequest
  ): BaseApiResponse<PlaybackResolvedDataDto>

  @GET("livetv/channels")
  suspend fun getLiveTvChannels(): BaseApiResponse<com.example.data.api.model.LiveTvDataDto>

  @GET("downloads")
  suspend fun getDownloads(
    @Query("id") id: String,
    @Query("type") type: String,
    @Query("season") season: Int? = null,
    @Query("episode") episode: Int? = null
  ): BaseApiResponse<com.example.data.api.model.DownloadsDataDto>

  @GET("trailer")
  suspend fun getTrailer(
    @Query("q") query: String
  ): BaseApiResponse<com.example.data.api.model.TrailerDataDto>

  @GET("discover")
  suspend fun discover(
    @Query("platform") platform: String? = null,
    @Query("category") category: String? = null,
    @Query("genre") genre: String? = null,
    @Query("status") status: String? = null,
    @Query("limit") limit: Int = 30
  ): BaseApiResponse<List<MediaDto>>
}
