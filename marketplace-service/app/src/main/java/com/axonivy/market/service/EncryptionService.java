package com.axonivy.market.service;

public interface EncryptionService {

  String encrypt(String value);
  String decrypt(String value);

}
