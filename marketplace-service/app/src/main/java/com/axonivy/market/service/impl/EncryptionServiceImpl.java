package com.axonivy.market.service.impl;

import com.axonivy.market.service.EncryptionService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class EncryptionServiceImpl implements EncryptionService {

  private static final String SALT = "a1b2c3d4e5f6a7b8";

  private TextEncryptor encryptor;
  private boolean enabled;

  public EncryptionServiceImpl(
      @Value("${encryption.key:}") String encryptionKey) {

    try {
      if (StringUtils.isNotBlank(encryptionKey)) {
        this.encryptor = Encryptors.text(encryptionKey, SALT);
        this.enabled = true;
      }
    } catch (Exception e) {
      log.warn("Encryption is disabled due to invalid configuration", e);
    }
  }

  @Override
  public String encrypt(String value) {
    if (!enabled || value == null) {
      return value;
    }
    return encryptor.encrypt(value);
  }

  @Override
  public String decrypt(String value) {
    if (!enabled || value == null) {
      return value;
    }
    return encryptor.decrypt(value);
  }
}