package com.example.webapp.dao;

import com.example.webapp.model.Seat;
import com.example.webapp.model.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatDao extends JpaRepository<Seat, Long> {
    long countByLocationIdAndStatus(Long locationId, SeatStatus status);
}
