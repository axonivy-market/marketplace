package com.axonivy.market.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.axonivy.market.constants.CommonConstants.COMMA;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {
  @Override
  public String convertToDatabaseColumn(List<String> stringList) {
    return (stringList != null && !stringList.isEmpty()) ? String.join(COMMA, stringList) : Strings.EMPTY;
  }

  @Override
  public List<String> convertToEntityAttribute(String string) {
    if (string == null || string.trim().isEmpty()) {
      return new ArrayList<>();
    }
    return new ArrayList<>(Arrays.asList(string.split(COMMA)));
  }
}
