# Personal Task Guide — Hossain Ayman (24101407)

## 1. Feature Status Overview (5 Assigned Features)

| Feature ID | Sprint | Feature Name | Status | Primary URL Prefix |
| :--- | :--- | :--- | :--- | :--- |
| **F07** | Sprint 1 (Module 2) | TV Series Episode Tracker & Binge-Pace Calculator | **DONE (Merged to `main`)** | `/episodes/**` |
| **F03** | Sprint 2–4 (Module 1) | "What Should I Watch Tonight?" Personal Recommender | **PLANNED** | `/tonights-pick/**` |
| **F04** | Sprint 2–4 (Module 1) | Smart Watchlist & "Leaving Soon" Priority Sorter | **PLANNED** | `/watchlist/**` |
| **F16** | Sprint 2–4 (Module 4) | Spoiler-Locked Episode & Ending Discussion Threads | **PLANNED** | `/discussions/**` |
| **F20** | Sprint 2–4 (Module 4) | Seasonal Watch Challenges & Milestone Badges | **PLANNED** | `/challenges/**` |

---

## 2. Completed Feature Details & Live File Inventory (Agent Handoff Notes)

### F07 — TV Series Episode Tracker & Binge-Pace Calculator (`DONE`)
- **What Is Live:**
  - Per-user episode tracking dashboard (`GET /episodes`) with watch-status filter pills (`ALL`, `WATCHING`, `ON_HOLD`, `COMPLETED`, `DROPPED`), live status counts, tracked show cards, and untracked catalog browsing (`Start Tracking`).
  - Series detail tracker (`GET /episodes/{seriesId}`) with season-grouped episode checklists, sequential checkpoint checkoffs, step-back undo when toggling the current checkpoint, auto-completion (`COMPLETED`) on the final episode (and auto-revert to `WATCHING` when stepping back), plus one-click `+1 Next Episode` advancement.
  - Remaining-time and daily binge-pace calculator (zero-division safe across empty episode lists, completed series, unset target dates, overdue targets, finish-today targets, and multi-day targets).
  - Live TMDB v3 season/episode sync (`EpisodeTmdbSyncService`) fetching `/tv/{tmdbId}` and `/tv/{tmdbId}/season/{seasonNumber}` while strictly excluding `season_number == 0` (Specials) so F16's `(0, 0)` spoiler-free baseline stays clean.
  - Neon PostgreSQL query optimization: `GET /episodes` executes at most 3 feature queries total (`findAllSeriesWithEpisodeCounts` via `LEFT JOIN`, `findAllProgressByUserId`, and bulk `findEpisodesBySeriesIds` via `ANY(?)` when tracked series exist); `upsertEpisodes` uses a single PostgreSQL `UNNEST(...)` round-trip; `startTracking`, `updateSettingsIfSeriesExists`, and `resetProgressIfSeriesExists` run single-query `INSERT ... ON CONFLICT` statements.
  - Idempotent fallback seed SQL seeding Seasons 1 & 2 episodes for starter TV series in `data.sql` (`You`, `Jujutsu Kaisen`).
- **Live HTTP Routes (`EpisodeController` — `@RequestMapping("/episodes")`):**
  - `GET /episodes` (`?status=ALL|WATCHING|ON_HOLD|COMPLETED|DROPPED`)
  - `GET /episodes/{seriesId}`
  - `POST /episodes/{seriesId}/track`
  - `POST /episodes/{seriesId}/check` (`seasonNumber`, `episodeNumber`, optional `returnTo`)
  - `POST /episodes/{seriesId}/next` (optional `returnTo`)
  - `POST /episodes/{seriesId}/settings` (`watchStatus`, optional `targetFinishDate`)
  - `POST /episodes/{seriesId}/sync`
  - `POST /episodes/{seriesId}/reset`
  - `POST /episodes/{seriesId}/remove`
- **Live Files:**
  - `src/main/java/com/cinecircle/model/Episode.java`
  - `src/main/java/com/cinecircle/model/EpisodeProgress.java`
  - `src/main/java/com/cinecircle/model/EpisodeViewModels.java`
  - `src/main/java/com/cinecircle/repository/EpisodeRepository.java`
  - `src/main/java/com/cinecircle/service/EpisodeService.java`
  - `src/main/java/com/cinecircle/service/EpisodeTmdbSyncService.java`
  - `src/main/java/com/cinecircle/controller/EpisodeController.java`
  - `src/main/resources/templates/episodes/dashboard.html`
  - `src/main/resources/templates/episodes/tracker.html`
  - `src/main/resources/static/css/f7-episodes.css`
  - `src/main/resources/static/js/f7-episodes.js`
  - `src/main/resources/sql/f7_seed.sql`
- **Data Contract Note for Teammates / Agents:**
  - `episodes` is shared catalog data (`UNIQUE (series_media_id, season_number, episode_number)`), with Season 0 Specials excluded during TMDB sync.
  - `episode_progress` is per-user (`UNIQUE (user_id, series_media_id)`) storing `highest_season`, `highest_episode`, `watch_status` (`WATCHING`, `ON_HOLD`, `DROPPED`, `COMPLETED`), and `target_finish_date` — this is the checkpoint source consumed later by F16 spoiler-locking.
  - `EpisodeProgress.java` preserves full backward compatibility (all original fields, getters, and setters plus an explicit default constructor and `getCheckpointLabel()`) with the existing shared `EpisodeProgressRepository.java`.

### Team Lead Shared Infrastructure Completed (`DONE` — Merged to `main`)
- **Logged-In Slide-Over Sidebar Navigation:**
  - **Live Files:**
    - `src/main/resources/templates/fragments/navbar.html`
    - `src/main/resources/static/css/style.css`
    - `src/main/resources/static/js/sidebar.js`
  - **Handoff Rule for Teammates / Agents:** Every page that includes `th:replace="~{fragments/navbar :: navbar}"` automatically receives the top navbar and the authenticated slide-over sidebar drawer (`#ccSidebar`) with pre-wired links for Home (`/`) and all 20 feature routes (`/tmdb/search`, `/catalog`, `/platforms`, `/tonights-pick`, `/watchlist`, `/franchises`, `/episodes`, `/reviews`, `/diary`, `/lists`, `/watch-wrapped`, `/taste-match`, `/lobby`, `/social`, `/buddy-watch`, `/advisories`, `/discussions`, `/clubs`, `/brackets`, `/challenges`), plus `Episodes` (`/episodes`) in the top bar for logged-in users. **Teammates and their AI agents must NOT edit `navbar.html` or `style.css` to add nav links.**
- **TMDB Catalog Search & Bulk Import:**
  - **Live Files:**
    - `src/main/java/com/cinecircle/controller/TmdbCatalogController.java`
    - `src/main/java/com/cinecircle/repository/TmdbCatalogRepository.java`
    - `src/main/resources/templates/tmdb/search.html`
    - `src/main/resources/static/css/tmdb-search.css`
  - **Live Routes & Data Note:** `GET /tmdb/search`, `POST /tmdb/import`, `GET /tmdb/bulk-import`. Imported TV shows are saved in `media_items` with `media_type = 'SERIES'`; feature SQL queries should filter defensively using `UPPER(media_type) IN ('SERIES', 'TV')`.

---

## 3. Remaining Assigned Features (Planned)

1. **F03 (Module 1 — `/tonights-pick/**`):** "What Should I Watch Tonight?" Personal Recommender
   - 3 prompts: Current Mood/Vibe, Max Time Available, Solo or Family-Safe.
   - Exclude movies already present in `reviews` for the current user; match `user_subscriptions` and past high-rated genres. Return top 3 unwatched movies.
2. **F04 (Module 1 — `/watchlist/**`):** Smart Watchlist & "Leaving Soon" Priority Sorter
   - Calculate dynamic `priority_score` based on friend ratings in `reviews` + upcoming `leaving_date` in `streaming_availability`.
   - Auto-archive items with `last_interacted_at` older than 180 days into an Archived tab.
3. **F16 (Module 4 — `/discussions/**`):** Spoiler-Locked Episode & Ending Discussion Threads
   - Bind every `discussion_posts` entry to a `checkpoint_season` / `checkpoint_episode`.
   - Compare against viewer's `episode_progress`: blur and lock threads if user's logged episode is lower.
4. **F20 (Module 4 — `/challenges/**`):** Seasonal Watch Challenges & Milestone Badges
   - Opt into challenges in `watch_challenges` (`user_challenges`).
   - Verify logged `reviews` against challenge rules (genre, country, max runtime, count), update progress bar, and unlock badge at 100%.

---

## 4. Files Owned by Ayman (Updated & Accurate)

- **Controllers:**
  - Live (`DONE`): `EpisodeController.java`, `TmdbCatalogController.java` (shared TMDB catalog import)
  - Planned: `RecommenderController.java`, `WatchlistController.java`, `DiscussionController.java`, `ChallengeController.java`
- **Models / ViewModels:**
  - Live (`DONE`): `Episode.java`, `EpisodeProgress.java`, `EpisodeViewModels.java`
- **Services:**
  - Live (`DONE`): `EpisodeService.java`, `EpisodeTmdbSyncService.java`
  - Planned: `RecommendationService.java`, `WatchlistPriorityService.java`, `SpoilerGuardService.java`, `ChallengeService.java`
- **Repositories:**
  - Live (`DONE`): `EpisodeRepository.java`, `TmdbCatalogRepository.java` (shared TMDB catalog import)
  - Planned: `WatchlistRepository.java`, `ChallengeRepository.java` (plus inject shared `MediaItemRepository`, `ReviewRepository`, `EpisodeProgressRepository`, `EpisodeRepository`, `DiscussionPostRepository`, `UserRepository`)
- **Templates, Static Assets & SQL:**
  - Live (`DONE`): `templates/episodes/dashboard.html`, `templates/episodes/tracker.html`, `static/css/f7-episodes.css`, `static/js/f7-episodes.js`, `src/main/resources/sql/f7_seed.sql` (plus shared `templates/tmdb/search.html`, `static/css/tmdb-search.css`, `static/js/sidebar.js`)
  - Planned: `templates/recommender/*.html`, `templates/watchlist/*.html`, `templates/discussions/*.html`, `templates/challenges/*.html`
