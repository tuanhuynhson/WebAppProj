package com.example.webapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "concert_locations")
public class ConcertLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String city;

    @Column(name = "venue_name")
    private String venueName;

    private String country;

    private String address;

    @Column(name = "concert_date")
    private LocalDateTime concertDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    public Long getId() {
        return id;
    }

    public String getCity() {
        return city;
    }

    public String getVenueName() {
        return venueName;
    }

    public String getCountry() {
        return country;
    }

    public String getAddress() {
        return address;
    }

    public LocalDateTime getConcertDate() {
        return concertDate;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
