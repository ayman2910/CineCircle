-- ============================================================
-- CineCircle PostgreSQL Schema (CSE470 Group 4)
-- Shared across all 4 team members (Explicit SQL / JdbcTemplate)
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'VIEWER',
    trust_score INT NOT NULL DEFAULT 100,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_subscriptions (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    platform_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, platform_name)
);

CREATE TABLE IF NOT EXISTS media_items (
    id BIGSERIAL PRIMARY KEY,
    tmdb_id VARCHAR(50) UNIQUE,
    title VARCHAR(200) NOT NULL,
    media_type VARCHAR(20) NOT NULL DEFAULT 'MOVIE',
    genre VARCHAR(100) NOT NULL,
    country VARCHAR(80) DEFAULT 'USA',
    release_year INT,
    runtime_minutes INT NOT NULL DEFAULT 120,
    vibe_tags VARCHAR(255),
    poster_path VARCHAR(255),
    overview TEXT,
    is_family_safe BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS streaming_availability (
    id BIGSERIAL PRIMARY KEY,
    media_id BIGINT NOT NULL REFERENCES media_items(id) ON DELETE CASCADE,
    platform_name VARCHAR(50) NOT NULL,
    access_type VARCHAR(30) NOT NULL DEFAULT 'SUBSCRIPTION',
    price DECIMAL(8,2) DEFAULT 0.00,
    leaving_date DATE
);

CREATE TABLE IF NOT EXISTS episodes (
    id BIGSERIAL PRIMARY KEY,
    series_media_id BIGINT NOT NULL REFERENCES media_items(id) ON DELETE CASCADE,
    season_number INT NOT NULL,
    episode_number INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    runtime_minutes INT NOT NULL DEFAULT 45,
    UNIQUE (series_media_id, season_number, episode_number)
);

CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    media_id BIGINT NOT NULL REFERENCES media_items(id) ON DELETE CASCADE,
    story_score INT NOT NULL CHECK (story_score BETWEEN 1 AND 10),
    acting_score INT NOT NULL CHECK (acting_score BETWEEN 1 AND 10),
    visuals_score INT NOT NULL CHECK (visuals_score BETWEEN 1 AND 10),
    music_score INT NOT NULL CHECK (music_score BETWEEN 1 AND 10),
    rewatch_score INT NOT NULL CHECK (rewatch_score BETWEEN 1 AND 10),
    overall_score DOUBLE PRECISION NOT NULL,
    review_text TEXT,
    is_rewatch BOOLEAN NOT NULL DEFAULT FALSE,
    watched_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS episode_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    series_media_id BIGINT NOT NULL REFERENCES media_items(id) ON DELETE CASCADE,
    highest_season INT NOT NULL DEFAULT 1,
    highest_episode INT NOT NULL DEFAULT 0,
    watch_status VARCHAR(20) NOT NULL DEFAULT 'WATCHING',
    target_finish_date DATE,
    UNIQUE (user_id, series_media_id)
);

CREATE TABLE IF NOT EXISTS watchlist_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    media_id BIGINT NOT NULL REFERENCES media_items(id) ON DELETE CASCADE,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_interacted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    priority_score DOUBLE PRECISION DEFAULT 0.0,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (user_id, media_id)
);

CREATE TABLE IF NOT EXISTS content_advisory_votes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    media_id BIGINT NOT NULL REFERENCES media_items(id) ON DELETE CASCADE,
    warning_category VARCHAR(50) NOT NULL,
    is_safe_vote BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (user_id, media_id, warning_category)
);

CREATE TABLE IF NOT EXISTS discussion_posts (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    media_id BIGINT NOT NULL REFERENCES media_items(id) ON DELETE CASCADE,
    checkpoint_season INT NOT NULL DEFAULT 0,
    checkpoint_episode INT NOT NULL DEFAULT 0,
    content TEXT NOT NULL,
    report_count INT NOT NULL DEFAULT 0,
    is_quarantined BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS spoiler_reports (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES discussion_posts(id) ON DELETE CASCADE,
    reporter_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reason VARCHAR(255),
    resolution_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (post_id, reporter_id)
);

CREATE TABLE IF NOT EXISTS franchise_guides (
    id BIGSERIAL PRIMARY KEY,
    franchise_name VARCHAR(150) NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS franchise_guide_items (
    id BIGSERIAL PRIMARY KEY,
    franchise_id BIGINT NOT NULL REFERENCES franchise_guides(id) ON DELETE CASCADE,
    media_id BIGINT NOT NULL REFERENCES media_items(id) ON DELETE CASCADE,
    release_order INT NOT NULL,
    chronological_order INT NOT NULL
);

CREATE TABLE IF NOT EXISTS custom_lists (
    id BIGSERIAL PRIMARY KEY,
    creator_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    list_type VARCHAR(20) NOT NULL DEFAULT 'RANKED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS custom_list_items (
    id BIGSERIAL PRIMARY KEY,
    list_id BIGINT NOT NULL REFERENCES custom_lists(id) ON DELETE CASCADE,
    media_id BIGINT NOT NULL REFERENCES media_items(id) ON DELETE CASCADE,
    rank_position INT,
    tier_label VARCHAR(5)
);

CREATE TABLE IF NOT EXISTS group_lobbies (
    id BIGSERIAL PRIMARY KEY,
    host_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    room_code VARCHAR(12) UNIQUE NOT NULL,
    max_runtime_cap INT DEFAULT 180,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    winning_media_id BIGINT REFERENCES media_items(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS lobby_members (
    lobby_id BIGINT NOT NULL REFERENCES group_lobbies(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (lobby_id, user_id)
);

CREATE TABLE IF NOT EXISTS lobby_votes (
    id BIGSERIAL PRIMARY KEY,
    lobby_id BIGINT NOT NULL REFERENCES group_lobbies(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    media_id BIGINT NOT NULL REFERENCES media_items(id) ON DELETE CASCADE,
    vote_type VARCHAR(10) NOT NULL,
    UNIQUE (lobby_id, user_id, media_id)
);

CREATE TABLE IF NOT EXISTS recommendation_prompts (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    prompt_text TEXT NOT NULL,
    desired_vibe VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS recommendation_replies (
    id BIGSERIAL PRIMARY KEY,
    prompt_id BIGINT NOT NULL REFERENCES recommendation_prompts(id) ON DELETE CASCADE,
    responder_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    suggested_media_id BIGINT NOT NULL REFERENCES media_items(id) ON DELETE CASCADE,
    comment_text TEXT,
    upvotes INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS buddy_watches (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    media_id BIGINT NOT NULL REFERENCES media_items(id) ON DELETE CASCADE,
    deadline DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'PROPOSED',
    is_comparison_unlocked BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS movie_clubs (
    id BIGSERIAL PRIMARY KEY,
    moderator_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    club_name VARCHAR(150) NOT NULL,
    theme VARCHAR(150),
    current_movie_id BIGINT REFERENCES media_items(id),
    cycle_state VARCHAR(20) NOT NULL DEFAULT 'NOMINATION'
);

CREATE TABLE IF NOT EXISTS movie_club_members (
    club_id BIGINT NOT NULL REFERENCES movie_clubs(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (club_id, user_id)
);

CREATE TABLE IF NOT EXISTS tournament_brackets (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    current_round INT NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS bracket_matchups (
    id BIGSERIAL PRIMARY KEY,
    bracket_id BIGINT NOT NULL REFERENCES tournament_brackets(id) ON DELETE CASCADE,
    round_number INT NOT NULL,
    media_a_id BIGINT REFERENCES media_items(id),
    media_b_id BIGINT REFERENCES media_items(id),
    votes_a INT NOT NULL DEFAULT 0,
    votes_b INT NOT NULL DEFAULT 0,
    winner_media_id BIGINT REFERENCES media_items(id)
);

CREATE TABLE IF NOT EXISTS watch_challenges (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    required_genre VARCHAR(80),
    max_runtime_rule INT,
    required_count INT NOT NULL DEFAULT 3,
    badge_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS user_challenges (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    challenge_id BIGINT NOT NULL REFERENCES watch_challenges(id) ON DELETE CASCADE,
    completed_count INT NOT NULL DEFAULT 0,
    badge_unlocked BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (user_id, challenge_id)
);
