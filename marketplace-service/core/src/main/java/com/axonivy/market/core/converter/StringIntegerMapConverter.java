package com.axonivy.market.core.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Map;

@Converter
public class StringIntegerMapConverter implements AttributeConverter<Map<String, Integer>, String> {
  private static final Logger LOGGER = LogManager.getLogger(StringIntegerMapConverter.class);
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(Map<String, Integer> attribute) {
    if (attribute == null || attribute.isEmpty()) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(attribute);
    } catch (JsonProcessingException e) {
      LOGGER.error("Error converting map to JSON", e);
      return null;
    }
  }

  @Override
  public Map<String, Integer> convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isBlank()) {
      return Collections.emptyMap();
    }
    try {
      return objectMapper.readValue(dbData, new TypeReference<>() {});
    } catch (JsonProcessingException e) {
      LOGGER.error("Error converting JSON to map", e);
      return Collections.emptyMap();
    }
  }
}

