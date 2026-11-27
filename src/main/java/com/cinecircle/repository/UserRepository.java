package com.cinecircle.repository;

import com.cinecircle.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Normalizes an email address by trimming surrounding whitespace and lowercasing with {@link Locale#ROOT}.
     */
    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        u.setTrustScore(rs.getInt("trust_score"));
        if (rs.getTimestamp("created_at") != null) {
            u.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return u;
    };

    public Optional<User> findByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null || normalizedEmail.isEmpty()) {
            return Optional.empty();
        }
        List<User> users = jdbcTemplate.query(
                "SELECT * FROM users WHERE LOWER(TRIM(email)) = ?",
                userRowMapper,
                normalizedEmail);
        return users.stream().findFirst();
    }

    public Optional<User> findById(Long id) {
        List<User> users = jdbcTemplate.query("SELECT * FROM users WHERE id = ?", userRowMapper, id);
        return users.stream().findFirst();
    }

    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM users ORDER BY id", userRowMapper);
    }

    public void save(User user) {
        String normalizedEmail = normalizeEmail(user.getEmail());
        user.setEmail(normalizedEmail);
        String sql = "INSERT INTO users (name, email, password_hash, role, trust_score) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, user.getName(), normalizedEmail, user.getPasswordHash(),
                user.getRole() != null ? user.getRole() : "VIEWER", 100);
    }
}
