package com.axonivy.market.config;

import com.fasterxml.jackson.databind.Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Conditional;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.web.webauthn.jackson.WebauthnJackson2Module;
import org.springframework.security.web.webauthn.management.JdbcPublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.JdbcUserCredentialRepository;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;

@Configuration
@Conditional(PasskeyCondition.class)
public class PasskeyPersistenceConfig {
  @Bean
  public PublicKeyCredentialUserEntityRepository publicKeyCredentialUserEntityRepository(
      JdbcOperations jdbcOperations) {
    return new JdbcPublicKeyCredentialUserEntityRepository(jdbcOperations);
  }

  @Bean
  public UserCredentialRepository userCredentialRepository(JdbcOperations jdbcOperations) {
    return new JdbcUserCredentialRepository(jdbcOperations);
  }

  @Bean
  public Module webauthnJacksonModule() {
    return new WebauthnJackson2Module();
  }
}
