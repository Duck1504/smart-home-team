package com.smarthome.service;

import com.smarthome.dto.product.ProductReviewDto;
import com.smarthome.dto.product.ProductReviewRequest;
import com.smarthome.entity.Product;
import com.smarthome.entity.ProductReview;
import com.smarthome.entity.User;
import com.smarthome.exception.ApiException;
import com.smarthome.repository.ProductRepository;
import com.smarthome.repository.ProductReviewRepository;
import com.smarthome.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductReviewService {
  private final ProductReviewRepository productReviewRepository;
  private final ProductRepository productRepository;
  private final UserRepository userRepository;

  public ProductReviewService(
    ProductReviewRepository productReviewRepository,
    ProductRepository productRepository,
    UserRepository userRepository
  ) {
    this.productReviewRepository = productReviewRepository;
    this.productRepository = productRepository;
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public List<ProductReviewDto> listByProductId(Long productId) {
    return productReviewRepository.findByProduct_IdOrderBySubmittedAtDescIdDesc(productId).stream()
      .map(this::toDto)
      .toList();
  }

  @Transactional(readOnly = true)
  public Map<Long, RatingSummary> getSummariesByProductIds(Collection<Long> productIds) {
    if (productIds == null || productIds.isEmpty()) {
      return Collections.emptyMap();
    }

    Map<Long, MutableSummary> summaries = new HashMap<>();
    for (ProductReview review : productReviewRepository.findByProduct_IdIn(productIds)) {
      summaries
        .computeIfAbsent(review.getProduct().getId(), ignored -> new MutableSummary())
        .accept(review.getRating());
    }

    Map<Long, RatingSummary> result = new HashMap<>();
    for (Map.Entry<Long, MutableSummary> entry : summaries.entrySet()) {
      result.put(entry.getKey(), entry.getValue().toSummary());
    }
    return result;
  }

  @Transactional
  public ProductReviewDto upsertReview(Long productId, String username, ProductReviewRequest request) {
    Product product = productRepository.findById(productId)
      .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Product not found"));

    User user = userRepository.findByUsername(username)
      .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "User not found"));

    ProductReview review = productReviewRepository.findByProduct_IdAndUser_Id(productId, user.getId())
      .orElseGet(ProductReview::new);

    review.setProduct(product);
    review.setUser(user);
    review.setRating(request.getRating());
    review.setComment(normalizeComment(request.getComment()));
    review.setSubmittedAt(Instant.now());

    return toDto(productReviewRepository.save(review));
  }

  private String normalizeComment(String comment) {
    if (comment == null) {
      return null;
    }

    String normalized = comment.trim();
    return normalized.isEmpty() ? null : normalized;
  }

  private ProductReviewDto toDto(ProductReview review) {
    return new ProductReviewDto(
      review.getId(),
      review.getUser().getUsername(),
      review.getRating(),
      review.getComment(),
      review.getSubmittedAt()
    );
  }

  public static RatingSummary emptySummary() {
    return new RatingSummary(BigDecimal.ZERO, 0L);
  }

  public static class RatingSummary {
    private final BigDecimal averageRating;
    private final long reviewCount;

    public RatingSummary(BigDecimal averageRating, long reviewCount) {
      this.averageRating = averageRating;
      this.reviewCount = reviewCount;
    }

    public BigDecimal getAverageRating() {
      return averageRating;
    }

    public long getReviewCount() {
      return reviewCount;
    }
  }

  private static class MutableSummary {
    private long totalRating;
    private long reviewCount;

    private void accept(Integer rating) {
      totalRating += rating == null ? 0 : rating;
      reviewCount += 1;
    }

    private RatingSummary toSummary() {
      if (reviewCount == 0) {
        return emptySummary();
      }

      return new RatingSummary(
        BigDecimal.valueOf(totalRating)
          .divide(BigDecimal.valueOf(reviewCount), 1, RoundingMode.HALF_UP),
        reviewCount
      );
    }
  }
}
