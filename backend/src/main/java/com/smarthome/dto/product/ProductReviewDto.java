package com.smarthome.dto.product;

import java.time.Instant;

public class ProductReviewDto {
  private Long id;
  private String username;
  private Integer rating;
  private String comment;
  private Instant submittedAt;

  public ProductReviewDto(Long id, String username, Integer rating, String comment, Instant submittedAt) {
    this.id = id;
    this.username = username;
    this.rating = rating;
    this.comment = comment;
    this.submittedAt = submittedAt;
  }

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public Integer getRating() {
    return rating;
  }

  public String getComment() {
    return comment;
  }

  public Instant getSubmittedAt() {
    return submittedAt;
  }
}
