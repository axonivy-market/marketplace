package com.axonivy.market.core.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Converter
public class StringIntegerMapConverter implements AttributeConverter<Map<String, Integer>, String> {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final TypeReference<Map<String, Integer>> MAP_TYPE = new TypeReference<>() {
  };

  @Override
  public String convertToDatabaseColumn(Map<String, Integer> attribute) {
    if (attribute == null || attribute.isEmpty()) {
      return null;
    }
    try {
      return OBJECT_MAPPER.writeValueAsString(attribute);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Failed to serialize alert map to JSON", e);
    }
  }

  @Override
  public Map<String, Integer> convertToEntityAttribute(String dbData) {
    if (StringUtils.isBlank(dbData)) {
      return Map.of();
    }
    try {
      return OBJECT_MAPPER.readValue(dbData, MAP_TYPE);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Failed to deserialize alert map JSON", e);
    }
  }
}

