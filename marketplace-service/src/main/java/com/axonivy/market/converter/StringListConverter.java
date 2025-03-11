package com.axonivy.market.converter;

import com.axonivy.market.constants.CommonConstants;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {
  @Override
  public String convertToDatabaseColumn(List<String> stringList) {
    return (stringList != null && !stringList.isEmpty()) ? String.join(CommonConstants.SPLIT_CHAR, stringList) : "";
  }

  @Override
  public List<String> convertToEntityAttribute(String string) {
    if (string == null || string.trim().isEmpty()) {
      return new ArrayList<>();
    }
    return new ArrayList<>(Arrays.asList(string.split(CommonConstants.SPLIT_CHAR)));
  }
}
