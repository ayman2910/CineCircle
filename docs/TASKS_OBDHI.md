# Personal Task Guide — Suhaila Obdhi (24101193)

## Assigned Features (5)
1. **F02 (Module 1):** "Where to Watch" Streaming Availability & Price Tracker
   - Show platforms (`streaming_availability`) on movie pages.
   - Let users toggle owned platforms in `user_subscriptions` and filter catalog by "Available on My Services" via SQL `INNER JOIN`.
2. **F06 (Module 2):** 5-Category Breakdown Rating & Review System
   - Form with 5 sliders (1-10): Story/Pacing, Acting, Visuals, Music, Rewatch Value + written review.
   - Service computes weighted `overall_score = (story*0.30) + (acting*0.25) + (visuals*0.15) + (music*0.15) + (rewatch*0.15)` and displays community category averages via `SELECT AVG(...)`.
3. **F10 (Module 2):** Personal "Watch-Wrapped" Stats & Habit Dashboard
   - Read-only dashboard querying `reviews` + `media_items` via SQL `COUNT`, `SUM`, `AVG`, `GROUP BY`.
   - Shows total movies/hours watched, top 3 genres, and user's average rating vs. global community average.
4. **F15 (Module 3):** Parent / Family Content & Trigger-Warning Filter
   - Users vote on advisories (`FAMILY_SAFE`, `AWKWARD_SCENES`, `JUMP_SCARES`, `EXTREME_VIOLENCE`) in `content_advisory_votes`.
   - If >75% vote safe, update `media_items.is_family_safe = true` and provide a "Family-Safe Only" filter.
5. **F17 (Module 4):** Community Spoiler Reporting & Auto-Quarantine Moderation
   - Users click "Report Spoiler" on a `discussion_posts` entry -> inserts into `spoiler_reports` and increments `report_count`.
   - If `report_count >= 3`, auto-set `is_quarantined = true` (blurred).
   - Moderator dashboard to Confirm Spoiler (delete/hide + decrease author `trust_score`) or Dismiss Report (un-blur).

## Files You Own (Create/Edit Only These)
- **Controllers:** `StreamingController.java`, `ReviewController.java`, `StatsController.java`, `AdvisoryController.java`, `ModerationController.java`
- **Services:** `StreamingService.java`, `ReviewService.java`, `WatchWrappedService.java`, `ContentAdvisoryService.java`, `SpoilerModerationService.java`
- **Repositories:** `StreamingRepository.java`, `AdvisoryRepository.java`, `SpoilerReportRepository.java` (plus inject shared `ReviewRepository`, `MediaItemRepository`, `DiscussionPostRepository`, `UserRepository`)
- **Templates:** `templates/streaming/*.html`, `templates/reviews/*.html`, `templates/stats/*.html`, `templates/advisory/*.html`, `templates/moderation/*.html`
