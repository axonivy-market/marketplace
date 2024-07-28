package com.axonivy.market.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductCustomSortRequest {
  @NotEmpty(message = "orderedListOfProducts must not be empty")
  private List<String> orderedListOfProducts;

  @NotBlank(message = "sortRuleForRemainder must not be null or blank")
  private String sortRuleForRemainder;

  @NotBlank(message = "sortDirectionForRemainder must not be null or blank")
  private String sortDirectionForRemainder;
}
