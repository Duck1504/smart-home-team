package com.smarthome.repository;

import com.smarthome.entity.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
  List<ProductReview> findByProduct_IdOrderBySubmittedAtDescIdDesc(Long productId);
  List<ProductReview> findByProduct_IdIn(Collection<Long> productIds);
  Optional<ProductReview> findByProduct_IdAndUser_Id(Long productId, Long userId);
}
