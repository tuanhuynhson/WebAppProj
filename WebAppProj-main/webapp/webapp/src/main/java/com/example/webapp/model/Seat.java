package com.example.webapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "section_id")
    private Long sectionId;

    @Column(name = "row_label")
    private String rowLabel;

    @Column(name = "seat_number")
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    public Long getId() {
        return id;
    }

    public Long getLocationId() {
        return locationId;
    }

    public Long getSectionId() {
        return sectionId;
    }

    public String getRowLabel() {
        return rowLabel;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public SeatStatus getStatus() {
        return status;
    }
}
