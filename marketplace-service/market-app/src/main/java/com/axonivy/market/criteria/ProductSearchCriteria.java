package com.axonivy.market.criteria;

import com.axonivy.market.enums.DocumentField;
import com.axonivy.market.enums.Language;
import com.axonivy.market.enums.TypeOption;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.axonivy.market.enums.DocumentField.NAMES;
import static com.axonivy.market.enums.DocumentField.SHORT_DESCRIPTIONS;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchCriteria {

  public static final List<DocumentField> DEFAULT_SEARCH_FIELDS = List.of(NAMES, SHORT_DESCRIPTIONS);

  private String keyword;
  private TypeOption type;
  private Language language;
  private List<DocumentField> fields;
  private List<DocumentField> excludeFields;
  private boolean isListed;

}
