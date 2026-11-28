-- ============================================================
-- Feature F7 Fallback Seed Data (Neon PostgreSQL Idempotent)
-- Seeds Seasons 1 & 2 episodes for starter TV series in data.sql
-- ============================================================

-- 1. You (Seasons 1 & 2, 8 episodes each)
INSERT INTO episodes (series_media_id, season_number, episode_number, title, runtime_minutes)
SELECT m.id, v.season_number, v.episode_number, v.title, v.runtime_minutes
FROM media_items m
CROSS JOIN (VALUES
    (1, 1, 'Pilot', 48),
    (1, 2, 'The Last Nice Guy in New York', 45),
    (1, 3, 'Maybe', 46),
    (1, 4, 'The Captain', 45),
    (1, 5, 'Living with the Enemy', 45),
    (1, 6, 'Amour Fou', 44),
    (1, 7, 'Everythingship', 44),
    (1, 8, 'You Got Me, Babe', 46),
    (2, 1, 'A Fresh Start', 50),
    (2, 2, 'Just the Tip', 47),
    (2, 3, 'What Are Friends For?', 45),
    (2, 4, 'The Good, the Bad, & the Hendy', 46),
    (2, 5, 'Have a Good Wellkend, Joe!', 49),
    (2, 6, 'Farewell, My Bunny', 44),
    (2, 7, 'Ex-istential Crisis', 48),
    (2, 8, 'Fear and Loathing in Beverly Hills', 49)
) AS v(season_number, episode_number, title, runtime_minutes)
WHERE LOWER(m.title) = LOWER('You')
  AND UPPER(m.media_type) IN ('SERIES', 'TV')
ON CONFLICT (series_media_id, season_number, episode_number) DO NOTHING;

-- 2. Jujutsu Kaisen (Seasons 1 & 2, 8 episodes each)
INSERT INTO episodes (series_media_id, season_number, episode_number, title, runtime_minutes)
SELECT m.id, v.season_number, v.episode_number, v.title, v.runtime_minutes
FROM media_items m
CROSS JOIN (VALUES
    (1, 1, 'Ryomen Sukuna', 24),
    (1, 2, 'For Myself', 24),
    (1, 3, 'Girl of Steel', 24),
    (1, 4, 'Curse Womb Must Die', 24),
    (1, 5, 'Curse Womb Must Die -II-', 24),
    (1, 6, 'After Rain', 24),
    (1, 7, 'Assault', 24),
    (1, 8, 'Boredom', 24),
    (2, 1, 'Hidden Inventory', 24),
    (2, 2, 'Hidden Inventory 2', 24),
    (2, 3, 'Hidden Inventory 3', 24),
    (2, 4, 'Hidden Inventory 4', 24),
    (2, 5, 'Premature Death', 24),
    (2, 6, 'It''s Like That', 24),
    (2, 7, 'Evening Festival', 24),
    (2, 8, 'The Shibuya Incident', 24)
) AS v(season_number, episode_number, title, runtime_minutes)
WHERE LOWER(m.title) = LOWER('Jujutsu Kaisen')
  AND UPPER(m.media_type) IN ('SERIES', 'TV')
ON CONFLICT (series_media_id, season_number, episode_number) DO NOTHING;
