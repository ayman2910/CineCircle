package com.cinecircle;

import com.cinecircle.config.SecurityConfig;
import com.cinecircle.model.User;
import com.cinecircle.repository.UserRepository;
import com.cinecircle.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthFlowTest {

    @Test
    void seededUsersInDataSqlHaveValidBcryptHashForPassword123() throws Exception {
        String dataSql = Files.readString(Path.of("src/main/resources/data.sql"));
        Pattern seedPattern = Pattern.compile("\\('([^']+)',\\s*'([^']+@cinecircle\\.com)',\\s*'(\\$2[aby]\\$\\d{2}\\$[^']+)',\\s*'([A-Z]+)',\\s*100\\)");
        Matcher matcher = seedPattern.matcher(dataSql);

        PasswordEncoder encoder = new SecurityConfig().passwordEncoder();
        int matchedRows = 0;
        while (matcher.find()) {
            matchedRows++;
            String email = matcher.group(2);
            String hash = matcher.group(3);
            assertTrue(encoder.matches("password123", hash),
                    "Seed hash in data.sql does not match 'password123' for " + email);
        }
        assertEquals(4, matchedRows, "Expected 4 seeded users in data.sql");
    }

    @Test
    void userDetailsServiceNormalizesEmailAndAuthenticatesSeedHash() {
        UserRepository userRepository = mock(UserRepository.class);
        SecurityConfig securityConfig = new SecurityConfig();
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        User ayman = new User();
        ayman.setId(1L);
        ayman.setName("Hossain Ayman");
        ayman.setEmail("ayman@cinecircle.com");
        ayman.setPasswordHash("$2a$10$pDw4qdkndd/DVNVsEGdj8eCMyGOuUXyWFBvFV8hsh1riYMcsw8UwS");
        ayman.setRole("ADMIN");

        when(userRepository.findByEmail("ayman@cinecircle.com")).thenReturn(Optional.of(ayman));

        UserDetailsService uds = securityConfig.userDetailsService(userRepository);
        UserDetails details = uds.loadUserByUsername("  AYMAN@CineCircle.COM  ");

        assertEquals("ayman@cinecircle.com", details.getUsername());
        assertTrue(encoder.matches("password123", details.getPassword()));
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        verify(userRepository).findByEmail("ayman@cinecircle.com");

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(uds);
        authProvider.setPasswordEncoder(encoder);

        Authentication auth = authProvider.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated("  AYMAN@CineCircle.COM  ", "password123"));
        assertTrue(auth.isAuthenticated());
        assertEquals("ayman@cinecircle.com", auth.getName());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        assertThrows(BadCredentialsException.class, () -> authProvider.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated("ayman@cinecircle.com", "wrongpassword")));
    }

    @Test
    void userDetailsServiceThrowsOnBlankEmail() {
        UserRepository userRepository = mock(UserRepository.class);
        UserDetailsService uds = new SecurityConfig().userDetailsService(userRepository);

        assertThrows(UsernameNotFoundException.class, () -> uds.loadUserByUsername("   "));
        verifyNoInteractions(userRepository);
    }

    @Test
    void authServiceNormalizesEmailOnRegisterAndDuplicateCheck() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder encoder = new SecurityConfig().passwordEncoder();
        AuthService authService = new AuthService(userRepository, encoder);

        when(userRepository.findByEmail("newuser@cinecircle.com")).thenReturn(Optional.empty());

        boolean created = authService.registerUser("  New User  ", "  NewUser@CineCircle.COM ", "secret123");
        assertTrue(created);
        verify(userRepository).findByEmail("newuser@cinecircle.com");
        verify(userRepository).save(argThat(u ->
                "New User".equals(u.getName())
                        && "newuser@cinecircle.com".equals(u.getEmail())
                        && "VIEWER".equals(u.getRole())
                        && encoder.matches("secret123", u.getPasswordHash())
        ));

        User existing = new User();
        existing.setEmail("newuser@cinecircle.com");
        when(userRepository.findByEmail("newuser@cinecircle.com")).thenReturn(Optional.of(existing));

        boolean duplicate = authService.registerUser("New User", "NEWUSER@cinecircle.com ", "secret123");
        assertFalse(duplicate);
    }

    @Test
    void userRepositoryNormalizesEmailInFindAndSave() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        UserRepository repo = new UserRepository(jdbcTemplate);

        User stored = new User();
        stored.setEmail("ayman@cinecircle.com");
        when(jdbcTemplate.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<User>>any(), eq("ayman@cinecircle.com")))
                .thenReturn(List.of(stored));

        Optional<User> found = repo.findByEmail("  Ayman@CineCircle.com ");
        assertTrue(found.isPresent());
        verify(jdbcTemplate).query(
                eq("SELECT * FROM users WHERE LOWER(TRIM(email)) = ?"),
                org.mockito.ArgumentMatchers.<RowMapper<User>>any(),
                eq("ayman@cinecircle.com"));

        assertTrue(repo.findByEmail("   ").isEmpty());

        User toSave = new User();
        toSave.setName("Test User");
        toSave.setEmail("  TEST@Example.COM ");
        toSave.setPasswordHash("hashed");
        toSave.setRole("VIEWER");

        repo.save(toSave);
        assertEquals("test@example.com", toSave.getEmail());
        verify(jdbcTemplate).update(
                eq("INSERT INTO users (name, email, password_hash, role, trust_score) VALUES (?, ?, ?, ?, ?)"),
                eq("Test User"),
                eq("test@example.com"),
                eq("hashed"),
                eq("VIEWER"),
                eq(100));
    }
}
