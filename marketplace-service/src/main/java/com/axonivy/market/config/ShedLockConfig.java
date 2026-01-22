package com.axonivy.market.config;

import static com.axonivy.market.constants.EntityConstants.SHED_LOCK;

import lombok.extern.log4j.Log4j2;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Log4j2
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "${shedlock.defaults.lock-at-most-for}")
public class ShedLockConfig {

  @Bean
  public LockProvider lockProvider(DataSource dataSource) {
    return new JdbcTemplateLockProvider(
        JdbcTemplateLockProvider.Configuration.builder()
            .withJdbcTemplate(new JdbcTemplate(dataSource))
            .withTableName(SHED_LOCK)
            .usingDbTime()
            .build()
    );
  }
}
