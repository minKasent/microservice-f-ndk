package com.ndk.purchase.service;

import com.ndk.purchase.dto.response.LibraryItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LibraryService {
  Page<LibraryItemDto> getMyLibrary(String userId, Pageable pageable);
  
  LibraryItemDto getLibraryItem(String userId, Long contentId);
  
  boolean checkOwnership(String userId, Long contentId);
  
  void trackAccess(String userId, Long contentId);
}
