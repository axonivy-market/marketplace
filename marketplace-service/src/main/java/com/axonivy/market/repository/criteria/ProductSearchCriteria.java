package com.axonivy.market.repository.criteria;

import static com.axonivy.market.repository.enums.DocumentField.NAMES;
import static com.axonivy.market.repository.enums.DocumentField.SHORT_DESCRIPTIONS;

import java.util.List;

import com.axonivy.market.enums.TypeOption;
import com.axonivy.market.repository.enums.DocumentField;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchCriteria {

  public static final List<DocumentField> DEFAULT_SEARCH_FIELDS = List.of(NAMES, SHORT_DESCRIPTIONS);

  private String keyword;
  private TypeOption type;
  private String language;
  private List<DocumentField> fields;
  private List<DocumentField> excludeFields;

}
