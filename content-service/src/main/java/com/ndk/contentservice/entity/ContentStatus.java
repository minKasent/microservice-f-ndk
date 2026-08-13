package com.ndk.contentservice.entity;

public enum ContentStatus {
  DRAFT,           // Creator đang soạn thảo
  PENDING_REVIEW,  // Chờ moderator duyệt
  APPROVED,        // Đã được duyệt, có thể publish
  REJECTED,        // Bị từ chối
  PUBLISHED,       // Đã publish, consumer có thể mua
  ARCHIVED         // Đã lưu trữ, không còn bán
}
