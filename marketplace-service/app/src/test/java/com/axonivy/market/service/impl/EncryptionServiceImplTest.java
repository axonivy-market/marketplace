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
  void testDecryptReturnsEmptyForNullInput() {
    EncryptionServiceImpl service = new EncryptionServiceImpl(ENCRYPTION_KEY);

    String result = service.decrypt("null");
    assertEquals("", result,
        "Decrypting invalid should return empty string due to caught IllegalArgumentException");
  }

  @Test
  void testDecryptThrowsNPEWhenKeyIsBlank() {
    EncryptionServiceImpl service = new EncryptionServiceImpl("");

    assertThrows(IllegalStateException.class, () -> service.decrypt("someValue"),
        "Decrypt should throw NullPointerException when encryptor is null");
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
    assertNotNull(encrypted, "Encrypting an empty string should not return null");

    String decrypted = service.decrypt(encrypted);
    assertEquals("", decrypted, "Decrypting an encrypted empty string should return empty string");
  }

  @Test
  void testDecryptInvalidCiphertextReturnsEmpty() {
    EncryptionServiceImpl service = new EncryptionServiceImpl(ENCRYPTION_KEY);

    String result = service.decrypt("not-a-valid-ciphertext");
    assertEquals("", result, "Decrypting invalid ciphertext should return empty string");
  }
}
