package com.axonivy.market.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EncryptionServiceImplTest {

  private static final String ENCRYPTION_KEY = "testEncryptionKey123";

  @Test
  void testEncryptAndDecrypt() {
    EncryptionServiceImpl service = new EncryptionServiceImpl(ENCRYPTION_KEY);

    String original = "sensitiveData";
    String encrypted = service.encrypt(original);

    assertNotNull(encrypted, "Encrypted value should not be null");
    assertNotEquals(original, encrypted, "Encrypted value should differ from the original");

    String decrypted = service.decrypt(encrypted);
    assertEquals(original, decrypted, "Decrypted value should match the original");
  }

  @Test
  void testEncryptReturnsNullForNullInput() {
    EncryptionServiceImpl service = new EncryptionServiceImpl(ENCRYPTION_KEY);

    assertNull(service.encrypt(null), "Encrypting null should return null");
  }

  @Test
  void testDecryptReturnsNullForNullInput() {
    EncryptionServiceImpl service = new EncryptionServiceImpl(ENCRYPTION_KEY);

    assertNull(service.decrypt(null), "Decrypting null should return null");
  }

  @Test
  void testEncryptionDisabledWhenKeyIsBlank() {
    EncryptionServiceImpl service = new EncryptionServiceImpl("");

    String original = "sensitiveData";
    assertEquals(original, service.encrypt(original),
        "When encryption is disabled, encrypt should return the original value");
    assertEquals(original, service.decrypt(original),
        "When encryption is disabled, decrypt should return the original value");
  }

  @Test
  void testEncryptionDisabledWhenKeyIsNull() {
    EncryptionServiceImpl service = new EncryptionServiceImpl(null);

    String original = "sensitiveData";
    assertEquals(original, service.encrypt(original),
        "When encryption key is null, encrypt should return the original value");
    assertEquals(original, service.decrypt(original),
        "When encryption key is null, decrypt should return the original value");
  }

  @Test
  void testEncryptProducesDifferentCiphertextEachTime() {
    EncryptionServiceImpl service = new EncryptionServiceImpl(ENCRYPTION_KEY);

    String original = "sensitiveData";
    String encrypted1 = service.encrypt(original);
    String encrypted2 = service.encrypt(original);

    assertNotEquals(encrypted1, encrypted2,
        "Spring TextEncryptor should produce different ciphertext on each call due to random IV");

    assertEquals(original, service.decrypt(encrypted1), "Decrypted value of encrypted1 should match the original");
    assertEquals(original, service.decrypt(encrypted2), "Decrypted value of encrypted2 should match the original");
  }

  @Test
  void testEncryptAndDecryptEmptyString() {
    EncryptionServiceImpl service = new EncryptionServiceImpl(ENCRYPTION_KEY);

    String encrypted = service.encrypt("");
    assertNotNull(encrypted);

    String decrypted = service.decrypt(encrypted);
    assertEquals("", decrypted, "Decrypting an encrypted empty string should return empty string");
  }
}

