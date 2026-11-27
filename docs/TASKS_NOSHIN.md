# Personal Task Guide — Noshin Noor (24101154)

## Assigned Features (5)
1. **F01 (Module 1):** Movie & Series Catalog with "Vibe" & Runtime Filters
   - Browse/search `media_items` by keyword, genre, custom `vibe_tags`, and `runtime_minutes <= maxRuntime`.
   - Admin/Curator option to import new titles via `TmdbService` and attach custom vibe tags.
2. **F09 (Module 2):** Custom Ranked Lists & Tier-List Builder
   - Create ordered Top-10 or S/A/B/C/D tier lists (`custom_lists`, `custom_list_items`).
   - Show "You have seen X% of this list" by checking viewer's `reviews` and support 1-click Fork/Clone.
3. **F11 (Module 3):** Friend "Taste-Match" Compatibility Engine
   - Compare two users' 5-category ratings in `reviews` to compute a 0-100% Taste Match score.
   - Highlight "Movies You Both Loved" (both >= 8) and "Biggest Disagreements" (gap >= 4).
4. **F12 (Module 3):** Pre-Watch Group "Movie Picker" Lobby
   - Host opens a room (`group_lobbies`, `lobby_members`), hides titles any member already reviewed, filters by shared subscriptions.
   - Members vote (`YES=2`, `OKAY=1`, `VETO=-999`) in `lobby_votes` to lock the winning movie.
5. **F18 (Module 4):** Weekly Movie Club & Poll Manager
   - Moderators create themed clubs (`movie_clubs`), run weekly nomination/voting polls, set Movie of the Week for 7 days, and open club discussion.

## Files You Own (Create/Edit Only These)
- **Controllers:** `CatalogController.java`, `TierListController.java`, `TasteMatchController.java`, `LobbyController.java`, `MovieClubController.java`
- **Services:** `CatalogService.java`, `TierListService.java`, `TasteMatchService.java`, `LobbyService.java`, `MovieClubService.java`
- **Repositories:** `TierListRepository.java`, `LobbyRepository.java`, `MovieClubRepository.java` (plus inject shared `MediaItemRepository`, `ReviewRepository`, `UserRepository`)
- **Templates:** `templates/catalog/*.html`, `templates/lists/*.html`, `templates/tastematch/*.html`, `templates/lobby/*.html`, `templates/clubs/*.html`
