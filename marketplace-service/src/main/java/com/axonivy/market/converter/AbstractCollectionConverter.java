package com.axonivy.market.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static com.axonivy.market.constants.CommonConstants.COMMA;

@Converter
public abstract class AbstractCollectionConverter<T extends Collection<String>>
    implements AttributeConverter<T, String> {

  protected abstract T createCollection(Collection<String> elements);

  @Override
  public String convertToDatabaseColumn(T collection) {
    if (collection != null && !collection.isEmpty()) {
      return collection.stream().map(String::trim).collect(Collectors.joining(COMMA));
    }
    return Strings.EMPTY;
  }

  @Override
  public T convertToEntityAttribute(String string) {
    if (string == null || string.isBlank()) {
      return createCollection(Collections.emptyList());
    }
    return createCollection(Arrays.asList(string.split(COMMA)));
  }
}
