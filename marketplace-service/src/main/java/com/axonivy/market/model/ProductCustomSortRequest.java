package com.axonivy.market.model;

import com.axonivy.market.enums.SortDirection;
import com.axonivy.market.enums.SortOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
  @NotEmpty
  private List<String> orderedListOfProducts;

  @NotNull(message = "sortRuleForRemainder must not be null")
  private SortOption sortRuleForRemainder;

  @NotNull(message = "sortDirectionForRemainder must not be null")
  private SortDirection sortDirectionForRemainder;
}
