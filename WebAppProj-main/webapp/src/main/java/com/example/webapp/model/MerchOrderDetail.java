package com.example.webapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "merch_order_detail")
public class MerchOrderDetail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private MerchOrder order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private MerchProduct product;

    private Integer quantity;
    private Double price;
}