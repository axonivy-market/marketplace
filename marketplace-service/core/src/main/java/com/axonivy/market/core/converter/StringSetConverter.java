package com.axonivy.market.core.converter;

import jakarta.persistence.Converter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Converter
public class StringSetConverter extends AbstractCollectionConverter<Set<String>> {

  @Override
  protected Set<String> createCollection(Collection<String> elements) {
    return new HashSet<>(elements);
  }
}
