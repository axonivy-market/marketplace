package com.axonivy.market.config;

import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.service.AppSettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailSenderBuilderTest {

  @Mock
  private AppSettingService appSettingService;

  private MailSenderBuilder builder;

  @BeforeEach
  void setUp() {
    builder = new MailSenderBuilder(appSettingService);
  }

  @Test
  void testBuildCreatesMailSenderWithConfiguredProperties() {
    when(appSettingService.getStringValueByKey(AppSettingKey.MAIL_HOST)).thenReturn("smtp.example.com");
    when(appSettingService.getIntegerValueByKey(AppSettingKey.MAIL_PORT)).thenReturn(587);
    when(appSettingService.getStringValueByKey(AppSettingKey.MAIL_USERNAME)).thenReturn("user@example.com");
    when(appSettingService.getStringValueByKey(AppSettingKey.MAIL_PASSWORD)).thenReturn("secret");
    when(appSettingService.getBooleanValueByKey(AppSettingKey.MAIL_SMTP_AUTH)).thenReturn(true);
    when(appSettingService.getBooleanValueByKey(AppSettingKey.MAIL_SMTP_STARTTLS_ENABLE)).thenReturn(true);

    JavaMailSender sender = builder.build();

    assertNotNull(sender, "JavaMailSender should not be null");
    assertInstanceOf(JavaMailSenderImpl.class, sender, "Should return a JavaMailSenderImpl");

    JavaMailSenderImpl impl = (JavaMailSenderImpl) sender;
    assertEquals("smtp.example.com", impl.getHost(), "Host should match");
    assertEquals(587, impl.getPort(), "Port should match");
    assertEquals("user@example.com", impl.getUsername(), "Username should match");
    assertEquals("secret", impl.getPassword(), "Password should match");
    assertTrue((Boolean) impl.getJavaMailProperties().get("mail.smtp.auth"), "SMTP auth should be enabled");
    assertTrue((Boolean) impl.getJavaMailProperties().get("mail.smtp.starttls.enable"),
        "STARTTLS should be enabled");
  }

  @Test
  void testBuildWithCustomPort() {
    when(appSettingService.getStringValueByKey(AppSettingKey.MAIL_HOST)).thenReturn("mail.test.com");
    when(appSettingService.getIntegerValueByKey(AppSettingKey.MAIL_PORT)).thenReturn(465);
    when(appSettingService.getStringValueByKey(AppSettingKey.MAIL_USERNAME)).thenReturn("");
    when(appSettingService.getStringValueByKey(AppSettingKey.MAIL_PASSWORD)).thenReturn("");
    when(appSettingService.getBooleanValueByKey(AppSettingKey.MAIL_SMTP_AUTH)).thenReturn(false);
    when(appSettingService.getBooleanValueByKey(AppSettingKey.MAIL_SMTP_STARTTLS_ENABLE)).thenReturn(false);

    JavaMailSender sender = builder.build();

    JavaMailSenderImpl impl = (JavaMailSenderImpl) sender;
    assertEquals(465, impl.getPort(), "Port should be 465");
    assertFalse((Boolean) impl.getJavaMailProperties().get("mail.smtp.auth"), "SMTP auth should be disabled");
    assertFalse((Boolean) impl.getJavaMailProperties().get("mail.smtp.starttls.enable"),
        "STARTTLS should be disabled");
  }

  @Test
  void testBuildWithEmptyCredentials() {
    when(appSettingService.getStringValueByKey(AppSettingKey.MAIL_HOST)).thenReturn("");
    when(appSettingService.getIntegerValueByKey(AppSettingKey.MAIL_PORT)).thenReturn(587);
    when(appSettingService.getStringValueByKey(AppSettingKey.MAIL_USERNAME)).thenReturn("");
    when(appSettingService.getStringValueByKey(AppSettingKey.MAIL_PASSWORD)).thenReturn("");
    when(appSettingService.getBooleanValueByKey(AppSettingKey.MAIL_SMTP_AUTH)).thenReturn(true);
    when(appSettingService.getBooleanValueByKey(AppSettingKey.MAIL_SMTP_STARTTLS_ENABLE)).thenReturn(true);

    JavaMailSender sender = builder.build();

    JavaMailSenderImpl impl = (JavaMailSenderImpl) sender;
    assertEquals("", impl.getHost(), "Host should be empty");
    assertEquals("", impl.getUsername(), "Username should be empty");
    assertEquals("", impl.getPassword(), "Password should be empty");
  }
}

