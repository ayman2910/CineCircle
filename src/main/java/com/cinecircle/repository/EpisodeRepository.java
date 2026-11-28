package com.cinecircle.repository;

import com.cinecircle.model.Episode;
import com.cinecircle.model.EpisodeProgress;
import com.cinecircle.model.MediaItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Explicit SQL repository for F7 TV series episodes and user progress tracking.
 */
@Repository
public class EpisodeRepository {

    private final JdbcTemplate jdbcTemplate;

    public EpisodeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<MediaItem> mediaRowMapper = (rs, rowNum) -> {
        MediaItem m = new MediaItem();
        m.setId(rs.getLong("id"));
        m.setTmdbId(rs.getString("tmdb_id"));
        m.setTitle(rs.getString("title"));
        m.setMediaType(rs.getString("media_type"));
        m.setGenre(rs.getString("genre"));
        m.setCountry(rs.getString("country"));
        m.setReleaseYear((Integer) rs.getObject("release_year"));
        m.setRuntimeMinutes(rs.getInt("runtime_minutes"));
        m.setVibeTags(rs.getString("vibe_tags"));
        m.setPosterPath(rs.getString("poster_path"));
        m.setOverview(rs.getString("overview"));
        m.setFamilySafe(rs.getBoolean("is_family_safe"));
        return m;
    };

    private final RowMapper<Episode> episodeRowMapper = (rs, rowNum) -> {
        Episode e = new Episode();
        e.setId(rs.getLong("id"));
        e.setSeriesMediaId(rs.getLong("series_media_id"));
        e.setSeasonNumber(rs.getInt("season_number"));
        e.setEpisodeNumber(rs.getInt("episode_number"));
        e.setTitle(rs.getString("title"));
        e.setRuntimeMinutes(rs.getInt("runtime_minutes"));
        return e;
    };

    private final RowMapper<EpisodeProgress> progressRowMapper = (rs, rowNum) -> {
        EpisodeProgress ep = new EpisodeProgress();
        ep.setId(rs.getLong("id"));
        ep.setUserId(rs.getLong("user_id"));
        ep.setSeriesMediaId(rs.getLong("series_media_id"));
        ep.setHighestSeason(rs.getInt("highest_season"));
        ep.setHighestEpisode(rs.getInt("highest_episode"));
        ep.setWatchStatus(rs.getString("watch_status"));
        Date targetDate = rs.getDate("target_finish_date");
        if (targetDate != null) {
            ep.setTargetFinishDate(targetDate.toLocalDate());
        }
        return ep;
    };

    public List<MediaItem> findAllSeries() {
        String sql = "SELECT * FROM media_items WHERE UPPER(media_type) IN ('SERIES', 'TV') ORDER BY title ASC";
        return jdbcTemplate.query(sql, mediaRowMapper);
    }

    public Optional<MediaItem> findSeriesById(Long seriesMediaId) {
        if (seriesMediaId == null) {
            return Optional.empty();
        }
        String sql = "SELECT * FROM media_items WHERE UPPER(media_type) IN ('SERIES', 'TV') AND id = ?";
        List<MediaItem> list = jdbcTemplate.query(sql, mediaRowMapper, seriesMediaId);
        return list.stream().findFirst();
    }

    public List<Episode> findEpisodesBySeriesId(Long seriesMediaId) {
        if (seriesMediaId == null) {
            return List.of();
        }
        String sql = "SELECT * FROM episodes WHERE series_media_id = ? ORDER BY season_number ASC, episode_number ASC";
        return jdbcTemplate.query(sql, episodeRowMapper, seriesMediaId);
    }

    public int countEpisodesBySeriesId(Long seriesMediaId) {
        if (seriesMediaId == null) {
            return 0;
        }
        String sql = "SELECT COUNT(*) FROM episodes WHERE series_media_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, seriesMediaId);
        return count != null ? count : 0;
    }

    public void upsertEpisodes(List<Episode> episodes) {
        if (episodes == null || episodes.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO episodes (series_media_id, season_number, episode_number, title, runtime_minutes) "
                + "VALUES (?, ?, ?, ?, ?) "
                + "ON CONFLICT (series_media_id, season_number, episode_number) DO UPDATE SET "
                + "title = EXCLUDED.title, runtime_minutes = EXCLUDED.runtime_minutes";

        List<Object[]> batchArgs = new ArrayList<>();
        for (Episode episode : episodes) {
            if (episode == null || episode.getSeriesMediaId() == null) {
                continue;
            }
            String safeTitle = (episode.getTitle() == null || episode.getTitle().isBlank())
                    ? "Episode " + episode.getEpisodeNumber()
                    : episode.getTitle().trim();
            if (safeTitle.length() > 200) {
                safeTitle = safeTitle.substring(0, 200);
            }
            int safeRuntime = episode.getRuntimeMinutes() > 0 ? episode.getRuntimeMinutes() : 45;
            batchArgs.add(new Object[] {
                    episode.getSeriesMediaId(),
                    episode.getSeasonNumber(),
                    episode.getEpisodeNumber(),
                    safeTitle,
                    safeRuntime
            });
        }

        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }
    }

    public List<EpisodeProgress> findAllProgressByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        String sql = "SELECT * FROM episode_progress WHERE user_id = ? ORDER BY id DESC";
        return jdbcTemplate.query(sql, progressRowMapper, userId);
    }

    public Optional<EpisodeProgress> findProgressByUserAndSeries(Long userId, Long seriesMediaId) {
        if (userId == null || seriesMediaId == null) {
            return Optional.empty();
        }
        String sql = "SELECT * FROM episode_progress WHERE user_id = ? AND series_media_id = ?";
        List<EpisodeProgress> list = jdbcTemplate.query(sql, progressRowMapper, userId, seriesMediaId);
        return list.stream().findFirst();
    }

    public void upsertProgress(EpisodeProgress progress) {
        if (progress == null || progress.getUserId() == null || progress.getSeriesMediaId() == null) {
            return;
        }
        String watchStatus = (progress.getWatchStatus() == null || progress.getWatchStatus().isBlank())
                ? "WATCHING"
                : progress.getWatchStatus().trim().toUpperCase(Locale.ROOT);
        Date targetDate = progress.getTargetFinishDate() != null
                ? Date.valueOf(progress.getTargetFinishDate())
                : null;

        String sql = "INSERT INTO episode_progress "
                + "(user_id, series_media_id, highest_season, highest_episode, watch_status, target_finish_date) "
                + "VALUES (?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT (user_id, series_media_id) DO UPDATE SET "
                + "highest_season = EXCLUDED.highest_season, "
                + "highest_episode = EXCLUDED.highest_episode, "
                + "watch_status = EXCLUDED.watch_status, "
                + "target_finish_date = EXCLUDED.target_finish_date";

        jdbcTemplate.update(
                sql,
                progress.getUserId(),
                progress.getSeriesMediaId(),
                progress.getHighestSeason(),
                progress.getHighestEpisode(),
                watchStatus,
                targetDate
        );
    }

    public void deleteProgress(Long userId, Long seriesMediaId) {
        if (userId == null || seriesMediaId == null) {
            return;
        }
        String sql = "DELETE FROM episode_progress WHERE user_id = ? AND series_media_id = ?";
        jdbcTemplate.update(sql, userId, seriesMediaId);
    }
}
