package com.axonivy.market.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductRating {
  @Schema(description = "Specific rating point of product", example = "3")
  private Integer starRating;
  @Schema(description = "Count of rating on this specific point", example = "20")
  private Integer commentNumber;
  @Schema(description = "Weight ration of this point/ total point", example = "20")
  private Integer percent;
}
