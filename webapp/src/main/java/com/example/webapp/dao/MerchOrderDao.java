package com.example.webapp.dao;

import com.example.webapp.model.MerchOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MerchOrderDao extends JpaRepository<MerchOrder, Long> {
    List<MerchOrder> findByUserIdOrderByCreatedAtDesc(Long userId);
}