package com.axonivy.market.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityFeatureTest {
  @Test
  void testOfReturnsDependabotWhenNameEndsWithDependabot() {
    assertEquals(SecurityFeature.DEPENDABOT,
        SecurityFeature.of("Dependabot"),
        "Expected 'Dependabot' to map to SecurityFeature.DEPENDABOT");
  }

  @Test
  void testOfReturnsSecretScanningWhenNameEndsWithSecretScanning() {
    assertEquals(SecurityFeature.SECRET_SCANNING,
        SecurityFeature.of("GitHub Secret Scanning"),
        "Expected string ending with 'Secret Scanning' to map to SECRET_SCANNING");
  }

  @Test
  void testOfReturnsCodeScanningWhenNameEndsWithCodeScanning() {
    assertEquals(SecurityFeature.CODE_SCANNING,
        SecurityFeature.of("Advanced Code Scanning"),
        "Expected string ending with 'Code Scanning' to map to CODE_SCANNING");
  }

  @Test
  void testOfReturnsBranchProtectionWhenNameEndsWithBranchProtection() {
    assertEquals(SecurityFeature.BRANCH_PROTECTION,
        SecurityFeature.of("Default Branch Protection"),
        "Expected string ending with 'Branch Protection' to map to BRANCH_PROTECTION");
  }

  @Test
  void testOfIsCaseInsensitive() {
    assertEquals(SecurityFeature.DEPENDABOT,
        SecurityFeature.of("dependabot"),
        "Expected lowercase input to match DEPENDABOT");

    assertEquals(SecurityFeature.SECRET_SCANNING,
        SecurityFeature.of("secret scanning"),
        "Expected lowercase input to match SECRET_SCANNING");

    assertEquals(SecurityFeature.CODE_SCANNING,
        SecurityFeature.of("CODE SCANNING"),
        "Expected uppercase input to match CODE_SCANNING");
  }

  @Test
  void testOfReturnsNullForInvalidValue() {
    assertNull(SecurityFeature.of("Unknown Feature"),
        "Expected unknown feature to return null");
  }

  @Test
  void testOfReturnsNullForNullInput() {
    assertNull(SecurityFeature.of(null),
        "Expected null input to return null");
  }

  @Test
  void testEnumValuesHaveCorrectSecurityLabel() {
    assertEquals("Dependabot", SecurityFeature.DEPENDABOT.getSecurityLabel(),
        "Expected DEPENDABOT to have label 'Dependabot'");

    assertEquals("Secret Scanning", SecurityFeature.SECRET_SCANNING.getSecurityLabel(),
        "Expected SECRET_SCANNING to have label 'Secret Scanning'");

    assertEquals("Code Scanning", SecurityFeature.CODE_SCANNING.getSecurityLabel(),
        "Expected CODE_SCANNING to have label 'Code Scanning'");

    assertEquals("Branch Protection", SecurityFeature.BRANCH_PROTECTION.getSecurityLabel(),
        "Expected BRANCH_PROTECTION to have label 'Branch Protection'");
  }
}