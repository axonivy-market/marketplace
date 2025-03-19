package com.axonivy.market.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Converter
public abstract class CollectionConverter<T extends Collection<String>> implements AttributeConverter<T, String> {

  protected abstract T createCollection(Collection<String> elements);

  private static final String COMMA = ",";

  @Override
  public String convertToDatabaseColumn(T collection) {
    return (collection != null && !collection.isEmpty()) ?
        collection.stream().map(String::trim).collect(Collectors.joining(COMMA)) : Strings.EMPTY;
  }

  @Override
  public T convertToEntityAttribute(String string) {
    if (string == null || string.isBlank()) {
      return createCollection(Collections.emptyList());
    }
    return createCollection(Arrays.asList(string.split(COMMA)));
  }
}
