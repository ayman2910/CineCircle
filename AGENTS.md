# CineCircle — Master AI IDE Rules (CSE470 Group 4)

You are assisting a 4-member university software engineering team building **CineCircle** with Java 21, Spring Boot 3 (Spring MVC), Spring JdbcTemplate (explicit SQL), Thymeleaf, and PostgreSQL.

## 1. Strict Architecture & Stack Rules
- **Architecture:** Strict MVC separation.
  - `@Controller` classes ONLY handle HTTP routing, request parameters/DTOs, session user lookup, calling `@Service`, and returning Thymeleaf view names. NO SQL and NO business math in Controllers.
  - `@Service` classes contain ALL business logic (weighted scores, taste-match math, binge pace, recommendation scoring, spoiler lock checks, vote tallying).
  - `@Repository` classes ONLY execute explicit SQL using Spring `JdbcTemplate` and `RowMapper`. **DO NOT** introduce Hibernate, JPA (`@Entity`, `JpaRepository`), or ORM frameworks.
  - Thymeleaf templates (`src/main/resources/templates/`) ONLY render model attributes. Reuse `th:replace="~{fragments/navbar :: navbar}"` on every page.
- **Database:** PostgreSQL. Always check `ARCHITECTURE_AND_SCHEMA.md` and `src/main/resources/schema.sql` for exact table and column names before writing any SQL.

## 2. File Isolation & Merge Conflict Prevention (CRITICAL)
- **LOCKED SHARED FILES:** Do NOT modify `pom.xml`, `application.properties`, `schema.sql`, `data.sql`, `SecurityConfig.java`, `User.java`, `MediaItem.java`, `UserRepository.java`, `MediaItemRepository.java`, or `fragments/navbar.html` unless the user explicitly asks to update shared core infrastructure.
- **Read-Only Cross-Feature Access:** If your feature needs data owned by another teammate (e.g., reading `reviews`, `episode_progress`, or `discussion_posts`), inject the existing `*Repository` into your own `@Service`. Never edit another teammate's Controller or Service file.
- **Scoped Work:** Always check the user's personal task file (`docs/TASKS_AYMAN.md`, `docs/TASKS_OBDHI.md`, `docs/TASKS_NOSHIN.md`, or `docs/TASKS_AMIR.md`) and strictly create/edit only the files assigned to that member.

## 3. UI / Styling Guidelines
- Use the existing dark cinema theme in `/css/style.css` (dark slate background `#0f172a`, card surface `#1e293b`, indigo/amber accents).
- Display TMDB posters using: `https://image.tmdb.org/t/p/w500` + `posterPath`.
- Keep HTML clean, responsive, and free of heavy frontend frameworks (Vanilla JS only if needed for sliders, timers, or charts).
