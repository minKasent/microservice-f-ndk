package com.ndk.contentservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.contentservice.dto.request.CreateCategoryRequest;
import com.ndk.contentservice.dto.request.UpdateCategoryRequest;
import com.ndk.contentservice.dto.response.CategoryDto;
import com.ndk.contentservice.service.CategoryService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

  @Mock
  private CategoryService categoryService;

  @InjectMocks
  private CategoryController categoryController;

  @Test
  @DisplayName("getAllCategories should return 200 with list")
  void getAllCategories_Success() {
    List<CategoryDto> list = List.of(CategoryDto.builder().id(1L).name("Tech").build());
    when(categoryService.getAllCategories()).thenReturn(list);

    ResponseEntity<ApiResponse<List<CategoryDto>>> response = categoryController.getAllCategories();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData()).hasSize(1);
    assertThat(response.getBody().getData().get(0).getName()).isEqualTo("Tech");
  }

  @Test
  @DisplayName("getCategoryById should return 200 with category")
  void getCategoryById_Success() {
    CategoryDto dto = CategoryDto.builder().id(1L).name("Tech").build();
    when(categoryService.getCategoryById(1L)).thenReturn(dto);

    ResponseEntity<ApiResponse<CategoryDto>> response = categoryController.getCategoryById(1L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("createCategory should return 200 with created category")
  void createCategory_Success() {
    CreateCategoryRequest request = CreateCategoryRequest.builder().name("Tech").build();
    CategoryDto dto = CategoryDto.builder().id(1L).name("Tech").build();
    when(categoryService.createCategory(request)).thenReturn(dto);

    ResponseEntity<ApiResponse<CategoryDto>> response = categoryController.createCategory(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("updateCategory should return 200 with updated category")
  void updateCategory_Success() {
    UpdateCategoryRequest request = UpdateCategoryRequest.builder().name("New").build();
    CategoryDto dto = CategoryDto.builder().id(1L).name("New").build();
    when(categoryService.updateCategory(1L, request)).thenReturn(dto);

    ResponseEntity<ApiResponse<CategoryDto>> response = categoryController.updateCategory(1L, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().getName()).isEqualTo("New");
  }

  @Test
  @DisplayName("deleteCategory should return 200")
  void deleteCategory_Success() {
    ResponseEntity<ApiResponse<Void>> response = categoryController.deleteCategory(1L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(categoryService).deleteCategory(1L);
  }
}
