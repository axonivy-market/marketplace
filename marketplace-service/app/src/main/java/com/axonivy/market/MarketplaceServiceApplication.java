package com.axonivy.market;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

import static com.axonivy.market.core.constants.BasePackageConstants.*;

@Log4j2
@EnableAsync
@SpringBootApplication(scanBasePackages = {CORE_BASE_PACKAGE_NAME, APP_PACKAGE_NAME})
@AllArgsConstructor
@EnableJpaAuditing
public class MarketplaceServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(MarketplaceServiceApplication.class, args);
  }

}
