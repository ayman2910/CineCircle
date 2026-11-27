# Personal Task Guide — Amir Abrar Akib (24101163)

## Assigned Features (5)
1. **F05 (Module 1):** Franchise & Watch-Order Guide Builder
   - View multi-part franchises (`franchise_guides`, `franchise_guide_items`) toggled between Release Order and Chronological Order with automatic completion % from `reviews`.
2. **F08 (Module 2):** Personal Watch Diary, Rewatch Logger & Calendar Timeline
   - Log dated watches and rewatches in `reviews` (`watched_date`, `is_rewatch`).
   - Render a monthly calendar view and show rating delta between first watch and rewatch.
3. **F13 (Module 3):** Friend Activity Feed & "Ask for a Recommendation" Inbox
   - Display recent reviews from other users and allow posting recommendation requests (`recommendation_prompts`).
   - Friends attach catalog movies as replies (`recommendation_replies`) and upvote suggestions.
4. **F14 (Module 3):** Direct "Watch-Dare" / Buddy-Watch Tracker
   - Two users pair up on a movie (`buddy_watches`). Keep individual reviews hidden until both submit ratings, then unlock a side-by-side comparison card.
5. **F19 (Module 4):** Head-to-Head Movie Showdowns & Community Awards Brackets
   - Knockout tournament brackets (`tournament_brackets`, `bracket_matchups`) advancing winners from Quarterfinals -> Semifinals -> Winner.

## Files You Own (Create/Edit Only These)
- **Controllers:** `FranchiseController.java`, `DiaryController.java`, `FeedController.java`, `BuddyWatchController.java`, `BracketController.java`
- **Services:** `FranchiseService.java`, `DiaryService.java`, `FeedService.java`, `BuddyWatchService.java`, `BracketService.java`
- **Repositories:** `FranchiseRepository.java`, `FeedRepository.java`, `BuddyWatchRepository.java`, `BracketRepository.java` (plus inject shared `MediaItemRepository`, `ReviewRepository`, `UserRepository`)
- **Templates:** `templates/franchises/*.html`, `templates/diary/*.html`, `templates/feed/*.html`, `templates/buddy/*.html`, `templates/brackets/*.html`
