# CineCircle — Shared Architecture & Database Schema Reference

All 4 teammates share this exact PostgreSQL schema (auto-initialized from `src/main/resources/schema.sql`).
Always use these exact table and column names in your `JdbcTemplate` SQL queries.

## Package Structure
```text
com.cinecircle
├── CineCircleApplication.java
├── config/
│   └── SecurityConfig.java          (LOCKED - BCrypt + Spring Security)
├── model/                           (Shared POJOs matching DB tables)
│   ├── User.java                    (LOCKED)
│   ├── MediaItem.java               (LOCKED)
│   ├── Review.java
│   ├── EpisodeProgress.java
│   └── DiscussionPost.java
├── repository/                      (JdbcTemplate + RowMapper)
│   ├── UserRepository.java          (LOCKED)
│   ├── MediaItemRepository.java     (LOCKED)
│   ├── ReviewRepository.java        (Shared Read/Write)
│   ├── EpisodeProgressRepository.java
│   └── DiscussionPostRepository.java
├── service/                         (Put member-specific Services here)
│   ├── AuthService.java
│   └── TmdbService.java             (TMDB API helper)
└── controller/                      (Put member-specific Controllers here)
    ├── HomeController.java
    └── AuthController.java
```

## Core Shared Tables (Already Created in `schema.sql`)
1. **`users`**: `id`, `name`, `email`, `password_hash`, `role` (`VIEWER`, `HOST`, `MODERATOR`, `ADMIN`), `trust_score`, `created_at`
2. **`user_subscriptions`**: `user_id`, `platform_name` (`Netflix`, `Prime Video`, `Chorki`, `Hoichoi`, `Disney+`, `Cinema`)
3. **`media_items`**: `id`, `tmdb_id`, `title`, `media_type` (`MOVIE`, `SERIES`), `genre`, `country`, `release_year`, `runtime_minutes`, `vibe_tags` (comma-separated), `poster_path`, `overview`, `is_family_safe`
4. **`streaming_availability`**: `id`, `media_id`, `platform_name`, `access_type` (`SUBSCRIPTION`, `RENT`, `BUY`), `price`, `leaving_date`
5. **`episodes`**: `id`, `series_media_id`, `season_number`, `episode_number`, `title`, `runtime_minutes`
6. **`reviews`**: `id`, `user_id`, `media_id`, `story_score`, `acting_score`, `visuals_score`, `music_score`, `rewatch_score`, `overall_score`, `review_text`, `is_rewatch`, `watched_date`, `created_at`
7. **`episode_progress`**: `id`, `user_id`, `series_media_id`, `highest_season`, `highest_episode`, `watch_status` (`WATCHING`, `ON_HOLD`, `DROPPED`, `COMPLETED`), `target_finish_date`
8. **`watchlist_items`**: `id`, `user_id`, `media_id`, `added_at`, `last_interacted_at`, `priority_score`, `is_archived`
9. **`discussion_posts`**: `id`, `author_id`, `media_id`, `checkpoint_season`, `checkpoint_episode`, `content`, `report_count`, `is_quarantined`, `created_at`
10. **`spoiler_reports`**: `id`, `post_id`, `reporter_id`, `reason`, `resolution_status` (`PENDING`, `CONFIRMED`, `DISMISSED`), `created_at`
11. **`content_advisory_votes`**: `id`, `user_id`, `media_id`, `warning_category` (`FAMILY_SAFE`, `AWKWARD_SCENES`, `JUMP_SCARES`, `EXTREME_VIOLENCE`), `is_safe_vote`

## Feature-Specific Tables (Also Pre-Created in `schema.sql`)
- `custom_lists` & `custom_list_items` (F09 - Noshin)
- `franchise_guides` & `franchise_guide_items` (F05 - Amir)
- `group_lobbies`, `lobby_members`, `lobby_votes` (F12 - Noshin)
- `recommendation_prompts`, `recommendation_replies` (F13 - Amir)
- `buddy_watches` (F14 - Amir)
- `movie_clubs`, `movie_club_members` (F18 - Noshin)
- `tournament_brackets`, `bracket_matchups` (F19 - Amir)
- `watch_challenges`, `user_challenges` (F20 - Ayman)
