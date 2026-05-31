package com.example.webapp.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "merch_order")
public class MerchOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String customerName;
    private String address;
    private Double totalAmount;
    private String status = "Pending";
    
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

}