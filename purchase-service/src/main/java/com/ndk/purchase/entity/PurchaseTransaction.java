package com.ndk.purchase.entity;

import com.ndk.purchase.enums.TransactionStatus;
import com.ndk.purchase.enums.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "purchase_transaction")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseTransaction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "purchase_id", nullable = false)
  private Long purchaseId;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "transaction_type", nullable = false, length = 30)
  private TransactionType transactionType;
  
  @Column(name = "user_id", nullable = false, length = 36)
  private String userId;
  
  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;
  
  @Column(name = "credit_transaction_id", length = 50)
  private String creditTransactionId;
  
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TransactionStatus status;
  
  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;
  
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
  
  @Column(name = "completed_at")
  private Instant completedAt;
  
  @ManyToOne
  @JoinColumn(name = "purchase_id", insertable = false, updatable = false)
  private Purchase purchase;
}
