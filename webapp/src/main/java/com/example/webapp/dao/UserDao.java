package com.example.webapp.dao;

import com.example.webapp.model.User;
import com.example.webapp.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDao extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsername(String username);

    long countByRole(UserRole role);
}