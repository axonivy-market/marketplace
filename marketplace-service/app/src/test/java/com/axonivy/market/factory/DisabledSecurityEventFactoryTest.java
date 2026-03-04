package com.axonivy.market.factory;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.enums.SecurityFeature;
import com.axonivy.market.github.model.DisabledSecurityEvent;
import com.axonivy.market.github.model.ProductSecurityInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.axonivy.market.enums.AccessLevel.DISABLED;
import static com.axonivy.market.enums.AccessLevel.ENABLED;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DisabledSecurityEventFactoryTest extends BaseSetup {

  @Test
  void testFromReturnsEmptyList() {
    ProductSecurityInfo productSecurityInfo = mockProductSecurityInfo();

    productSecurityInfo.setArchived(true);
    List<DisabledSecurityEvent> event1 = DisabledSecurityEventFactory.from(productSecurityInfo);
    assertThat(event1).as("Archived repositories must not produce security events").isEmpty();

    productSecurityInfo.setVisibility("PRIVATE");
    List<DisabledSecurityEvent> event2 = DisabledSecurityEventFactory.from(productSecurityInfo);
    assertThat(event2).as("Non-public repositories must not produce security events").isEmpty();
  }

  @Test
  void testFromWhenDependabotIsDisabled() {
    ProductSecurityInfo productSecurityInfo = mockProductSecurityInfo();
    productSecurityInfo.setDependabot(dependabot(DISABLED));

    List<DisabledSecurityEvent> events = DisabledSecurityEventFactory.from(productSecurityInfo);

    assertThat(events)
        .as("Disabled Dependabot must be considered as one security event")
        .hasSize(1)
        .extracting(DisabledSecurityEvent::getFeature)
        .containsExactly(SecurityFeature.DEPENDABOT);
  }

  @Test
  void testFromWithAllDisabledSecurityFeatures() {
    ProductSecurityInfo productSecurityInfo = mockProductSecurityInfo();
    productSecurityInfo.setDependabot(dependabot(DISABLED));
    productSecurityInfo.setSecretScanning(secretScanning(DISABLED));
    productSecurityInfo.setCodeScanning(codeScanning(DISABLED));
    productSecurityInfo.setBranchProtectionEnabled(false);

    List<DisabledSecurityEvent> events = DisabledSecurityEventFactory.from(productSecurityInfo);

    assertThat(events)
        .as("All disabled security features must produce corresponding events")
        .extracting(DisabledSecurityEvent::getFeature)
        .containsExactlyInAnyOrder(
            SecurityFeature.DEPENDABOT,
            SecurityFeature.SECRET_SCANNING,
            SecurityFeature.CODE_SCANNING,
            SecurityFeature.BRANCH_PROTECTION
        );
  }

  @Test
  void testFromWhenAllFeaturesAreEnabled() {
    ProductSecurityInfo productSecurityInfo = mockProductSecurityInfo();
    productSecurityInfo.setDependabot(dependabot(ENABLED));
    productSecurityInfo.setSecretScanning(secretScanning(ENABLED));
    productSecurityInfo.setCodeScanning(codeScanning(ENABLED));
    productSecurityInfo.setBranchProtectionEnabled(true);

    List<DisabledSecurityEvent> events = DisabledSecurityEventFactory.from(productSecurityInfo);

    assertThat(events).as("No events should be created when all security features are enabled").isEmpty();
  }
}
