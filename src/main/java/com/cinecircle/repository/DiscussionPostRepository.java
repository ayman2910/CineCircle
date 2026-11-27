package com.cinecircle.repository;

import com.cinecircle.model.DiscussionPost;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DiscussionPostRepository {

    private final JdbcTemplate jdbcTemplate;

    public DiscussionPostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<DiscussionPost> rowMapper = (rs, rowNum) -> {
        DiscussionPost p = new DiscussionPost();
        p.setId(rs.getLong("id"));
        p.setAuthorId(rs.getLong("author_id"));
        p.setMediaId(rs.getLong("media_id"));
        p.setCheckpointSeason(rs.getInt("checkpoint_season"));
        p.setCheckpointEpisode(rs.getInt("checkpoint_episode"));
        p.setContent(rs.getString("content"));
        p.setReportCount(rs.getInt("report_count"));
        p.setQuarantined(rs.getBoolean("is_quarantined"));
        if (rs.getTimestamp("created_at") != null) {
            p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return p;
    };

    public List<DiscussionPost> findByMediaId(Long mediaId) {
        return jdbcTemplate.query(
                "SELECT * FROM discussion_posts WHERE media_id = ? ORDER BY checkpoint_season, checkpoint_episode, created_at DESC",
                rowMapper, mediaId);
    }
}
