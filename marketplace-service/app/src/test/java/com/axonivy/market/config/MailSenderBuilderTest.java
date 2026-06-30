package com.axonivy.market.config;

import com.axonivy.market.enums.AppSettingCategory;
import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.service.AppSettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailSenderBuilderTest {

  @Mock
  private AppSettingService appSettingService;

  private MailSenderBuilder builder;

  private final Map<String, String> mailSettings = Map.ofEntries(
      Map.entry(AppSettingKey.MAIL_HOST.getKey(), "smtp.example.com"),
      Map.entry(AppSettingKey.MAIL_PORT.getKey(), "465"), Map.entry(AppSettingKey.MAIL_USERNAME.getKey(), ""),
      Map.entry(AppSettingKey.MAIL_PASSWORD.getKey(), ""), Map.entry(AppSettingKey.MAIL_SMTP_AUTH.getKey(), "false"),
      Map.entry(AppSettingKey.MAIL_SMTP_STARTTLS_ENABLE.getKey(), "false"));

  @BeforeEach
  void setUp() {
    builder = new MailSenderBuilder(appSettingService);
    when(appSettingService.getByCategory(AppSettingCategory.MAIL)).thenReturn(mailSettings);
  }

  @Test
  void testBuildCreatesMailSenderWithConfiguredProperties() {
    JavaMailSender sender = builder.build();
    assertNotNull(sender, "JavaMailSender should not be null");
    assertInstanceOf(JavaMailSenderImpl.class, sender, "Should return a JavaMailSenderImpl");

    JavaMailSenderImpl impl = (JavaMailSenderImpl) sender;
    assertEquals("smtp.example.com", impl.getHost(), "Host should match");
    assertEquals(465, impl.getPort(), "Port should match");
    assertEquals("", impl.getUsername(), "Username should match");
    assertEquals("", impl.getPassword(), "Password should match");
    assertFalse((Boolean) impl.getJavaMailProperties().get("mail.smtp.auth"), "SMTP auth should be disabled");
    assertFalse((Boolean) impl.getJavaMailProperties().get("mail.smtp.starttls.enable"), "STARTTLS should be disabled");
  }

  @Test
  void testBuildWithCustomPort() {
    JavaMailSender sender = builder.build();
    JavaMailSenderImpl impl = (JavaMailSenderImpl) sender;
    assertEquals(465, impl.getPort(), "Port should be 465");
    assertFalse((Boolean) impl.getJavaMailProperties().get("mail.smtp.auth"), "SMTP auth should be disabled");
    assertFalse((Boolean) impl.getJavaMailProperties().get("mail.smtp.starttls.enable"), "STARTTLS should be disabled");
  }

  @Test
  void testBuildWithEmptyCredentials() {
    JavaMailSender sender = builder.build();
    JavaMailSenderImpl impl = (JavaMailSenderImpl) sender;
    assertEquals("", impl.getUsername(), "Username should be empty");
    assertEquals("", impl.getPassword(), "Password should be empty");
  }
}

