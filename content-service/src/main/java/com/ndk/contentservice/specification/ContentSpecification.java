package com.ndk.contentservice.specification;

import com.ndk.contentservice.dto.request.ContentSearchRequest;
import com.ndk.contentservice.entity.Content;
import com.ndk.contentservice.entity.ContentLevel;
import com.ndk.contentservice.entity.ContentStatus;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class ContentSpecification {

  public static Specification<Content> withFilters(ContentSearchRequest request) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      // Keyword search in title and description
      if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
        String keyword = "%" + request.getKeyword().toLowerCase() + "%";
        Predicate titlePredicate = criteriaBuilder.like(
            criteriaBuilder.lower(root.get("title")), keyword);
        Predicate descriptionPredicate = criteriaBuilder.like(
            criteriaBuilder.lower(root.get("description")), keyword);
        predicates.add(criteriaBuilder.or(titlePredicate, descriptionPredicate));
      }

      // Category filter
      if (request.getCategoryId() != null) {
        predicates.add(criteriaBuilder.equal(root.get("category").get("id"), request.getCategoryId()));
      }

      // Status filter
      if (request.getStatus() != null) {
        predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
      }

      // Level filter
      if (request.getLevel() != null) {
        predicates.add(criteriaBuilder.equal(root.get("level"), request.getLevel()));
      }

      // Creator filter
      if (request.getCreatorId() != null) {
        predicates.add(criteriaBuilder.equal(root.get("creatorId"), request.getCreatorId()));
      }

      // Price range filter
      if (request.getMinPrice() != null) {
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), request.getMinPrice()));
      }
      if (request.getMaxPrice() != null) {
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), request.getMaxPrice()));
      }

      // View count range filter
      if (request.getMinViewCount() != null) {
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("viewCount"), request.getMinViewCount()));
      }
      if (request.getMaxViewCount() != null) {
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("viewCount"), request.getMaxViewCount()));
      }

      // Purchase count range filter
      if (request.getMinPurchaseCount() != null) {
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("purchaseCount"), request.getMinPurchaseCount()));
      }
      if (request.getMaxPurchaseCount() != null) {
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("purchaseCount"), request.getMaxPurchaseCount()));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }

  public static Specification<Content> hasStatus(ContentStatus status) {
    return (root, query, criteriaBuilder) -> 
        criteriaBuilder.equal(root.get("status"), status);
  }

  public static Specification<Content> hasCategory(Long categoryId) {
    return (root, query, criteriaBuilder) -> 
        criteriaBuilder.equal(root.get("category").get("id"), categoryId);
  }

  public static Specification<Content> hasLevel(ContentLevel level) {
    return (root, query, criteriaBuilder) -> 
        criteriaBuilder.equal(root.get("level"), level);
  }

  public static Specification<Content> hasCreator(Long creatorId) {
    return (root, query, criteriaBuilder) -> 
        criteriaBuilder.equal(root.get("creatorId"), creatorId);
  }

  public static Specification<Content> priceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
    return (root, query, criteriaBuilder) -> {
      if (minPrice != null && maxPrice != null) {
        return criteriaBuilder.between(root.get("price"), minPrice, maxPrice);
      } else if (minPrice != null) {
        return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
      } else if (maxPrice != null) {
        return criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
      }
      return criteriaBuilder.conjunction();
    };
  }

  public static Specification<Content> searchByKeyword(String keyword) {
    return (root, query, criteriaBuilder) -> {
      if (keyword == null || keyword.trim().isEmpty()) {
        return criteriaBuilder.conjunction();
      }
      String searchPattern = "%" + keyword.toLowerCase() + "%";
      return criteriaBuilder.or(
          criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern),
          criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern)
      );
    };
  }
}
