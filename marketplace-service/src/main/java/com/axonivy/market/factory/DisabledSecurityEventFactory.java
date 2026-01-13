package com.axonivy.market.factory;

import com.axonivy.market.github.model.CodeScanning;
import com.axonivy.market.github.model.Dependabot;
import com.axonivy.market.github.model.DisabledSecurityEvent;
import com.axonivy.market.github.model.ProductSecurityInfo;
import com.axonivy.market.github.model.SecretScanning;
import com.axonivy.market.enums.SecurityFeature;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.kohsuke.github.GHRepository;

import java.util.ArrayList;
import java.util.List;

import static com.axonivy.market.enums.AccessLevel.DISABLED;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DisabledSecurityEventFactory {

  public static List<DisabledSecurityEvent> from(ProductSecurityInfo info) {
    if (!isRepoEligible(info)) {
      return List.of();
    }

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

  private static boolean isDisabled(Object feature) {
    return isDependabotDisabled(feature) || isSecretScanningDisabled(feature) || isCodeScanningDisabled(feature);
  }

  private static boolean isDependabotDisabled(Object feature) {
    return feature instanceof Dependabot dependabot && DISABLED == dependabot.getStatus();
  }

  private static boolean isSecretScanningDisabled(Object feature) {
    return feature instanceof SecretScanning secretScanning && DISABLED == secretScanning.getStatus();
  }

  private static boolean isCodeScanningDisabled(Object feature) {
    return feature instanceof CodeScanning codeScanning && DISABLED == codeScanning.getStatus();
  }

  private static DisabledSecurityEvent event(ProductSecurityInfo info, SecurityFeature feature) {
    return new DisabledSecurityEvent(info.getRepoName(), feature, DISABLED);
  }

  private static boolean isRepoEligible(ProductSecurityInfo info) {
    return !info.isArchived() && GHRepository.Visibility.PUBLIC.name().equalsIgnoreCase(info.getVisibility());
  }
}