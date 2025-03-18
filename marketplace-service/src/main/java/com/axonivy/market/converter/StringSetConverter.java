package com.axonivy.market.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.axonivy.market.constants.CommonConstants.COMMA;

@Converter
public class StringSetConverter implements AttributeConverter<Set<String>, String> {

  @Override
  public String convertToDatabaseColumn(Set<String> versions) {
    if (versions == null || versions.isEmpty()) {
      return Strings.EMPTY;
    }
    return versions.stream().map(String::trim).collect(Collectors.joining(COMMA));
  }

  @Override
  public Set<String> convertToEntityAttribute(String version) {
    if (version == null || version.isBlank()) {
      return new HashSet<>();
    }
    return Arrays.stream(version.split(COMMA)).map(String::trim).collect(Collectors.toSet());
  }
}
