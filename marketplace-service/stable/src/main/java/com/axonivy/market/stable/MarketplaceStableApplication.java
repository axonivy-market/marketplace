package com.axonivy.market.stable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static com.axonivy.market.core.constants.BasePackageConstants.*;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {CORE_BASE_PACKAGE_NAME, STABLE_PACKAGE_NAME})
@EnableJpaRepositories(basePackages = CORE_BASE_PACKAGE_REPO_NAME)
@EntityScan(basePackages = CORE_BASE_PACKAGE_ENTITY_NAME)
public class MarketplaceStableApplication {
  public static void main(String[] args) {
    SpringApplication.run(MarketplaceStableApplication.class, args);
  }
}
