package com.axonivy.market;

import com.axonivy.market.service.ExternalDocumentService;
import com.axonivy.market.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

import static com.axonivy.market.core.constants.BasePackageConstants.*;

@Log4j2
@EnableAsync
@EnableScheduling
@SpringBootApplication(scanBasePackages = {CORE_BASE_PACKAGE_NAME, APP_PACKAGE_NAME})
@EnableCaching
@EnableJpaAuditing
public class MarketplaceServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(MarketplaceServiceApplication.class, args);
  }
}
