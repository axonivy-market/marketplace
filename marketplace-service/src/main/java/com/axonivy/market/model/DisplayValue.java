package com.axonivy.market.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DisplayValue {

  private String locale;
  private String value;

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof DisplayValue other)) {
      return false;
    }
    EqualsBuilder builder = new EqualsBuilder();
    builder.append(value, other.getValue());
    builder.append(locale, other.locale);
    return builder.isEquals();
  }

  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();
    builder.append(getValue());
    builder.append(getLocale());
    return builder.hashCode();
  }
}
