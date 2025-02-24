package com.axonivy.market.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.Map;

@Converter(autoApply = true)
public class MapStringConverter implements AttributeConverter<Map<String, String>, String> {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(Map<String, String> attribute) {
    try {
      return objectMapper.writeValueAsString(attribute);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Could not convert map to JSON", e);
    }
  }

  @Override
  public Map<String, String> convertToEntityAttribute(String dbData) {
    try {
      return objectMapper.readValue(dbData, new TypeReference<>() {
      });
    } catch (IOException e) {
      throw new RuntimeException("Could not convert JSON to map", e);
    }
  }
}
