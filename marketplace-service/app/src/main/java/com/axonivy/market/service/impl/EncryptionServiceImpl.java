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

  public EncryptionServiceImpl(@Value("${market.encryption.key:}") String encryptionKey,
      @Value("${market.encryption.salt:}") String salt) {

    try {
      if (StringUtils.isNotBlank(encryptionKey)) {
        this.encryptor = Encryptors.text(encryptionKey, salt);
      }
    } catch (IllegalStateException e) {
      log.warn("Encryption is disabled due to invalid configuration", e);
    }
  }

  @Override
  public String encrypt(String value) {
    if (encryptor == null) {
      throw new IllegalStateException("Encryption is not configured (market.encryption.key)");
    }
    return encryptor.encrypt(StringUtils.defaultString(value));
  }

  @Override
  public String decrypt(String value) {
    if (encryptor == null) {
      throw new IllegalStateException("Encryption is not configured (market.encryption.key)");
    }
    try {
      return encryptor.decrypt(StringUtils.defaultString(value));
    } catch (IllegalArgumentException ex) {
      return StringUtils.EMPTY;
    }
  }
}