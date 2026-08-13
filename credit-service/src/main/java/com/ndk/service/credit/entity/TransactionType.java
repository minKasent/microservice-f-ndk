package com.ndk.service.credit.entity;

public enum TransactionType {
  DEPOSIT,      // Consumer nạp tiền vào ví
  PURCHASE,     // Consumer mua content (trừ credit)
  EARNING,      // Creator nhận credit từ bán content
  WITHDRAWAL,   // Creator rút tiền (trừ credit)
  REFUND        // Hoàn tiền
}
