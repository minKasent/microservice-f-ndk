package com.ndk.contentservice.dto.request;

import com.ndk.contentservice.entity.ContentLevel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateContentRequest {
  @NotBlank(message = "{content.title.notBlank}")
  @Size(max = 255, message = "{content.title.size}")
  private String title;

  @Size(max = 5000, message = "{content.description.size}")
  private String description;

  @NotNull(message = "{content.categoryId.notNull}")
  private Long categoryId;

  private ContentLevel level;

  @NotNull(message = "{content.price.notNull}")
  @DecimalMin(value = "0.0", message = "{content.price.min}")
  private BigDecimal price;

  private String thumbnail;
}
