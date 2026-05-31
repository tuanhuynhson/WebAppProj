package com.example.webapp.dao;

import com.example.webapp.model.ConcertLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConcertLocationDao extends JpaRepository<ConcertLocation, Long> {
    List<ConcertLocation> findAllByOrderByConcertDateAscIdAsc();
}
