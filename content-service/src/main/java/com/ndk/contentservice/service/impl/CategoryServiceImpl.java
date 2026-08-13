package com.ndk.contentservice.service.impl;

import com.ndk.common.api.exception.DevSharingException;
import com.ndk.contentservice.dto.request.CreateCategoryRequest;
import com.ndk.contentservice.dto.request.UpdateCategoryRequest;
import com.ndk.contentservice.dto.response.CategoryDto;
import com.ndk.contentservice.entity.Category;
import com.ndk.contentservice.exception.ExceptionEnum;
import com.ndk.contentservice.mapper.CategoryMapper;
import com.ndk.contentservice.repository.CategoryRepository;
import com.ndk.contentservice.service.CategoryService;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

  private final CategoryRepository categoryRepository;
  private final CategoryMapper categoryMapper;

  @Override
  @Transactional
  public CategoryDto createCategory(CreateCategoryRequest request) {
    log.info("Creating category: {}", request.getName());

    Category category = Category.builder()
        .name(request.getName())
        .description(request.getDescription())
        .slug(request.getSlug())
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();

    category = categoryRepository.save(category);
    log.info("Category created successfully with id: {}", category.getId());

    return categoryMapper.toDto(category);
  }

  @Override
  @Transactional
  public CategoryDto updateCategory(Long categoryId, UpdateCategoryRequest request) {
    log.info("Updating category {}", categoryId);

    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.CATEGORY_NOT_FOUND, null));

    if (request.getName() != null) {
      category.setName(request.getName());
    }
    if (request.getDescription() != null) {
      category.setDescription(request.getDescription());
    }
    if (request.getSlug() != null) {
      category.setSlug(request.getSlug());
    }

    category.setUpdatedAt(Instant.now());
    category = categoryRepository.save(category);

    log.info("Category {} updated successfully", categoryId);
    return categoryMapper.toDto(category);
  }

  @Override
  @Transactional
  public void deleteCategory(Long categoryId) {
    log.info("Deleting category {}", categoryId);

    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.CATEGORY_NOT_FOUND, null));

    categoryRepository.delete(category);
    log.info("Category {} deleted successfully", categoryId);
  }

  @Override
  @Transactional(readOnly = true)
  public CategoryDto getCategoryById(Long categoryId) {
    log.info("Getting category {}", categoryId);

    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.CATEGORY_NOT_FOUND, null));

    return categoryMapper.toDto(category);
  }

  @Override
  @Transactional(readOnly = true)
  public List<CategoryDto> getAllCategories() {
    log.info("Getting all categories");

    List<Category> categories = categoryRepository.findAll();
    return categories.stream()
        .map(categoryMapper::toDto)
        .toList();
  }
}
