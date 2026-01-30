package com.axonivy.market.neo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static com.axonivy.market.core.constants.BasePackageConstants.*;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {CORE_BASE_PACKAGE_NAME, "com.axonivy.market.neo"})
@EnableJpaRepositories(basePackages = CORE_BASE_PACKAGE_REPO_NAME)
@EntityScan(basePackages = CORE_BASE_PACKAGE_ENTITY_NAME)
public class MarketNeoApplication {
  public static void main(String[] args) {
    SpringApplication.run(MarketNeoApplication.class, args);
  }
}
