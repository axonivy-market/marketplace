package com.axonivy.market.repository.criteria;

import static com.axonivy.market.repository.constants.FieldConstants.NAMES_FIELD;
import static com.axonivy.market.repository.constants.FieldConstants.SHORT_DESCRIPTIONS_FIELD;

import com.axonivy.market.enums.TypeOption;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchCriteria {

  public static final String[] DEFAULT_SEARCH_FIELDS = { NAMES_FIELD, SHORT_DESCRIPTIONS_FIELD };

  private String keyword;
  private TypeOption type;
  private String language;
  private String[] excludeProperties;

}
