package com.ndk.purchase.enums;

public enum TransactionType {
  DEDUCT_BUYER,      // Trừ credit từ buyer
  ADD_CREATOR,       // Cộng commission cho creator
  ADD_PLATFORM,      // Cộng platform fee
  REFUND_BUYER       // Hoàn tiền cho buyer
}
