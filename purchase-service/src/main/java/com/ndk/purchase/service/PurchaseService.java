package com.ndk.purchase.service;

import com.ndk.purchase.dto.request.PurchaseContentRequest;
import com.ndk.purchase.dto.response.PurchaseDto;
import com.ndk.purchase.dto.response.PurchaseTransactionDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PurchaseService {
  PurchaseDto purchaseContent(String buyerId, PurchaseContentRequest request);
  
  PurchaseDto getPurchaseById(Long id);
  
  Page<PurchaseDto> getMyPurchases(String buyerId, Pageable pageable);
  
  List<PurchaseTransactionDto> getPurchaseTransactions(Long purchaseId);
}
