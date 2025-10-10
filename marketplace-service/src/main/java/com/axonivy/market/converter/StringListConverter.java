package com.axonivy.market.converter;

import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Converter
public class StringListConverter extends AbstractCollectionConverter<List<String>> {

  @Override
  protected List<String> createCollection(Collection<String> elements) {
    return new ArrayList<>(elements);
  }

}
