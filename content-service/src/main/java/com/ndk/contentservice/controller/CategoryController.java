package com.ndk.contentservice.controller;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.contentservice.dto.request.CreateCategoryRequest;
import com.ndk.contentservice.dto.request.UpdateCategoryRequest;
import com.ndk.contentservice.dto.response.CategoryDto;
import com.ndk.contentservice.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "APIs for managing categories")
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping
  @Operation(summary = "Get all categories", description = "Get list of all available categories")
  public ResponseEntity<ApiResponse<List<CategoryDto>>> getAllCategories() {
    List<CategoryDto> categories = categoryService.getAllCategories();
    return ResponseEntity.ok(ApiResponse.success(categories));
  }

  @GetMapping("/{categoryId}")
  @Operation(summary = "Get category by ID", description = "Get category details by ID")
  public ResponseEntity<ApiResponse<CategoryDto>> getCategoryById(@PathVariable Long categoryId) {
    CategoryDto category = categoryService.getCategoryById(categoryId);
    return ResponseEntity.ok(ApiResponse.success(category));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create category", description = "Create a new category (Admin only)")
  public ResponseEntity<ApiResponse<CategoryDto>> createCategory(
      @Valid @RequestBody CreateCategoryRequest request) {
    CategoryDto category = categoryService.createCategory(request);
    return ResponseEntity.ok(ApiResponse.success(category));
  }

  @PutMapping("/{categoryId}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update category", description = "Update an existing category (Admin only)")
  public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(
      @PathVariable Long categoryId,
      @Valid @RequestBody UpdateCategoryRequest request) {
    CategoryDto category = categoryService.updateCategory(categoryId, request);
    return ResponseEntity.ok(ApiResponse.success(category));
  }

  @DeleteMapping("/{categoryId}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Delete category", description = "Delete a category (Admin only)")
  public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long categoryId) {
    categoryService.deleteCategory(categoryId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
