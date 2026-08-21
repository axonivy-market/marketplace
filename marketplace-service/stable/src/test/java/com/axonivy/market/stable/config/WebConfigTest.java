package com.axonivy.market.stable.config;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@ExtendWith(MockitoExtension.class)
class WebConfigTest {

  @Mock
  private CorsRegistry corsRegistry;

  @Mock
  private CorsRegistration corsRegistration;

  private WebConfig webConfig;

  @BeforeEach
  void setUp() {
    webConfig = new WebConfig();
    ReflectionTestUtils.setField(webConfig, "marketCorsMappings", "/**");
    ReflectionTestUtils.setField(webConfig, "marketCorsMethods", "GET, POST, PUT, DELETE, OPTIONS");
    ReflectionTestUtils.setField(webConfig, "marketCorsAllowedOriginPatterns", "*");
    ReflectionTestUtils.setField(webConfig, "marketCorsAllowedOriginMaxAge", 3600);

    when(corsRegistry.addMapping(anyString())).thenReturn(corsRegistration);
    when(corsRegistration.allowedOriginPatterns(anyString())).thenReturn(corsRegistration);
    when(corsRegistration.allowedMethods(any(String[].class))).thenReturn(corsRegistration);
    when(corsRegistration.allowedHeaders(any(String[].class))).thenReturn(corsRegistration);
    when(corsRegistration.maxAge(anyLong())).thenReturn(corsRegistration);
  }

  @Test
  void shouldApplyCorsMappingFromConfiguration() {
    webConfig.addCorsMappings(corsRegistry);

    verify(corsRegistry).addMapping("/**");
    verify(corsRegistration).allowedOriginPatterns("*");
    verify(corsRegistration).maxAge(3600);
  }

  @Test
  void shouldSplitAllowedMethodsFromCommaSeparatedProperty() {
    webConfig.addCorsMappings(corsRegistry);

    ArgumentCaptor<String[]> methodsCaptor = ArgumentCaptor.forClass(String[].class);
    verify(corsRegistration).allowedMethods(methodsCaptor.capture());
    assertArrayEquals(new String[] {"GET", "POST", "PUT", "DELETE", "OPTIONS"}, methodsCaptor.getValue());
  }

  @Test
  void shouldUseWildcardHeadersByDefault() {
    webConfig.addCorsMappings(corsRegistry);

    ArgumentCaptor<String[]> headersCaptor = ArgumentCaptor.forClass(String[].class);
    verify(corsRegistration).allowedHeaders(headersCaptor.capture());
    assertArrayEquals(new String[] {"*"}, headersCaptor.getValue());
  }
}
