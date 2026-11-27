package com.cinecircle.repository;

import com.cinecircle.model.MediaItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TmdbCatalogRepository {

    private final JdbcTemplate jdbcTemplate;

    public TmdbCatalogRepository(JdbcTemplate jdbcTemplate) {
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

    public boolean insertIfNotExists(MediaItem item) {
        if (item == null || item.getTitle() == null || item.getTitle().isBlank()) {
            return false;
        }
        String sql = "INSERT INTO media_items (tmdb_id, title, media_type, genre, country, release_year, "
                + "runtime_minutes, vibe_tags, poster_path, overview, is_family_safe) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT (tmdb_id) DO NOTHING";
        int rowsAffected = jdbcTemplate.update(
                sql,
                item.getTmdbId(),
                item.getTitle(),
                item.getMediaType(),
                item.getGenre(),
                item.getCountry(),
                item.getReleaseYear(),
                item.getRuntimeMinutes(),
                item.getVibeTags(),
                item.getPosterPath(),
                item.getOverview(),
                item.isFamilySafe());
        return rowsAffected > 0;
    }

    public Optional<MediaItem> findByTmdbId(String tmdbId) {
        if (tmdbId == null || tmdbId.isBlank()) {
            return Optional.empty();
        }
        List<MediaItem> list = jdbcTemplate.query(
                "SELECT * FROM media_items WHERE tmdb_id = ?",
                mediaRowMapper,
                tmdbId.trim());
        return list.stream().findFirst();
    }
}
