package com.ndk.purchase.service.impl;

import com.ndk.common.api.logging.LogExecutionTime;

import com.ndk.common.api.exception.DevSharingException;
import com.ndk.purchase.client.ContentServiceClient;
import com.ndk.purchase.client.IdentityServiceClient;
import com.ndk.purchase.client.NotificationServiceClient;
import com.ndk.purchase.client.adapter.CreditServiceClientAdapter;
import com.ndk.purchase.dto.feign.AddCreditRequest;
import com.ndk.purchase.dto.feign.ContentDto;
import com.ndk.purchase.dto.feign.CreditBalanceDto;
import com.ndk.purchase.dto.feign.CreditTransactionDto;
import com.ndk.purchase.dto.feign.DeductCreditRequest;
import com.ndk.purchase.dto.feign.SendNotificationRequest;
import com.ndk.purchase.dto.feign.UserDto;
import com.ndk.purchase.dto.request.PurchaseContentRequest;
import com.ndk.purchase.enums.NotificationChannel;
import com.ndk.purchase.dto.response.PurchaseDto;
import com.ndk.purchase.dto.response.PurchaseTransactionDto;
import com.ndk.purchase.entity.Library;
import com.ndk.purchase.entity.Purchase;
import com.ndk.purchase.entity.PurchaseTransaction;
import com.ndk.purchase.enums.PurchaseStatus;
import com.ndk.purchase.enums.TransactionStatus;
import com.ndk.purchase.enums.TransactionType;
import com.ndk.purchase.exception.ExceptionEnum;
import com.ndk.purchase.mapper.PurchaseMapper;
import com.ndk.purchase.mapper.PurchaseTransactionMapper;
import com.ndk.purchase.repository.LibraryRepository;
import com.ndk.purchase.repository.PurchaseRepository;
import com.ndk.purchase.repository.PurchaseTransactionRepository;
import com.ndk.purchase.service.PurchaseService;
import com.ndk.purchase.saga.PurchaseSagaOrchestrator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@LogExecutionTime
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PurchaseServiceImpl implements PurchaseService {

  private final PurchaseRepository purchaseRepository;
  private final LibraryRepository libraryRepository;
  private final PurchaseTransactionRepository transactionRepository;
  private final PurchaseMapper purchaseMapper;
  private final PurchaseTransactionMapper transactionMapper;

  private final CreditServiceClientAdapter creditClient;
  private final ContentServiceClient contentClient;
  private final IdentityServiceClient identityClient;
  private final NotificationServiceClient notificationClient;

  @Value("${purchase.platform-fee-rate:0.30}")
  private BigDecimal platformFeeRate;

  @Value("${purchase.creator-commission-rate:0.70}")
  private BigDecimal creatorCommissionRate;

  @Value("${purchase.saga.enabled:true}")
  private boolean sagaEnabled;

  private final PurchaseSagaOrchestrator sagaOrchestrator;

  @Override
  public PurchaseDto purchaseContent(String buyerId, PurchaseContentRequest request) {
    if (sagaEnabled) {
      log.info("Saga mode enabled - delegating to saga orchestrator - BuyerId: {}, ContentId: {}",
          buyerId, request.getContentId());
      return sagaOrchestrator.initiateSaga(buyerId, request);
    }

    // Step 1: Check if already owned
    if (libraryRepository.existsByUserIdAndContentId(buyerId, request.getContentId())) {
      throw new DevSharingException(ExceptionEnum.CONTENT_ALREADY_OWNED, null);
    }

    // Step 2: Get content info
    ContentDto content = contentClient.getContentById(request.getContentId()).getData();
    if (content == null) {
      throw new DevSharingException(ExceptionEnum.CONTENT_NOT_FOUND,
          new Object[]{request.getContentId()});
    }

    // Step 3: Get user info
    UserDto buyer = identityClient.getUserById(buyerId).getData();
    UserDto creator = identityClient.getUserById(content.getCreatorId()).getData();

    // Step 4: Check credit balance
    CreditBalanceDto balance = creditClient.getBalance(buyerId).getData();
    if (balance.getBalance().compareTo(content.getPrice()) < 0) {
      throw new DevSharingException(ExceptionEnum.INSUFFICIENT_CREDIT,
          new Object[]{content.getPrice().toString(), balance.getBalance().toString()});
    }

    // Step 5: Calculate pricing
    BigDecimal platformFee = content.getPrice().multiply(platformFeeRate)
        .setScale(2, RoundingMode.HALF_UP);
    BigDecimal creatorCommission = content.getPrice().multiply(creatorCommissionRate)
        .setScale(2, RoundingMode.HALF_UP);

    // Step 6: Create purchase record
    String purchaseCode = generatePurchaseCode();
    Purchase purchase = Purchase.builder()
        .purchaseCode(purchaseCode)
        .buyerId(buyerId)
        .contentId(request.getContentId())
        .creatorId(content.getCreatorId())
        .contentPrice(content.getPrice())
        .platformFeeRate(platformFeeRate)
        .platformFee(platformFee)
        .creatorCommissionRate(creatorCommissionRate)
        .creatorCommission(creatorCommission)
        .status(PurchaseStatus.PENDING)
        .purchasedAt(Instant.now())
        .build();

    purchase = purchaseRepository.save(purchase);

    // Step 7: Execute transactions
    try {
      // 7a. Deduct buyer credit
      PurchaseTransaction deductTx = deductBuyerCredit(purchase);
      if (deductTx.getStatus() == TransactionStatus.FAILED) {
        purchase.setStatus(PurchaseStatus.FAILED);
        purchaseRepository.save(purchase);
        throw new DevSharingException(ExceptionEnum.CREDIT_DEDUCTION_FAILED, null);
      }

      // 7b. Add creator commission
      PurchaseTransaction addCreatorTx = addCreatorCommission(purchase);
      if (addCreatorTx.getStatus() == TransactionStatus.FAILED) {
        purchase.setStatus(PurchaseStatus.FAILED);
        purchaseRepository.save(purchase);
        throw new DevSharingException(ExceptionEnum.COMMISSION_PAYMENT_FAILED, null);
      }

      // Step 8: Update purchase status
      purchase.setStatus(PurchaseStatus.COMPLETED);
      purchase.setCompletedAt(Instant.now());
      purchaseRepository.save(purchase);

      // Step 9: Add to library
      Library library = Library.builder()
          .userId(buyerId)
          .contentId(request.getContentId())
          .purchaseId(purchase.getId())
          .accessGrantedAt(Instant.now())
          .accessCount(0)
          .isActive(true)
          .build();
      libraryRepository.save(library);

      // Step 10: Increment purchase count in content-service
      try {
        contentClient.incrementPurchaseCount(request.getContentId());
        log.info("Purchase count incremented for content: {}", request.getContentId());
      } catch (Exception e) {
        log.error("Failed to increment purchase count: {}", e.getMessage());
        // Don't fail the purchase if this fails
      }

      // Step 11: Send notifications (async)
      sendNotifications(purchase, buyer, creator, content);

    } catch (Exception e) {
      log.error("Purchase failed: {}", e.getMessage(), e);
      purchase.setStatus(PurchaseStatus.FAILED);
      purchaseRepository.save(purchase);
      throw e;
    }

    PurchaseDto dto = purchaseMapper.toDto(purchase);
    dto.setContentTitle(content.getTitle());
    dto.setCreatorName(creator.getDisplayName());
    return dto;
  }

  private PurchaseTransaction deductBuyerCredit(Purchase purchase) {
    DeductCreditRequest request = DeductCreditRequest.builder()
        .userId(purchase.getBuyerId())
        .amount(purchase.getContentPrice())
        .reason("Purchase content: " + purchase.getContentId())
        .referenceId(purchase.getPurchaseCode())
        .build();

    PurchaseTransaction transaction = PurchaseTransaction.builder()
        .purchaseId(purchase.getId())
        .transactionType(TransactionType.DEDUCT_BUYER)
        .userId(purchase.getBuyerId())
        .amount(purchase.getContentPrice())
        .status(TransactionStatus.PENDING)
        .createdAt(Instant.now())
        .build();

    try {
      CreditTransactionDto creditTx = creditClient.deduct(request).getData();
      transaction.setCreditTransactionId(creditTx.getId());
      transaction.setStatus(TransactionStatus.COMPLETED);
      transaction.setCompletedAt(Instant.now());
    } catch (Exception e) {
      transaction.setStatus(TransactionStatus.FAILED);
      transaction.setErrorMessage(e.getMessage());
      log.error("Failed to deduct buyer credit: {}", e.getMessage());
    }

    return transactionRepository.save(transaction);
  }

  private PurchaseTransaction addCreatorCommission(Purchase purchase) {
    AddCreditRequest request = AddCreditRequest.builder()
        .userId(purchase.getCreatorId())
        .amount(purchase.getCreatorCommission())
        .reason("Sale commission: " + purchase.getContentId())
        .referenceId(purchase.getPurchaseCode())
        .build();

    PurchaseTransaction transaction = PurchaseTransaction.builder()
        .purchaseId(purchase.getId())
        .transactionType(TransactionType.ADD_CREATOR)
        .userId(purchase.getCreatorId())
        .amount(purchase.getCreatorCommission())
        .status(TransactionStatus.PENDING)
        .createdAt(Instant.now())
        .build();

    try {
      CreditTransactionDto creditTx = creditClient.add(request).getData();
      transaction.setCreditTransactionId(creditTx.getId());
      transaction.setStatus(TransactionStatus.COMPLETED);
      transaction.setCompletedAt(Instant.now());
    } catch (Exception e) {
      transaction.setStatus(TransactionStatus.FAILED);
      transaction.setErrorMessage(e.getMessage());
      log.error("Failed to add creator commission: {}", e.getMessage());
    }

    return transactionRepository.save(transaction);
  }

//  @Async
  private void sendNotifications(Purchase purchase, UserDto buyer, UserDto creator,
      ContentDto content) {
    try {
      // Notify buyer
      SendNotificationRequest buyerNotif = SendNotificationRequest.builder()
          .channel(NotificationChannel.EMAIL)
          .recipient(buyer.getEmail())
          .subject("Purchase Successful - DevSharing")
          .content(buildBuyerNotificationContent(buyer.getDisplayName(), content.getTitle(), purchase.getContentPrice()))
          .build();
      notificationClient.send(buyerNotif);

      // Notify creator
      SendNotificationRequest creatorNotif = SendNotificationRequest.builder()
          .channel(NotificationChannel.EMAIL)
          .recipient(creator.getEmail())
          .subject("Content Sold - DevSharing")
          .content(buildCreatorNotificationContent(creator.getDisplayName(), content.getTitle(), purchase.getCreatorCommission()))
          .build();
      notificationClient.send(creatorNotif);

    } catch (Exception e) {
      log.error("Failed to send notifications: {}", e.getMessage());
    }
  }

  private String generatePurchaseCode() {
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    return "PUR-" + timestamp + "-" + System.currentTimeMillis() % 10000;
  }

  @Override
  public PurchaseDto getPurchaseById(Long id) {
    Purchase purchase = purchaseRepository.findById(id)
        .orElseThrow(
            () -> new DevSharingException(ExceptionEnum.PURCHASE_NOT_FOUND, new Object[]{id.toString()}));

    PurchaseDto dto = purchaseMapper.toDto(purchase);

    try {
      ContentDto content = contentClient.getContentById(purchase.getContentId()).getData();
      UserDto creator = identityClient.getUserById(purchase.getCreatorId()).getData();
      dto.setContentTitle(content.getTitle());
      dto.setCreatorName(creator.getDisplayName());
    } catch (Exception e) {
      log.error("Failed to enrich purchase data: {}", e.getMessage());
    }

    return dto;
  }

  @Override
  public Page<PurchaseDto> getMyPurchases(String buyerId, Pageable pageable) {
    return purchaseRepository.findByBuyerId(buyerId, pageable)
        .map(purchase -> {
          PurchaseDto dto = purchaseMapper.toDto(purchase);
          try {
            ContentDto content = contentClient.getContentById(purchase.getContentId()).getData();
            dto.setContentTitle(content.getTitle());
          } catch (Exception e) {
            log.error("Failed to get content info: {}", e.getMessage());
          }
          return dto;
        });
  }

  @Override
  public List<PurchaseTransactionDto> getPurchaseTransactions(Long purchaseId) {
    return transactionRepository.findByPurchaseId(purchaseId).stream()
        .map(transactionMapper::toDto)
        .toList();
  }

  private String buildBuyerNotificationContent(String buyerName, String contentTitle, BigDecimal price) {
    return String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                .content { padding: 20px; background-color: #f9f9f9; }
                .success { background-color: #d4edda; border: 1px solid #c3e6cb; padding: 15px; border-radius: 5px; margin: 20px 0; }
                .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Purchase Successful!</h1>
                </div>
                <div class="content">
                    <p>Hello <strong>%s</strong>,</p>
                    <div class="success">
                        <h2>Your purchase was successful!</h2>
                    </div>
                    <p><strong>Content:</strong> %s</p>
                    <p><strong>Price:</strong> %s credits</p>
                    <p>You can now access this content in your library.</p>
                    <p>Thank you for your purchase!</p>
                </div>
                <div class="footer">
                    <p>© 2024 DevSharing Platform. All rights reserved.</p>
                </div>
            </div>
        </body>
        </html>
        """, buyerName, contentTitle, price.toString());
  }

  private String buildCreatorNotificationContent(String creatorName, String contentTitle, BigDecimal commission) {
    return String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; }
                .content { padding: 20px; background-color: #f9f9f9; }
                .success { background-color: #e3f2fd; border: 1px solid #90caf9; padding: 15px; border-radius: 5px; margin: 20px 0; }
                .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Content Sold!</h1>
                </div>
                <div class="content">
                    <p>Hello <strong>%s</strong>,</p>
                    <div class="success">
                        <h2>Great news! Your content has been sold!</h2>
                    </div>
                    <p><strong>Content:</strong> %s</p>
                    <p><strong>Your Commission:</strong> %s credits</p>
                    <p>The commission has been added to your account.</p>
                    <p>Keep creating amazing content!</p>
                </div>
                <div class="footer">
                    <p>© 2024 DevSharing Platform. All rights reserved.</p>
                </div>
            </div>
        </body>
        </html>
        """, creatorName, contentTitle, commission.toString());
  }
}
