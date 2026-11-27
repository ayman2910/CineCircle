# CineCircle — CSE470 Group 4

A Social Watch-Party, Movie Club & Taste-Match Recommendation Hub built with **Java 21**, **Spring Boot (Spring MVC)**, **JdbcTemplate**, **Thymeleaf**, and **PostgreSQL**.

## Team Members & Feature Allocation
- **Hossain Ayman (24101407):** F03, F04, F07, F16, F20 (`docs/TASKS_AYMAN.md`)
- **Suhaila Obdhi (24101193):** F02, F06, F10, F15, F17 (`docs/TASKS_OBDHI.md`)
- **Noshin Noor (24101154):** F01, F09, F11, F12, F18 (`docs/TASKS_NOSHIN.md`)
- **Amir Abrar Akib (24101163):** F05, F08, F13, F14, F19 (`docs/TASKS_AMIR.md`)

## Quick Start (Day 1 Setup)
1. **Database:** Update `src/main/resources/application.properties` with our shared Neon/Supabase PostgreSQL URL (or local PostgreSQL credentials).
2. **TMDB API Key:** Paste the shared TMDB API key into `tmdb.api.key` in `application.properties`.
3. **Run the App:**
   ```bash
   mvn spring-boot:run
   ```
   Open `http://localhost:8080`. The database tables (`schema.sql`) and sample movies/users (`data.sql`) load automatically on startup.
4. **Git Workflow (Zero Merge Conflicts):**
   - Never push directly to `main`.
   - Create your own branch: `git checkout -b feat-<yourname>-sprint1`
   - Only create/edit files listed in your `docs/TASKS_<NAME>.md`.
   - Do NOT edit shared core files (`User.java`, `MediaItem.java`, `pom.xml`, `schema.sql`) without coordinating in the group chat first.

## Test Login Accounts (Seeded in `data.sql`)
- `ayman@cinecircle.com` / `password123` (Role: `ADMIN`)
- `obdhi@cinecircle.com` / `password123` (Role: `MODERATOR`)
- `noshin@cinecircle.com` / `password123` (Role: `HOST`)
- `amir@cinecircle.com` / `password123` (Role: `VIEWER`)
