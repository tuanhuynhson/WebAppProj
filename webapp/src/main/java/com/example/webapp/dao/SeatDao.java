package com.example.webapp.dao;

import com.example.webapp.model.Seat;
import com.example.webapp.model.SeatStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatDao extends JpaRepository<Seat, Long> {

    @Query("""
            select count(seat)
            from Seat seat, SeatSection section
            where seat.sectionId = section.id
              and seat.locationId = :locationId
              and seat.status = :status
              and section.sectionCode is not null
            """)
    long countByLocationIdAndStatus(@Param("locationId") Long locationId, @Param("status") SeatStatus status);

    long countByStatus(SeatStatus status);

    @Query("""
            select count(seat)
            from Seat seat, SeatSection section
            where seat.sectionId = section.id
              and section.sectionCode is not null
            """)
    long countValidSeats();

    @Query("""
            select count(seat)
            from Seat seat, SeatSection section
            where seat.sectionId = section.id
              and seat.status = :status
              and section.sectionCode is not null
            """)
    long countValidByStatus(@Param("status") SeatStatus status);

    long countBySectionIdAndStatus(Long sectionId, SeatStatus status);

    long countBySectionId(Long sectionId);

    List<Seat> findAllByOrderByIdAsc();

    @Query("""
            select seat
            from Seat seat, SeatSection section
            where seat.sectionId = section.id
              and seat.userId = :userId
              and seat.status = :status
              and section.sectionCode is not null
            order by seat.id asc
            """)
    List<Seat> findByUserIdAndStatusOrderByIdAsc(@Param("userId") Long userId, @Param("status") SeatStatus status);

    List<Seat> findBySectionIdAndStatusOrderByIdAsc(Long sectionId, SeatStatus status, Pageable pageable);
}
