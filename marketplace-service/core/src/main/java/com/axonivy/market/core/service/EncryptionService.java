package com.axonivy.market.core.service;

public interface EncryptionService {

  String encrypt(String value);
  String decrypt(String value);

}
