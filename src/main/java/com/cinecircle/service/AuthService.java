package com.cinecircle.service;

import com.cinecircle.model.User;
import com.cinecircle.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean registerUser(String name, String email, String rawPassword) {
        String normalizedEmail = UserRepository.normalizeEmail(email);
        if (normalizedEmail == null || normalizedEmail.isEmpty()) {
            return false;
        }
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            return false;
        }
        User user = new User();
        user.setName(name != null ? name.trim() : null);
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole("VIEWER");
        userRepository.save(user);
        return true;
    }
}
