-- Seed Users (Password for all test accounts is: password123)
INSERT INTO users (name, email, password_hash, role, trust_score) VALUES
('Hossain Ayman', 'ayman@cinecircle.com', '$2a$10$pDw4qdkndd/DVNVsEGdj8eCMyGOuUXyWFBvFV8hsh1riYMcsw8UwS', 'ADMIN', 100),
('Suhaila Obdhi', 'obdhi@cinecircle.com', '$2a$10$pDw4qdkndd/DVNVsEGdj8eCMyGOuUXyWFBvFV8hsh1riYMcsw8UwS', 'MODERATOR', 100),
('Noshin Noor', 'noshin@cinecircle.com', '$2a$10$pDw4qdkndd/DVNVsEGdj8eCMyGOuUXyWFBvFV8hsh1riYMcsw8UwS', 'HOST', 100),
('Amir Abrar Akib', 'amir@cinecircle.com', '$2a$10$pDw4qdkndd/DVNVsEGdj8eCMyGOuUXyWFBvFV8hsh1riYMcsw8UwS', 'VIEWER', 100)
ON CONFLICT (email) DO NOTHING;

-- Seed User Subscriptions
INSERT INTO user_subscriptions (user_id, platform_name) VALUES
(1, 'Netflix'), (1, 'Prime Video'), (1, 'Chorki'),
(2, 'Netflix'), (2, 'Hoichoi'),
(3, 'Netflix'), (3, 'Prime Video'),
(4, 'Prime Video')
ON CONFLICT DO NOTHING;

-- Seed Movies & TV Series
INSERT INTO media_items (tmdb_id, title, media_type, genre, country, release_year, runtime_minutes, vibe_tags, poster_path, overview, is_family_safe) VALUES
('489999', 'Searching', 'MOVIE', 'Thriller', 'USA', 2018, 102, 'Fast-Paced Thriller,Whodunit,Edge of Your Seat', '/pk9R56G23aD83c63W51w218f8sD.jpg', 'After his 16-year-old daughter goes missing, a desperate father breaks into her laptop to look for clues.', TRUE),
('122906', 'About Time', 'MOVIE', 'Romance', 'UK', 2013, 123, 'Feel-Good,Comfort Watch,Heartwarming', '/iRRI9BG3e12dF2g5f02x1t3R7wU.jpg', 'At the age of 21, Tim discovers he can travel in time and change what happens in his own life.', TRUE),
('334543', 'Piku', 'MOVIE', 'Comedy', 'India', 2015, 123, 'Feel-Good Road Trip,Comfort Watch,Slice of Life', '/vB7pS3k9M052X1h043188x7t2P.jpg', 'A quirky road trip from Delhi to Kolkata brings an architect closer to her eccentric father.', TRUE),
('348892', 'Drishyam', 'MOVIE', 'Thriller', 'India', 2015, 163, 'Fast-Paced Thriller,Whodunit,Smart Plot', '/j6M8S1N32V8a6F60W5r132934h.jpg', 'Desperate measures are taken by a man who tries to save his family from the dark side of the law.', TRUE),
('79698', 'Zindagi Na Milegi Dobara', 'MOVIE', 'Drama', 'India', 2011, 155, 'Feel-Good Road Trip,Friendship,Comfort Watch', '/h5w6Q2t0N510f7f2g7x03j4c5W.jpg', 'Three friends decide to turn their fantasy vacation in Spain into reality after one gets engaged.', TRUE),
('138832', 'We''re the Millers', 'MOVIE', 'Comedy', 'USA', 2013, 110, 'Laugh Out Loud,Road Trip,Fast-Paced', '/qF2LJ0opmnTHwjhb4zF54uN5R5.jpg', 'A veteran pot dealer creates a fake family as part of his plan to move a huge shipment across the border.', FALSE),
('78191', 'You', 'SERIES', 'Thriller', 'USA', 2018, 45, 'Fast-Paced Thriller,Psychological,Binge-Worthy', '/7bEYwjUvlJW7GerM8GYmqwl4oS3.jpg', 'A dangerously charming, intensely obsessive young man goes to extreme measures to insert himself into the lives of those he is transfixed by.', FALSE),
('95479', 'Jujutsu Kaisen', 'SERIES', 'Anime', 'Japan', 2020, 24, 'Fast-Paced Action,Supernatural,Hype', '/hFWP5HkbVEe40hrXgtCeQxoccHE.jpg', 'A boy swallows a cursed talisman and becomes cursed himself, entering a shaman school to locate the demon''s other body parts.', FALSE)
ON CONFLICT (tmdb_id) DO NOTHING;

-- Seed Streaming Availability
INSERT INTO streaming_availability (media_id, platform_name, access_type, price, leaving_date) VALUES
(1, 'Netflix', 'SUBSCRIPTION', 0.00, '2026-12-15'),
(2, 'Prime Video', 'SUBSCRIPTION', 0.00, NULL),
(3, 'Hoichoi', 'SUBSCRIPTION', 0.00, NULL),
(3, 'Prime Video', 'SUBSCRIPTION', 0.00, '2026-12-05'),
(4, 'Netflix', 'SUBSCRIPTION', 0.00, NULL),
(5, 'Netflix', 'SUBSCRIPTION', 0.00, NULL),
(6, 'Netflix', 'SUBSCRIPTION', 0.00, '2026-12-20'),
(7, 'Netflix', 'SUBSCRIPTION', 0.00, NULL),
(8, 'Netflix', 'SUBSCRIPTION', 0.00, NULL)
ON CONFLICT DO NOTHING;

-- Seed Sample Episodes
INSERT INTO episodes (series_media_id, season_number, episode_number, title, runtime_minutes) VALUES
(7, 1, 1, 'Pilot', 48),
(7, 1, 2, 'The Last Nice Guy in New York', 45),
(7, 1, 3, 'Maybe', 46),
(7, 1, 4, 'The Captain', 45),
(8, 1, 1, 'Ryomen Sukuna', 24),
(8, 1, 2, 'For Myself', 24),
(8, 1, 3, 'Girl of Steel', 24),
(8, 1, 4, 'Curse Womb Must Die', 24)
ON CONFLICT DO NOTHING;

-- Seed Sample Reviews
INSERT INTO reviews (user_id, media_id, story_score, acting_score, visuals_score, music_score, rewatch_score, overall_score, review_text, is_rewatch, watched_date) VALUES
(1, 1, 9, 9, 8, 8, 9, 8.70, 'Super gripping thriller from minute one!', FALSE, '2026-11-10'),
(2, 1, 9, 8, 8, 7, 8, 8.15, 'Kept me guessing until the very end.', FALSE, '2026-11-12'),
(3, 1, 8, 8, 9, 8, 7, 8.00, 'Clever screenlife format and great pacing.', FALSE, '2026-11-14'),
(1, 3, 9, 10, 8, 9, 10, 9.25, 'Ultimate comfort movie, Irrfan Khan is phenomenal.', TRUE, '2026-11-15'),
(2, 2, 9, 9, 9, 10, 9, 9.15, 'Heartwarming and emotional.', FALSE, '2026-11-16');

-- Seed Sample Episode Progress
INSERT INTO episode_progress (user_id, series_media_id, highest_season, highest_episode, watch_status, target_finish_date) VALUES
(1, 7, 1, 2, 'WATCHING', '2026-12-10'),
(2, 7, 1, 4, 'WATCHING', '2026-12-15')
ON CONFLICT DO NOTHING;

-- Seed Sample Watch Challenges
INSERT INTO watch_challenges (title, description, required_genre, max_runtime_rule, required_count, badge_name) VALUES
('Fast Thriller Sprint', 'Watch 3 Thrillers under 120 minutes this month.', 'Thriller', 120, 3, 'Speed Sleuth'),
('Comfort Watch Club', 'Log 3 Feel-Good or Comedy movies.', 'Comedy', 150, 3, 'Cozy Cinephile');
