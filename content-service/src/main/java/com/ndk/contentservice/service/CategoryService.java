package com.ndk.contentservice.service;

import com.ndk.contentservice.dto.request.CreateCategoryRequest;
import com.ndk.contentservice.dto.request.UpdateCategoryRequest;
import com.ndk.contentservice.dto.response.CategoryDto;
import java.util.List;

public interface CategoryService {
  
  // CRUD operations (Admin only)
  CategoryDto createCategory(CreateCategoryRequest request);
  
  CategoryDto updateCategory(Long categoryId, UpdateCategoryRequest request);
  
  void deleteCategory(Long categoryId);
  
  CategoryDto getCategoryById(Long categoryId);
  
  // Public operations
  List<CategoryDto> getAllCategories();
}
