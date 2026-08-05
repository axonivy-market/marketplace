package com.axonivy.market.core;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
@EnableAutoConfiguration
@AutoConfigurationPackage
@EnableJpaRepositories(basePackages = "com.axonivy.market.core.repository")
public class CoreTestApplication {
}
