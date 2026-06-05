package com.axonivy.market.config;

import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.service.AppSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailSenderService {

  private final AppSettingService appSettingService;

  public JavaMailSender createMailSender() {
    var sender = new JavaMailSenderImpl();
    sender.setHost(appSettingService.getValueByKey(AppSettingKey.MAIL_HOST));
    sender.setPort(Integer.parseInt(appSettingService.getValueByKey(AppSettingKey.MAIL_PORT)));
    sender.setUsername(appSettingService.getValueByKey(AppSettingKey.MAIL_USERNAME));
    sender.setPassword(appSettingService.getValueByKey(AppSettingKey.MAIL_PASSWORD));
    var props = sender.getJavaMailProperties();
    props.put("mail.smtp.auth", appSettingService.getValueByKey(AppSettingKey.MAIL_SMTP_AUTH));
    props.put("mail.smtp.starttls.enable", appSettingService.getValueByKey(AppSettingKey.MAIL_SMTP_STARTTLS_ENABLE));

    return sender;
  }
}