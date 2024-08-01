package com.axonivy.market.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackModelRequest {
  @Schema(description = "Product id (from meta.json)", example = "portal")
  @NotBlank(message = "Product id cannot be blank")
  private String productId;

  @Schema(description = "User's feedback content", example = "Pretty cool connector.")
  @NotBlank(message = "Content cannot be blank")
  @Size(max = 250, message = "Content length must not exceed 250 characters")
  private String content;

  @Schema(description = "User's rating point of target product", example = "5", minimum = "1", maximum = "5")
  @Min(value = 1, message = "Rating should not be less than 1")
  @Max(value = 5, message = "Rating should not be greater than 5")
  private Integer rating;
}
