package com.axonivy.market;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.springframework.boot.builder.SpringApplicationBuilder;

class ServletInitializerTest {

  @Test
  void testConfigureReturnsBuilderInstance() {
    ServletInitializer initializer = new ServletInitializer();

    SpringApplicationBuilder builder = initializer.configure(new SpringApplicationBuilder());

    assertInstanceOf(SpringApplicationBuilder.class, builder,
        "configure() should return an instance of SpringApplicationBuilder");
  }
}
