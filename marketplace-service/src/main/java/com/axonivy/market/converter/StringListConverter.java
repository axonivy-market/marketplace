package com.axonivy.market.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {
  private static final String SPLIT_CHAR = ";";

  @Override
  public String convertToDatabaseColumn(List<String> stringList) {
    return (stringList != null && !stringList.isEmpty()) ? String.join(SPLIT_CHAR, stringList) : "";
  }

  @Override
  public List<String> convertToEntityAttribute(String string) {
    if (string == null || string.trim().isEmpty()) {
      return new ArrayList<>();
    }
    return new ArrayList<>(Arrays.asList(string.split(SPLIT_CHAR)));
  }
}
