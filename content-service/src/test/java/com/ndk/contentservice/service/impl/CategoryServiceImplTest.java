package com.ndk.contentservice.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ndk.common.api.exception.DevSharingException;
import com.ndk.contentservice.dto.request.CreateCategoryRequest;
import com.ndk.contentservice.dto.request.UpdateCategoryRequest;
import com.ndk.contentservice.dto.response.CategoryDto;
import com.ndk.contentservice.entity.Category;
import com.ndk.contentservice.exception.ExceptionEnum;
import com.ndk.contentservice.mapper.CategoryMapper;
import com.ndk.contentservice.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private CategoryMapper categoryMapper;

  @InjectMocks
  private CategoryServiceImpl categoryService;

  @Test
  @DisplayName("createCategory should save and return dto")
  void createCategory_Success() {
    CreateCategoryRequest request = CreateCategoryRequest.builder()
        .name("Backend")
        .description("Backend Development")
        .slug("backend")
        .build();

    Category savedCategory = Category.builder()
        .id(1L)
        .name("Backend")
        .description("Backend Development")
        .slug("backend")
        .build();

    CategoryDto expectedDto = CategoryDto.builder()
        .id(1L)
        .name("Backend")
        .build();

    when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
    when(categoryMapper.toDto(savedCategory)).thenReturn(expectedDto);

    CategoryDto result = categoryService.createCategory(request);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getName()).isEqualTo("Backend");
    verify(categoryRepository).save(any(Category.class));
  }

  @Test
  @DisplayName("updateCategory should update existing fields and save")
  void updateCategory_Success() {
    Long categoryId = 1L;
    Category existing = Category.builder()
        .id(categoryId)
        .name("Old Name")
        .description("Old Desc")
        .slug("old-slug")
        .build();

    UpdateCategoryRequest request = UpdateCategoryRequest.builder()
        .name("New Name")
        .description("New Desc")
        .slug("new-slug")
        .build();

    CategoryDto expectedDto = CategoryDto.builder()
        .id(categoryId)
        .name("New Name")
        .build();

    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existing));
    when(categoryRepository.save(existing)).thenReturn(existing);
    when(categoryMapper.toDto(existing)).thenReturn(expectedDto);

    CategoryDto result = categoryService.updateCategory(categoryId, request);

    assertThat(result.getName()).isEqualTo("New Name");
    assertThat(existing.getName()).isEqualTo("New Name");
    assertThat(existing.getDescription()).isEqualTo("New Desc");
    assertThat(existing.getSlug()).isEqualTo("new-slug");
    verify(categoryRepository).save(existing);
  }

  @Test
  @DisplayName("updateCategory should throw CATEGORY_NOT_FOUND when category does not exist")
  void updateCategory_ThrowsException_WhenNotFound() {
    Long categoryId = 99L;
    UpdateCategoryRequest request = UpdateCategoryRequest.builder().name("New").build();

    when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> categoryService.updateCategory(categoryId, request))
        .isInstanceOf(DevSharingException.class)
        .satisfies(e -> {
          DevSharingException ex = (DevSharingException) e;
          assertThat(ex.getExceptionInfo().getErrorCode()).isEqualTo(ExceptionEnum.CATEGORY_NOT_FOUND.getErrorCode());
        });
  }

  @Test
  @DisplayName("deleteCategory should delete entity when found")
  void deleteCategory_Success() {
    Long categoryId = 1L;
    Category existing = Category.builder().id(categoryId).build();

    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existing));

    categoryService.deleteCategory(categoryId);

    verify(categoryRepository).delete(existing);
  }

  @Test
  @DisplayName("deleteCategory should throw CATEGORY_NOT_FOUND when category does not exist")
  void deleteCategory_ThrowsException_WhenNotFound() {
    Long categoryId = 99L;
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
        .isInstanceOf(DevSharingException.class)
        .satisfies(e -> {
          DevSharingException ex = (DevSharingException) e;
          assertThat(ex.getExceptionInfo().getErrorCode()).isEqualTo(ExceptionEnum.CATEGORY_NOT_FOUND.getErrorCode());
        });
  }

  @Test
  @DisplayName("getCategoryById should return dto when found")
  void getCategoryById_Success() {
    Long categoryId = 1L;
    Category existing = Category.builder().id(categoryId).name("AI").build();
    CategoryDto dto = CategoryDto.builder().id(categoryId).name("AI").build();

    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existing));
    when(categoryMapper.toDto(existing)).thenReturn(dto);

    CategoryDto result = categoryService.getCategoryById(categoryId);

    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("AI");
  }

  @Test
  @DisplayName("getCategoryById should throw exception when not found")
  void getCategoryById_ThrowsException_WhenNotFound() {
    Long categoryId = 99L;
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> categoryService.getCategoryById(categoryId))
        .isInstanceOf(DevSharingException.class)
        .satisfies(e -> {
          DevSharingException ex = (DevSharingException) e;
          assertThat(ex.getExceptionInfo().getErrorCode()).isEqualTo(ExceptionEnum.CATEGORY_NOT_FOUND.getErrorCode());
        });
  }

  @Test
  @DisplayName("getAllCategories should return all categories mapped to DTO")
  void getAllCategories_Success() {
    Category cat1 = Category.builder().id(1L).name("Cat 1").build();
    Category cat2 = Category.builder().id(2L).name("Cat 2").build();
    CategoryDto dto1 = CategoryDto.builder().id(1L).name("Cat 1").build();
    CategoryDto dto2 = CategoryDto.builder().id(2L).name("Cat 2").build();

    when(categoryRepository.findAll()).thenReturn(List.of(cat1, cat2));
    when(categoryMapper.toDto(cat1)).thenReturn(dto1);
    when(categoryMapper.toDto(cat2)).thenReturn(dto2);

    List<CategoryDto> result = categoryService.getAllCategories();

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getName()).isEqualTo("Cat 1");
    assertThat(result.get(1).getName()).isEqualTo("Cat 2");
  }
}
