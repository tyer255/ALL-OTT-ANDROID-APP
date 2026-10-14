package com.example.data.repository

import com.example.data.model.CastMember
import com.example.data.model.ContentRowSection
import com.example.data.model.Episode
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.Season
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockMediaRepository : MediaRepository {

  // Cast members
  private val castChristianBale = CastMember("c1", "Marcus Vance", "Commander Thorne", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300")
  private val castZendaya = CastMember("c2", "Elena Rostova", "Dr. Lyra Chen", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300")
  private val castIdris = CastMember("c3", "Kaelen Voss", "General Ward", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300")
  private val castFlorence = CastMember("c4", "Aria Sterling", "Maya Sterling", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=300")
  private val castOscar = CastMember("c5", "Devan Patel", "Kabir Roy", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=300")
  private val castDeepika = CastMember("c6", "Priya Sharma", "Inspector Tara", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=300")
  private val castShahRukh = CastMember("c7", "Vikram Rathore", "Special Forces Commander", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=300")
  private val castAnimeTanjiro = CastMember("c8", "Tanjiro Kamado", "Demon Slayer Corps", "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=300")

  // Episodes for Series
  private val keplerEpisodesS1 = listOf(
    Episode("ep_k1", 1, 1, "The Signal from Orbit", "A deep-space surveyor station intercepts an anomalous broadcast originating from Kepler-186f.", "54m", "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600"),
    Episode("ep_k2", 1, 2, "Atmospheric Entry", "Commander Thorne leads the vanguard descent through the ionic tempest, losing orbital comms.", "48m", "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=600"),
    Episode("ep_k3", 1, 3, "Monolith Sanctuary", "Inside a subterranean biosphere, the expedition discovers an ancient biological archive.", "52m", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600")
  )

  private val mumbaiSyndicateEpisodes = listOf(
    Episode("ms_1", 1, 1, "The Docks of Ballard", "Kabir assumes control of the southern freight docks after a sudden ambush.", "52m", "https://images.unsplash.com/photo-1524492412937-b28074a5d7da?w=600"),
    Episode("ms_2", 1, 2, "Blood Currency", "An honest crime branch inspector tightens maritime surveillance routes.", "49m", "https://images.unsplash.com/photo-1566438480900-0609be27a4be?w=600"),
    Episode("ms_3", 1, 3, "The High Table", "Kabir meets the ministers behind the coastal real estate conglomerate.", "56m", "https://images.unsplash.com/photo-1514565131-fce0801e5785?w=600")
  )

  private val soloLevelingEpisodes = listOf(
    Episode("sl_1", 1, 1, "I'm Used to It", "A low-rank hunter enters a mysterious double dungeon that challenges all established laws.", "24m", "https://images.unsplash.com/photo-1563089145-599997674d42?w=600"),
    Episode("sl_2", 1, 2, "If I Had One More Chance", "Trapped in the temple of trials, Jinwoo awakens an enigmatic personal leveling interface.", "24m", "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=600"),
    Episode("sl_3", 1, 3, "It's Like a Game", "Starting from level 1, Jinwoo trains relentlessly in daily survival instances.", "24m", "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600")
  )

  // Curated Catalog
  private val allMedia: List<MediaItem> = listOf(
    // 1. Kepler Series (Hero / Trending / Web Series)
    MediaItem(
      id = "m1",
      title = "Chronicles of Kepler",
      tagline = "Beyond our solar boundaries lies humanity's second dawn.",
      overview = "In the year 2184, a rogue expedition uncovers an engineered alien megastructure in the Kepler constellation that holds the answers to humanity's survival.",
      backdropUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=1200",
      posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600",
      mediaType = MediaType.SERIES,
      rating = 9.4,
      releaseYear = 2024,
      duration = "2 Seasons",
      genres = listOf("Sci-Fi", "Mystery", "Web Series"),
      maturityRating = "TV-MA",
      audioFormats = "4K Ultra HD • Dolby Vision • Dolby Atmos",
      isTrending = true,
      isTopRated = true,
      progressPercent = 0.65f,
      cast = listOf(castChristianBale, castZendaya, castIdris),
      seasons = listOf(Season(1, "Season 1", keplerEpisodesS1))
    ),

    // 2. Vanguard Iron Horizon (Blockbuster Cinema)
    MediaItem(
      id = "m2",
      title = "Vanguard: Iron Horizon",
      tagline = "The line between allegiance and betrayal has collapsed.",
      overview = "When an elite tactical unit is disavowed following an intercept in Tokyo, operative Jack Mercer must navigate global syndicates to prevent a sub-orbital EMP strike.",
      backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200",
      posterUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=600",
      mediaType = MediaType.MOVIE,
      rating = 8.8,
      releaseYear = 2024,
      duration = "2h 28m",
      genres = listOf("Action", "Thriller", "Blockbuster"),
      maturityRating = "PG-13",
      audioFormats = "4K Ultra HD • IMAX Enhanced • 5.1 Surround",
      isTrending = true,
      cast = listOf(castIdris, castFlorence, castChristianBale)
    ),

    // 3. The Mumbai Syndicate (Bollywood & Indian Cinema / Top Web Series)
    MediaItem(
      id = "m3",
      title = "The Mumbai Syndicate",
      tagline = "Power does not negotiate. It rules.",
      overview = "A gritty criminal dynasty saga chronicling the ruthless ascension of Kabir Roy across the high-stakes maritime territories and shadowy boardroom corridors of Mumbai.",
      backdropUrl = "https://images.unsplash.com/photo-1524492412937-b28074a5d7da?w=1200",
      posterUrl = "https://images.unsplash.com/photo-1566438480900-0609be27a4be?w=600",
      mediaType = MediaType.SERIES,
      rating = 9.1,
      releaseYear = 2024,
      duration = "3 Seasons",
      genres = listOf("Bollywood", "Indian Cinema", "Crime", "Web Series"),
      maturityRating = "18+",
      audioFormats = "4K UHD • Dolby Atmos • Hindi 5.1",
      isTrending = true,
      isTopRated = true,
      cast = listOf(castOscar, castDeepika, castShahRukh),
      seasons = listOf(Season(1, "Season 1", mumbaiSyndicateEpisodes))
    ),

    // 4. Singularity 2099 (Global Hits / Sci-Fi)
    MediaItem(
      id = "m4",
      title = "Singularity 2099",
      tagline = "Consciousness was only the beginning.",
      overview = "In a flooded neon-drenched metropolis, an artificial intelligence forensic investigator discovers memory fragments indicating her creator did not die naturally.",
      backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200",
      posterUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=600",
      mediaType = MediaType.MOVIE,
      rating = 8.7,
      releaseYear = 2024,
      duration = "1h 56m",
      genres = listOf("Sci-Fi", "Cyberpunk", "Global Hits"),
      maturityRating = "R",
      audioFormats = "4K Ultra HD • Spatial Audio",
      isTrending = true,
      cast = listOf(castZendaya, castFlorence)
    ),

    // 5. Solo Leveling: Arise (Anime Hits Crunchyroll Specials)
    MediaItem(
      id = "a1",
      title = "Solo Leveling: Shadow Monarch",
      tagline = "From the weakest hunter to the ultimate sovereign.",
      overview = "Sung Jinwoo, an E-rank hunter known as mankind's weakest, awakens an enigmatic interface during a dual-dungeon expedition granting limitless ascension.",
      backdropUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=1200",
      posterUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=600",
      mediaType = MediaType.SERIES,
      rating = 9.5,
      releaseYear = 2024,
      duration = "2 Seasons",
      genres = listOf("Anime", "Fantasy", "Action", "Crunchyroll"),
      maturityRating = "16+",
      audioFormats = "4K HDR • Japanese / Hindi Dub • 5.1",
      isTrending = true,
      isTopRated = true,
      cast = listOf(castAnimeTanjiro),
      seasons = listOf(Season(1, "Season 1", soloLevelingEpisodes))
    ),

    // 6. Jujutsu Kaisen: Shibuya Incident (Anime Hits Crunchyroll Specials)
    MediaItem(
      id = "a2",
      title = "Jujutsu Kaisen: Shibuya Arc",
      tagline = "Curse energy ignites in the heart of Tokyo.",
      overview = "On Halloween night in Shibuya, an unprecedented barrier descends upon Tokyo as special grade curses execute a meticulous conspiracy to seal Satoru Gojo.",
      backdropUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1200",
      posterUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=600",
      mediaType = MediaType.SERIES,
      rating = 9.3,
      releaseYear = 2024,
      duration = "2 Seasons",
      genres = listOf("Anime", "Action", "Supernatural", "Crunchyroll"),
      maturityRating = "18+",
      audioFormats = "4K UHD • Japanese / English / Hindi",
      isTrending = true,
      cast = listOf(castAnimeTanjiro)
    ),

    // 7. Demon Slayer: Hashira Training (Anime Hits)
    MediaItem(
      id = "a3",
      title = "Demon Slayer: Hashira Training",
      tagline = "Master the breathing techniques for the final fortress.",
      overview = "In preparation for the impending confrontation with Muzan Kibutsuji, the Demon Slayer Corps initiates rigorous master sessions overseen by the nine Hashiras.",
      backdropUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=1200",
      posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600",
      mediaType = MediaType.SERIES,
      rating = 9.2,
      releaseYear = 2024,
      duration = "Season 4",
      genres = listOf("Anime", "Action", "Shonen", "Crunchyroll"),
      maturityRating = "16+",
      audioFormats = "1080p Full HD • Dolby Atmos",
      isTrending = true
    ),

    // 8. Ravan: Rise of the Asura (Bollywood & Indian Cinema)
    MediaItem(
      id = "b1",
      title = "Ravan: Rise of the Asura",
      tagline = "Mythology reimagined with groundbreaking visual splendour.",
      overview = "A breathtaking mythological action spectacle depicting the untold genesis of Lanka's greatest scholar king, armed with celestial boons and fierce convictions.",
      backdropUrl = "https://images.unsplash.com/photo-1524492412937-b28074a5d7da?w=1200",
      posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600",
      mediaType = MediaType.MOVIE,
      rating = 9.0,
      releaseYear = 2024,
      duration = "2h 45m",
      genres = listOf("Bollywood", "Indian Cinema", "Action", "Mythology"),
      maturityRating = "UA 16+",
      audioFormats = "4K IMAX Enhanced • Dolby Atmos • Hindi / Tamil / Telugu",
      isTrending = true,
      cast = listOf(castShahRukh, castDeepika)
    ),

    // 9. Varanasi Undercover (Bollywood & Indian Cinema / Top Web Series)
    MediaItem(
      id = "b2",
      title = "Varanasi Undercover",
      tagline = "The holy ghats harbor unholy conspiracies.",
      overview = "A relentless RAW operative infiltrates an international antique smuggling network operating along the sacred ghats and labyrinthine alleys of Varanasi.",
      backdropUrl = "https://images.unsplash.com/photo-1566438480900-0609be27a4be?w=1200",
      posterUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=600",
      mediaType = MediaType.SERIES,
      rating = 8.9,
      releaseYear = 2024,
      duration = "2 Seasons",
      genres = listOf("Bollywood", "Indian Cinema", "Espionage", "Web Series"),
      maturityRating = "18+",
      audioFormats = "4K UHD • Dolby Digital 5.1",
      isTrending = true,
      cast = listOf(castOscar, castDeepika)
    ),

    // 10. Dune: Imperial Legacy (Blockbuster Cinema & Movies / Global Hits)
    MediaItem(
      id = "g1",
      title = "Dune: Imperial Legacy",
      tagline = "Control the spice, control the universe.",
      overview = "Paul Atreides unites with Chani and the Fremen while seeking vengeance against the conspirators who devastated his lineage across Arrakis.",
      backdropUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=1200",
      posterUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=600",
      mediaType = MediaType.MOVIE,
      rating = 9.1,
      releaseYear = 2024,
      duration = "2h 46m",
      genres = listOf("Blockbuster", "Sci-Fi", "Global Hits"),
      maturityRating = "PG-13",
      audioFormats = "4K IMAX • Dolby Atmos",
      isTrending = true,
      cast = listOf(castZendaya, castFlorence)
    ),

    // 11. Oppenheimer: Quantum Dawn (Blockbuster Cinema & Movies / Global Hits)
    MediaItem(
      id = "g2",
      title = "Quantum Dawn: The Trinity Project",
      tagline = "The moment the world changed forever.",
      overview = "A masterclass historical drama portraying the race against time at Los Alamos and the profound moral consequences of atomic warfare.",
      backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200",
      posterUrl = "https://images.unsplash.com/photo-1447433589675-4aaa569f3e05?w=600",
      mediaType = MediaType.MOVIE,
      rating = 9.3,
      releaseYear = 2023,
      duration = "3h 00m",
      genres = listOf("Blockbuster", "Biography", "Global Hits"),
      maturityRating = "R",
      audioFormats = "4K IMAX Enhanced • 7.1 Surround",
      isTrending = true,
      cast = listOf(castChristianBale, castFlorence)
    ),

    // 12. Crown & Shadows (Top Web Series & Originals)
    MediaItem(
      id = "w1",
      title = "Crown & Shadows",
      tagline = "In the game of empires, silence is the deadliest weapon.",
      overview = "Inside the velvet-walled halls of monarchical diplomacy, an espionage network uncovers a secret covenant designed to redraw modern European borders.",
      backdropUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1200",
      posterUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=600",
      mediaType = MediaType.SERIES,
      rating = 8.8,
      releaseYear = 2024,
      duration = "4 Seasons",
      genres = listOf("Web Series", "Drama", "Political"),
      maturityRating = "TV-MA",
      audioFormats = "4K UHD • Dolby Vision",
      isTrending = true
    ),

    // 13. Delhi Cyber Cell (Bollywood & Indian Cinema / Top Web Series)
    MediaItem(
      id = "b3",
      title = "Delhi Cyber Cell",
      tagline = "One keystroke to bring down a nation.",
      overview = "A brilliant hacker turned cyber intelligence officer battles a syndicate conducting coordinated financial ransom attacks across Indian banks.",
      backdropUrl = "https://images.unsplash.com/photo-1524492412937-b28074a5d7da?w=1200",
      posterUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=600",
      mediaType = MediaType.SERIES,
      rating = 8.7,
      releaseYear = 2024,
      duration = "1 Season",
      genres = listOf("Bollywood", "Indian Cinema", "Tech Thriller", "Web Series"),
      maturityRating = "16+",
      audioFormats = "HD • Hindi 5.1"
    ),

    // 14. Gladiator II: Colosseum Blood (Blockbuster Cinema & Movies / Global Hits)
    MediaItem(
      id = "g3",
      title = "Gladiator: Eternal Rome",
      tagline = "What we do in life echoes in eternity.",
      overview = "Decades after Maximus, a new warrior rises in the sands of the Colosseum to overthrow corruption and reclaim the honor of the Roman Republic.",
      backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200",
      posterUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=600",
      mediaType = MediaType.MOVIE,
      rating = 8.9,
      releaseYear = 2024,
      duration = "2h 30m",
      genres = listOf("Blockbuster", "Action", "Global Hits"),
      maturityRating = "R",
      audioFormats = "4K IMAX Enhanced"
    )
  )

  // Watchlist in-memory state
  private val myListIds = MutableStateFlow<Set<String>>(setOf("m1", "m3", "a1"))
  private val myListFlow = MutableStateFlow<List<MediaItem>>(emptyList())

  init {
    updateMyList()
  }

  private fun updateMyList() {
    val items = allMedia.filter { myListIds.value.contains(it.id) }
    myListFlow.value = items
  }

  override fun getFeaturedHero(): Flow<List<MediaItem>> {
    return MutableStateFlow(listOf(allMedia[0], allMedia[2], allMedia[4], allMedia[7])).asStateFlow()
  }

  override fun getContinueWatching(): Flow<List<MediaItem>> {
    return MutableStateFlow(allMedia.filter { it.progressPercent != null }).asStateFlow()
  }

  override fun getContentRows(): Flow<List<ContentRowSection>> {
    // Exactly ordered as requested by user:
    // 1. Trending Across Platforms
    // 2. Top Web Series & Originals
    // 3. Blockbuster Cinema & Movies
    // 4. Bollywood & Indian Cinema
    // 5. Anime Hits: Crunchyroll Specials
    // 6. Bollywood & Global Hits
    val sections = listOf(
      ContentRowSection(
        id = "trending_across_platforms",
        title = "Trending Across Platforms",
        subtitle = "Hottest titles streaming across Netflix, Prime & Hotstar",
        items = listOf(allMedia[0], allMedia[2], allMedia[4], allMedia[1], allMedia[7], allMedia[9])
      ),
      ContentRowSection(
        id = "top_web_series_originals",
        title = "Top Web Series & Originals",
        subtitle = "Binge-worthy multi-season flagship television",
        items = listOf(allMedia[0], allMedia[2], allMedia[4], allMedia[11], allMedia[8], allMedia[12])
      ),
      ContentRowSection(
        id = "blockbuster_cinema_movies",
        title = "Blockbuster Cinema & Movies",
        subtitle = "Epic high-octane theatrical releases",
        items = listOf(allMedia[1], allMedia[9], allMedia[10], allMedia[7], allMedia[3], allMedia[13])
      ),
      ContentRowSection(
        id = "bollywood_indian_cinema",
        title = "Bollywood & Indian Cinema",
        subtitle = "Pan-Indian blockbusters, crime sagas & epics",
        items = listOf(allMedia[2], allMedia[7], allMedia[8], allMedia[12], allMedia[0])
      ),
      ContentRowSection(
        id = "anime_hits_crunchyroll",
        title = "Anime Hits: Crunchyroll Specials",
        subtitle = "Simulcasts & world-class Japanese animation",
        items = listOf(allMedia[4], allMedia[5], allMedia[6], allMedia[3], allMedia[1])
      ),
      ContentRowSection(
        id = "bollywood_global_hits",
        title = "Bollywood & Global Hits",
        subtitle = "Worldwide box-office sensations and chartbusters",
        items = listOf(allMedia[9], allMedia[10], allMedia[7], allMedia[13], allMedia[1], allMedia[3])
      )
    )
    return MutableStateFlow(sections).asStateFlow()
  }

  override suspend fun refreshHome(): Result<Unit> = Result.success(Unit)

  override suspend fun getMediaDetails(id: String, type: String?): MediaItem? {
    return getMediaById(id)
  }

  override suspend fun getSeasonEpisodes(seriesId: String, seasonNumber: Int): List<com.example.data.model.Episode> {
    val media = getMediaById(seriesId)
    return media?.seasons?.find { it.seasonNumber == seasonNumber }?.episodes ?: emptyList()
  }

  override suspend fun searchReal(query: String, type: String?): List<MediaItem> {
    return searchCatalog(query, null, null)
  }

  override suspend fun getServers(
    contentId: String,
    type: String,
    season: Int?,
    episode: Int?,
    tmdbId: Int?,
    title: String?
  ): List<com.example.data.model.ServerItem> {
    val cleanId = com.example.data.util.MediaEnrichmentRegistry.getCleanId(contentId, tmdbId, title)
    val isTv = type in listOf("tv", "series")
    val s = season ?: 1
    val e = episode ?: 1
    return listOf(
      com.example.data.model.ServerItem("srv_1", "Server 1 (VidStuck Stream)", "embed", "1080p FHD", "Multi-Audio (Hindi/Eng)", com.example.data.util.MediaEnrichmentRegistry.constructDirectServerUrl("srv_1", cleanId, isTv, s, e), null, true, 0),
      com.example.data.model.ServerItem("srv_tgvid", "Server 2 (TGVid Stream)", "embed", "1080p FHD", "Multi-Audio (Hindi/Eng)", com.example.data.util.MediaEnrichmentRegistry.constructDirectServerUrl("srv_tgvid", cleanId, isTv, s, e), null, true, 1),
      com.example.data.model.ServerItem("srv_2", "Server 3 (Ultra Proxy)", "embed", "1080p FHD", "Cloudflare Worker Proxy", com.example.data.util.MediaEnrichmentRegistry.constructDirectServerUrl("srv_2", cleanId, isTv, s, e), null, true, 2)
    )
  }

  override suspend fun resolvePlayback(
    contentId: String,
    type: String,
    serverId: String,
    season: Int?,
    episode: Int?,
    tmdbId: Int?,
    title: String?
  ): com.example.data.model.PlaybackResolved? {
    val cleanId = com.example.data.util.MediaEnrichmentRegistry.getCleanId(contentId, tmdbId, title)
    val isTv = type in listOf("tv", "series")
    val s = season ?: 1
    val e = episode ?: 1
    val url = com.example.data.util.MediaEnrichmentRegistry.constructDirectServerUrl(serverId, cleanId, isTv, s, e)
    return com.example.data.model.PlaybackResolved(
      contentId = cleanId,
      type = if (isTv) "tv" else "movie",
      serverId = serverId,
      serverName = "Server ($serverId)",
      streamUrl = url,
      securePlayerUrl = url,
      quality = "1080p FHD",
      language = "Multi-Audio",
      season = if (isTv) s else null,
      episode = if (isTv) e else null,
      available = true
    )
  }

  override fun getMediaById(id: String): MediaItem? {
    return allMedia.find { it.id == id } ?: allMedia.firstOrNull()
  }

  override fun getComingSoon(): Flow<List<MediaItem>> {
    return MutableStateFlow(listOf(allMedia[7], allMedia[9])).asStateFlow()
  }

  override fun getMyList(): Flow<List<MediaItem>> {
    return myListFlow.asStateFlow()
  }

  override fun toggleMyList(mediaItem: MediaItem): Boolean {
    val current = myListIds.value.toMutableSet()
    val added = if (current.contains(mediaItem.id)) {
      current.remove(mediaItem.id)
      false
    } else {
      current.add(mediaItem.id)
      true
    }
    myListIds.value = current
    updateMyList()
    return added
  }

  override fun isInMyList(id: String): Boolean {
    return myListIds.value.contains(id)
  }

  override fun searchCatalog(query: String, genre: String?, mediaType: MediaType?): List<MediaItem> {
    return allMedia.filter { item ->
      val matchesQuery = query.isBlank() ||
          item.title.contains(query, ignoreCase = true) ||
          item.genres.any { it.contains(query, ignoreCase = true) } ||
          item.overview.contains(query, ignoreCase = true) ||
          item.cast.any { it.name.contains(query, ignoreCase = true) }

      val matchesGenre = genre == null || genre == "All" || item.genres.any { it.equals(genre, ignoreCase = true) }
      val matchesType = mediaType == null || item.mediaType == mediaType

      matchesQuery && matchesGenre && matchesType
    }
  }

  override fun getTrendingKeywords(): List<String> {
    return listOf("Solo Leveling", "Chronicles of Kepler", "The Mumbai Syndicate", "Dune", "Anime", "Jawan", "Bollywood", "4K HDR")
  }

  override fun getRecentSearches(): List<String> {
    return listOf("Kepler", "Solo Leveling", "Dune 2024", "Crime Thriller")
  }

  override fun getAllGenres(): List<String> {
    return listOf("All", "Web Series", "Blockbuster", "Bollywood", "Anime", "Sci-Fi", "Action", "Crime")
  }

  override fun getWatchHistory(): List<MediaItem> {
    return allMedia.filter { it.progressPercent != null }
  }

  override fun recordWatchProgress(mediaItem: MediaItem, progressPercent: Float) {
    // Mock implementation
  }
}
