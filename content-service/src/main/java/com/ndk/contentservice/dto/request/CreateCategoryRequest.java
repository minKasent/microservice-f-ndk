package com.ndk.contentservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {
  @NotBlank(message = "Category name is required")
  @Size(max = 100, message = "Category name must not exceed 100 characters")
  private String name;

  @Size(max = 500, message = "Description must not exceed 500 characters")
  private String description;

  @NotBlank(message = "Slug is required")
  @Size(max = 100, message = "Slug must not exceed 100 characters")
  private String slug;
}
