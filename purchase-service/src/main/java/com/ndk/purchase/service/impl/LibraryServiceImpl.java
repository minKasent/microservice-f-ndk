package com.ndk.purchase.service.impl;

import com.ndk.common.api.logging.LogExecutionTime;

import com.ndk.common.api.exception.DevSharingException;
import com.ndk.purchase.client.ContentServiceClient;
import com.ndk.purchase.client.IdentityServiceClient;
import com.ndk.purchase.dto.feign.ContentDto;
import com.ndk.purchase.dto.feign.UserDto;
import com.ndk.purchase.dto.response.LibraryItemDto;
import com.ndk.purchase.entity.Library;
import com.ndk.purchase.entity.Purchase;
import com.ndk.purchase.exception.ExceptionEnum;
import com.ndk.purchase.mapper.LibraryMapper;
import com.ndk.purchase.repository.LibraryRepository;
import com.ndk.purchase.repository.PurchaseRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@LogExecutionTime
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LibraryServiceImpl implements com.ndk.purchase.service.LibraryService {
  private final LibraryRepository libraryRepository;
  private final PurchaseRepository purchaseRepository;
  private final LibraryMapper libraryMapper;
  private final ContentServiceClient contentClient;
  private final IdentityServiceClient identityClient;
  
  @Override
  public Page<LibraryItemDto> getMyLibrary(String userId, Pageable pageable) {
    return libraryRepository.findByUserIdAndIsActive(userId, true, pageable)
        .map(library -> {
          LibraryItemDto dto = libraryMapper.toDto(library);
          enrichLibraryItem(dto, library);
          return dto;
        });
  }
  
  @Override
  public LibraryItemDto getLibraryItem(String userId, Long contentId) {
    Library library = libraryRepository.findByUserIdAndContentId(userId, contentId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.LIBRARY_NOT_FOUND, null));
    
    LibraryItemDto dto = libraryMapper.toDto(library);
    enrichLibraryItem(dto, library);
    return dto;
  }
  
  @Override
  public boolean checkOwnership(String userId, Long contentId) {
    return libraryRepository.existsByUserIdAndContentId(userId, contentId);
  }
  
  @Override
  public void trackAccess(String userId, Long contentId) {
    Library library = libraryRepository.findByUserIdAndContentId(userId, contentId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.LIBRARY_NOT_FOUND, null));
    
    library.setLastAccessedAt(Instant.now());
    library.setAccessCount(library.getAccessCount() + 1);
    libraryRepository.save(library);
  }
  
  private void enrichLibraryItem(LibraryItemDto dto, Library library) {
    try {
      // Get content info
      ContentDto content = contentClient.getContentById(library.getContentId()).getData();
      dto.setContentTitle(content.getTitle());
      dto.setContentThumbnail(content.getThumbnail());
      
      // Get creator info
      UserDto creator = identityClient.getUserById(content.getCreatorId()).getData();
      dto.setCreatorName(creator.getDisplayName());
      
      // Get purchase price
      purchaseRepository.findById(library.getPurchaseId())
          .ifPresent(purchase -> dto.setPurchasePrice(purchase.getContentPrice()));

    } catch (Exception e) {
      log.error("Failed to enrich library item: {}", e.getMessage());
    }
  }
}
