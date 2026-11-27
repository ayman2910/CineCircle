# Personal Task Guide — Hossain Ayman (24101407)

## Assigned Features (5)
1. **F03 (Module 1):** "What Should I Watch Tonight?" Personal Recommender
   - 3 prompts: Current Mood/Vibe, Max Time Available, Solo or Family-Safe.
   - Exclude movies already present in `reviews` for the current user; match `user_subscriptions` and past high-rated genres. Return top 3 unwatched movies.
2. **F04 (Module 1):** Smart Watchlist & "Leaving Soon" Priority Sorter
   - Calculate dynamic `priority_score` based on friend ratings in `reviews` + upcoming `leaving_date` in `streaming_availability`.
   - Auto-archive items with `last_interacted_at` older than 180 days into an Archived tab.
3. **F07 (Module 2):** TV Series Episode Tracker & Binge-Pace Calculator
   - Check off episodes per season, update `episode_progress` (`WATCHING`, `ON_HOLD`, `DROPPED`, `COMPLETED`).
   - Compute remaining hours from `episodes` and required episodes/day to finish by `target_finish_date`.
4. **F16 (Module 4):** Spoiler-Locked Episode & Ending Discussion Threads
   - Bind every `discussion_posts` entry to a `checkpoint_season` / `checkpoint_episode`.
   - Compare against viewer's `episode_progress`: blur and lock threads if user's logged episode is lower.
5. **F20 (Module 4):** Seasonal Watch Challenges & Milestone Badges
   - Opt into challenges in `watch_challenges` (`user_challenges`).
   - Verify logged `reviews` against challenge rules (genre, country, max runtime, count), update progress bar, and unlock badge at 100%.

## Files You Own (Create/Edit Only These)
- **Controllers:** `RecommenderController.java`, `WatchlistController.java`, `SeriesTrackerController.java`, `DiscussionController.java`, `ChallengeController.java`
- **Services:** `RecommendationService.java`, `WatchlistPriorityService.java`, `BingePaceService.java`, `SpoilerGuardService.java`, `ChallengeService.java`
- **Repositories:** `WatchlistRepository.java`, `ChallengeRepository.java` (plus inject shared `MediaItemRepository`, `ReviewRepository`, `EpisodeProgressRepository`, `DiscussionPostRepository`)
- **Templates:** `templates/recommender/*.html`, `templates/watchlist/*.html`, `templates/series/*.html`, `templates/discussions/*.html`, `templates/challenges/*.html`
