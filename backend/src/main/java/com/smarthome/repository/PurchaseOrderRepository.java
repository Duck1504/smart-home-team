package com.smarthome.repository;

import com.smarthome.entity.PurchaseOrder;
import com.smarthome.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
  List<PurchaseOrder> findByUser(User user);
  long countByUser_Id(Long userId);
  List<PurchaseOrder> findTop10ByOrderByCreatedAtDesc();
}

