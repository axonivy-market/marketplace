package com.axonivy.market.factory;

import com.axonivy.market.github.model.CodeScanning;
import com.axonivy.market.github.model.Dependabot;
import com.axonivy.market.github.model.DisabledSecurityEvent;
import com.axonivy.market.github.model.ProductSecurityInfo;
import com.axonivy.market.github.model.SecretScanning;
import com.axonivy.market.github.model.SecurityFeature;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

import static com.axonivy.market.enums.AccessLevel.DISABLED;

import org.springframework.stereotype.Component;

@Component
@Log4j2
public class DisabledSecurityEventFactory {

  public List<DisabledSecurityEvent> from(ProductSecurityInfo info) {
    List<DisabledSecurityEvent> events = new ArrayList<>();

    if (isDisabled(info.getDependabot())) {
      events.add(event(info, SecurityFeature.DEPENDABOT));
    }

    if (isDisabled(info.getSecretScanning())) {
      events.add(event(info, SecurityFeature.SECRET_SCANNING));
    }

    if (isDisabled(info.getCodeScanning())) {
      events.add(event(info, SecurityFeature.CODE_SCANNING));
    }

    if (!info.isBranchProtectionEnabled()) {
      events.add(event(info, SecurityFeature.BRANCH_PROTECTION));
    }

    return events;
  }

  private boolean isDisabled(Object feature) {
    if (feature instanceof Dependabot d) {
      return d.getStatus() == DISABLED;
    }
    if (feature instanceof SecretScanning s) {
      return s.getStatus() == DISABLED;
    }
    if (feature instanceof CodeScanning c) {
      return c.getStatus() == DISABLED;
    }
    return false;
  }

  private DisabledSecurityEvent event(
      ProductSecurityInfo info,
      SecurityFeature feature
  ) {
    return new DisabledSecurityEvent(
        info.getRepoName(),
        feature,
        DISABLED
    );
  }
}

