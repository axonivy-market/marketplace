package com.axonivy.market.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@PreAuthorize("hasAuthority('MARKET_ADMIN')")
public @interface Authorized {

  AuthorizationScope scope() default AuthorizationScope.ORGANIZATION_TEAM;

  enum AuthorizationScope {
    ORGANIZATION_TEAM,
    ORGANIZATION,
    TEAM,
    USER
  }
}
