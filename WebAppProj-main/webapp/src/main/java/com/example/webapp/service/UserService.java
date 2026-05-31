package com.example.webapp.service;

import com.example.webapp.dao.UserDao;
import com.example.webapp.model.User;
import com.example.webapp.model.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByEmail(String email) {
        return userDao.findByEmailIgnoreCase(email);
    }

    public Optional<User> authenticate(String email, String rawPassword) {
        return userDao.findByEmailIgnoreCase(email)
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPasswordHash()));
    }

    @Transactional
    public User registerCustomer(String fullName, String email, String rawPassword) {
        String normalizedEmail = email.trim().toLowerCase();
        if (userDao.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        String passwordHash = passwordEncoder.encode(rawPassword);
        User user = new User(fullName.trim(), normalizedEmail, normalizedEmail, passwordHash, UserRole.CUSTOMER);
        return userDao.save(user);
    }

    @Transactional
    public User createAdminIfMissing(String email, String rawPassword) {
        return userDao.findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    String passwordHash = passwordEncoder.encode(rawPassword);
                    User admin = new User("Administrator", email, email, passwordHash, UserRole.ADMIN);
                    return userDao.save(admin);
                });
    }
}
