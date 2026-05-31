package com.example.webapp.dao;

import com.example.webapp.model.SeatSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatSectionDao extends JpaRepository<SeatSection, Long> {

    @Query("""
            select section
            from SeatSection section
            where section.locationId = :locationId
              and section.sectionCode is not null
            order by section.displayOrder asc, section.id asc
            """)
    List<SeatSection> findByLocationIdOrderByDisplayOrderAscIdAsc(@Param("locationId") Long locationId);
}
