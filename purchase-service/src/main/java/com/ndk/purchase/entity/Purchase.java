package com.ndk.purchase.entity;

import com.ndk.purchase.enums.PurchaseStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "purchase")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Purchase {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "purchase_code", nullable = false, unique = true, length = 50)
  private String purchaseCode;
  
  @Column(name = "buyer_id", nullable = false, length = 36)
  private String buyerId;
  
  @Column(name = "content_id", nullable = false)
  private Long contentId;  // Changed from String to Long to match content-service
  
  @Column(name = "creator_id", nullable = false, length = 36)
  private String creatorId;
  
  @Column(name = "content_price", nullable = false, precision = 15, scale = 2)
  private BigDecimal contentPrice;
  
  @Column(name = "platform_fee_rate", nullable = false, precision = 5, scale = 2)
  private BigDecimal platformFeeRate;
  
  @Column(name = "platform_fee", nullable = false, precision = 15, scale = 2)
  private BigDecimal platformFee;
  
  @Column(name = "creator_commission_rate", nullable = false, precision = 5, scale = 2)
  private BigDecimal creatorCommissionRate;
  
  @Column(name = "creator_commission", nullable = false, precision = 15, scale = 2)
  private BigDecimal creatorCommission;
  
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private PurchaseStatus status = PurchaseStatus.PENDING;
  
  @Column(name = "purchased_at", nullable = false)
  private Instant purchasedAt;
  
  @Column(name = "completed_at")
  private Instant completedAt;
  
  @OneToMany(mappedBy = "purchase")
  private List<PurchaseTransaction> transactions;
}
