package com.axonivy.market.config;

import com.axonivy.market.enums.AppSettingCategory;
import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.service.AppSettingService;
import com.axonivy.market.util.SettingValueParser;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MailSenderBuilder {

  private final AppSettingService appSettingService;

  public JavaMailSender build() {
    var sender = new JavaMailSenderImpl();
    Map<String, String> mailSettings = appSettingService.getByCategory(AppSettingCategory.MAIL);
    var mailPort = SettingValueParser.parseInteger(mailSettings.get(AppSettingKey.MAIL_PORT.getKey()),
        AppSettingKey.MAIL_PORT);

    sender.setHost(mailSettings.get(AppSettingKey.MAIL_HOST.getKey()));
    if (mailPort != null) {
      sender.setPort(mailPort);
    }
    sender.setUsername(mailSettings.get(AppSettingKey.MAIL_USERNAME.getKey()));
    sender.setPassword(mailSettings.get(AppSettingKey.MAIL_PASSWORD.getKey()));

    var props = sender.getJavaMailProperties();
    boolean auth = SettingValueParser.parseBoolean(mailSettings.get(AppSettingKey.MAIL_SMTP_AUTH.getKey()));
    boolean enable = SettingValueParser.parseBoolean(
        mailSettings.get(AppSettingKey.MAIL_SMTP_STARTTLS_ENABLE.getKey()));
    props.put("mail.smtp.auth", auth);
    props.put("mail.smtp.starttls.enable", enable);

    return sender;
  }
}