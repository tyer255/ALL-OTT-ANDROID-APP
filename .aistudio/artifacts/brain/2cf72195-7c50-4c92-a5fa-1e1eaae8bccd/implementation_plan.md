# Aether Stream - Premium Android OTT Streaming App

Aether Stream is an original, cinematic Android OTT streaming application engineered with Jetpack Compose and Material Design 3. It combines the visual drama of flagship entertainment platforms with a restrained, human-crafted design language, dynamic local mock repositories, and an API-ready architecture.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following user preferences were confirmed in Phase 1 and define the visual identity, catalog curation, and playback experience:

- **Brand Identity & Color Tone**: **Aether Stream** — Obsidian and deep charcoal slate (`#0B0E14` base, `#141A24` surfaces) paired with warm cinematic amber (`#F59E0B` / `#D97706`) and neutral platinum typography. Zero purple/cyan AI gradients, glowing borders, or neon slop.
- **Content Catalog Focus**: Global blockbusters, expansive sci-fi epics, and prestige drama series, balanced with regional cinema and trending originals.
- **Player Screen Presentation**: Dedicated full-screen playback environment featuring interactive scrub bars, duration indicators, audio/subtitle selector sheets, quality badges, and episodic back/forward quick navigation.

---

## 1. Overview & Core Concept

- **What It Does**: Delivers a full-featured streaming discovery and viewing experience on Android. Users can browse an auto-cycling cinematic hero carousel, explore multi-genre horizontal rows (Continue Watching, Trending Now, Top Rated, Sci-Fi, Hollywood, Indian Cinema), conduct real-time filtered searches, inspect detailed media sheets (with cast profiles, trailers, seasons, and episodes), toggle personal watchlists, view upcoming theatrical releases, and launch a complete mock player.
- **Target Audience / Persona**: Entertainment enthusiasts seeking an intuitive, responsive mobile streaming experience with immediate tactile feedback, crisp artwork rendering, and zero cognitive clutter.
- **Key Value**: A production-grade frontend architecture that feels completely alive today using structured mock repositories, designed to swap seamlessly to TMDB/ExoPlayer/Cloud APIs in future iterations without altering Compose layouts.

---

## 2. User Experience & Visual Design

### Key User Flows

```
┌──────────────┐     Select Media     ┌──────────────────────┐     Tap "Watch"     ┌──────────────────────┐
│  Home Feed   │ ───────────────────> │  Movie/Series Detail │ ──────────────────> │  Full-Screen Player  │
│ (Hero, Rows) │                      │ (Backdrop, Cast, Ep) │                     │ (Scrubber, Controls) │
└──────┬───────┘                      └──────────┬───────────┘                     └──────────────────────┘
       │                                         │ Add to List
       │ Bottom Nav                              ▼
       │                              ┌──────────────────────┐
       ├────────────────────────────> │   My List / Profile  │
       │                              │ (Watchlist, History) │
       │                              └──────────────────────┘
       │ Bottom Nav
       ▼
┌──────────────┐     Live Filter      ┌──────────────────────┐
│ Search Hub   │ ───────────────────> │ Categorized Results  │
│ (Input, Tag) │                      │ (Movies, TV, Status) │
└──────────────┘                      └──────────────────────┘
```

1. **Home Screen**:
   - **Cinematic Hero**: Edge-to-edge backdrop carousel featuring featured blockbusters with smooth auto-cycle timer, synopsis, IMDb-style rating pills, year, genres, "Watch Now" primary action, and "Add to List" toggle.
   - **Curated Content Rows**: Horizontal scroll rows with crisp poster ratios (2:3 for movies/series, 16:9 with progress bars for *Continue Watching*), category headers with tap targets, and smooth scroll momentum.
2. **Dedicated Search Hub**:
   - Clean search bar with clear button, interactive category chips (*All, Movies, Series, Sci-Fi, Action, Drama*), quick trending badges, and responsive vertical grid of results distinguishing format and status.
3. **Detail Page (Movie & Series)**:
   - Deep cinematic backdrop with subtle vignette scrim, title typography, metadata row (Rating, 4K HDR, Dolby Atmos, Runtime, Release Year), primary action bar, synopsis, full cast cards with character names, and "More Like This" recommendations.
   - For TV Series: Segmented season selector tab (`Season 1`, `Season 2`) expanding into rich episode lists with thumbnail stills, duration, title, and synopsis.
4. **Player UI Mockup**:
   - Immersive dark canvas with top navigation (back button, title, audio track, subtitle toggles), center play/pause/skip-10s controls, interactive scrubbing progress bar, timestamp indicators (`01:24:10 / 02:45:00`), quality badge (`4K HDR`), and Episode drawer for episodic content.
5. **Profile & My List Experience**:
   - Profile header, subscription status badge, segmented tabs (*Movies*, *Series*, *Saved*), watch history grid with quick-resume, and app preferences.
6. **Coming Soon Feed**:
   - Dedicated feed of anticipated releases displaying countdown badges, release dates, synopsis, and reminder notifications toggle.

### Visual Identity & Theme Tokens

In strict adherence to human-designed principles (No AI gradients, no glowing buttons, no decorative emojis):

- **Background Base**: `#0A0D14` (Deep obsidian slate)
- **Surface Elevation 1**: `#121824` (Subtle dark charcoal for cards and drawers)
- **Surface Elevation 2**: `#1A2232` (Interactive surfaces, pill chips)
- **Primary Brand Accent**: `#F59E0B` (Amber gold for primary play button, active indicators)
- **Accent On Surface**: `#0A0D14` (Deep contrast text on primary buttons)
- **Text Primary**: `#F8FAFC` (Crisp off-white / high readability)
- **Text Secondary**: `#94A3B8` (Muted slate for metadata and labels)
- **Rating / Highlight**: `#EAB308` (Warm star amber)
- **Progress Bar Track**: `#334155` / **Indicator**: `#F59E0B`
- **Corner Radii**: Restrained 8.dp for posters/cards, 12.dp for elevated sheets, 24.dp for pill chips.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Modular In-Memory Repository vs. Static Composables**
  - *Chosen Approach*: Implement `MediaRepository` and `StreamingCatalog` domain models supplying Kotlin `StateFlow` streams through an `AetherViewModel`.
  - *Why*: Allows realistic live state updates (e.g., adding/removing from My List, updating Continue Watching progress, filtering live search results) while maintaining a strict interface for future REST/TMDB integration.
  - *Trade-off*: Requires writing clean domain models up front, but eliminates refactoring when backend APIs are connected.
- **Decision 2: Compose Type-Safe Navigation & Back Handling**
  - *Chosen Approach*: Single-activity Compose architecture using a clear screen state sealed class (`Screen.Home`, `Screen.Search`, `Screen.Detail(mediaId)`, `Screen.Player(mediaId, episodeId)`, `Screen.MyList`, `Screen.Profile`, `Screen.ComingSoon`) with explicit `BackHandler` integration.
  - *Why*: Provides fluid transitions, simplifies the floating bottom navigation bar state, and ensures reliable Android back button behavior without stack desynchronization.
- **Decision 3: Image Loading Strategy**
  - *Chosen Approach*: High-quality Unsplash cinematic photography URLs backed by Coil (`coil-compose`) with vector fallbacks and styled canvas poster placeholders.
  - *Why*: Yields realistic cinematic aesthetics on emulator and devices without bundled heavy assets, handling network latency gracefully.

---

## 4. Technical Architecture & Data Strategy

### Component Hierarchy & Data Flow

```
┌────────────────────────────────────────────────────────┐
│                      MainActivity                      │
│                  AetherStreamTheme                     │
└───────────────────────────┬────────────────────────────┘
                            │
              ┌─────────────▼──────────────┐
              │      AetherViewModel       │ <─── LocalMediaRepository
              │ (Catalog, List, Playback)  │      (Domain Models & Mock DB)
              └─────────────┬──────────────┘
                            │ StateFlow<AetherUiState>
        ┌───────────────────┴───────────────────┐
        ▼                                       ▼
┌───────────────────────────────┐     ┌─────────────────────────┐
│     Scaffold & Navigation     │     │      PlayerScreen       │
│  (Floating Bottom Nav Bar)    │     │ (Overlay Controls, Bar) │
└───────┬───────────────────────┘     └─────────────────────────┘
        │
        ├── Screen.Home ──────> HeroCarousel, ContentRow, MediaCard
        ├── Screen.Search ────> SearchBar, GenreChips, ResultsGrid
        ├── Screen.Detail ────> BackdropHeader, ActionRow, EpisodeList
        ├── Screen.MyList ────> FilterTabs, MediaGrid
        ├── Screen.ComingSoon > UpcomingList, ReminderPill
        └── Screen.Profile ───> UserHeader, HistoryRow, SettingsList
```

### Domain Models

- `MediaItem`: `id`, `title`, `overview`, `backdropUrl`, `posterUrl`, `mediaType` (Movie/Series), `rating`, `releaseYear`, `duration`, `genres`, `isTrending`, `isTopRated`, `isComingSoon`, `progressPercent`, `castMembers`.
- `Episode`: `id`, `seasonNumber`, `episodeNumber`, `title`, `overview`, `thumbnailUrl`, `duration`.
- `CastMember`: `name`, `character`, `profileUrl`.
- `AetherUiState`: `featuredItems`, `contentRows`, `myList`, `continueWatching`, `searchQuery`, `searchResults`, `selectedMedia`, `currentPlayingMedia`, `isPlayerVisible`.

### Next-Step API Migration Roadmap (Documented for Phase 2)

Inside the repository layer, interfaces will be annotated with clean extension points:
1. Replace `LocalMediaRepositoryImpl` with `TmdbMediaRepositoryImpl` using Retrofit/Moshi.
2. Hook Android `Media3 / ExoPlayer` to replace the `PlayerSurface` mock component while retaining 100% of the UI controls and scrubber overlay.
3. Migrate `myList` and user history from in-memory StateFlow to `RoomDatabase` with DAOs.
