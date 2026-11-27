package com.cinecircle.repository;

import com.cinecircle.model.Review;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReviewRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReviewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Review> reviewRowMapper = (rs, rowNum) -> {
        Review r = new Review();
        r.setId(rs.getLong("id"));
        r.setUserId(rs.getLong("user_id"));
        r.setMediaId(rs.getLong("media_id"));
        r.setStoryScore(rs.getInt("story_score"));
        r.setActingScore(rs.getInt("acting_score"));
        r.setVisualsScore(rs.getInt("visuals_score"));
        r.setMusicScore(rs.getInt("music_score"));
        r.setRewatchScore(rs.getInt("rewatch_score"));
        r.setOverallScore(rs.getDouble("overall_score"));
        r.setReviewText(rs.getString("review_text"));
        r.setRewatch(rs.getBoolean("is_rewatch"));
        if (rs.getDate("watched_date") != null) {
            r.setWatchedDate(rs.getDate("watched_date").toLocalDate());
        }
        return r;
    };

    public List<Review> findByMediaId(Long mediaId) {
        return jdbcTemplate.query("SELECT * FROM reviews WHERE media_id = ? ORDER BY created_at DESC", reviewRowMapper, mediaId);
    }

    public List<Review> findByUserId(Long userId) {
        return jdbcTemplate.query("SELECT * FROM reviews WHERE user_id = ? ORDER BY watched_date DESC", reviewRowMapper, userId);
    }

    public void save(Review r) {
        String sql = "INSERT INTO reviews (user_id, media_id, story_score, acting_score, visuals_score, music_score, rewatch_score, overall_score, review_text, is_rewatch, watched_date) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, r.getUserId(), r.getMediaId(), r.getStoryScore(), r.getActingScore(),
                r.getVisualsScore(), r.getMusicScore(), r.getRewatchScore(), r.getOverallScore(),
                r.getReviewText(), r.isRewatch(), r.getWatchedDate());
    }
}
