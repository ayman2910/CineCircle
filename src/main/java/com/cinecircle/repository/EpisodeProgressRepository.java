package com.cinecircle.repository;

import com.cinecircle.model.EpisodeProgress;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EpisodeProgressRepository {

    private final JdbcTemplate jdbcTemplate;

    public EpisodeProgressRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<EpisodeProgress> rowMapper = (rs, rowNum) -> {
        EpisodeProgress ep = new EpisodeProgress();
        ep.setId(rs.getLong("id"));
        ep.setUserId(rs.getLong("user_id"));
        ep.setSeriesMediaId(rs.getLong("series_media_id"));
        ep.setHighestSeason(rs.getInt("highest_season"));
        ep.setHighestEpisode(rs.getInt("highest_episode"));
        ep.setWatchStatus(rs.getString("watch_status"));
        if (rs.getDate("target_finish_date") != null) {
            ep.setTargetFinishDate(rs.getDate("target_finish_date").toLocalDate());
        }
        return ep;
    };

    public Optional<EpisodeProgress> findByUserAndSeries(Long userId, Long seriesMediaId) {
        List<EpisodeProgress> list = jdbcTemplate.query(
                "SELECT * FROM episode_progress WHERE user_id = ? AND series_media_id = ?",
                rowMapper, userId, seriesMediaId);
        return list.stream().findFirst();
    }

    public List<EpisodeProgress> findByUserId(Long userId) {
        return jdbcTemplate.query("SELECT * FROM episode_progress WHERE user_id = ?", rowMapper, userId);
    }
}
