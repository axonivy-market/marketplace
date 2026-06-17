package com.axonivy.market.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

public class PasskeyCondition implements Condition {
  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    return StringUtils.hasText(context.getEnvironment().getProperty("market.passkey.rp-id"))
        && StringUtils.hasText(context.getEnvironment().getProperty("market.passkey.origins"));
  }
}
