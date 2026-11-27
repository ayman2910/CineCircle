package com.cinecircle.repository;

import com.cinecircle.model.MediaItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MediaItemRepository {

    private final JdbcTemplate jdbcTemplate;

    public MediaItemRepository(JdbcTemplate jdbcTemplate) {
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

    public List<MediaItem> findAll() {
        return jdbcTemplate.query("SELECT * FROM media_items ORDER BY id", mediaRowMapper);
    }

    public Optional<MediaItem> findById(Long id) {
        List<MediaItem> list = jdbcTemplate.query("SELECT * FROM media_items WHERE id = ?", mediaRowMapper, id);
        return list.stream().findFirst();
    }

    public List<MediaItem> findByType(String mediaType) {
        return jdbcTemplate.query("SELECT * FROM media_items WHERE media_type = ? ORDER BY title", mediaRowMapper, mediaType);
    }

    public void updatePosterPath(Long id, String posterPath) {
        jdbcTemplate.update("UPDATE media_items SET poster_path = ? WHERE id = ?", posterPath, id);
    }
}
