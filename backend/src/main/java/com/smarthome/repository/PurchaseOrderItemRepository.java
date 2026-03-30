package com.smarthome.repository;

import com.smarthome.entity.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {
  long countByProduct_Id(Long productId);
}
