package com.example.webapp.dao;

import com.example.webapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDao extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
}
