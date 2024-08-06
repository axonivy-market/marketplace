package com.axonivy.market.controller;

import static com.axonivy.market.constants.RequestMappingConstants.ROOT;
import static com.axonivy.market.constants.RequestMappingConstants.SWAGGER_URL;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.model.Message;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping(ROOT)
public class AppController {

  @GetMapping
  public ResponseEntity<Message> root() {
    var message = new Message();
    message.setHelpCode(ErrorCode.SUCCESSFUL.getCode());
    message.setMessageDetails(
        "Marketplace API is a REST APIs for Marketplace website. Try with %s".formatted(extractSwaggerUrl()));
    message.setHelpText(ErrorCode.SUCCESSFUL.getHelpText());
    return new ResponseEntity<>(message, HttpStatus.OK);
  }

  private String extractSwaggerUrl() {
    var swaggerURL = SWAGGER_URL;
    try {
      swaggerURL = ServletUriComponentsBuilder.fromCurrentContextPath().path(SWAGGER_URL).toUriString();
    } catch (Exception e) {
      log.error("Cannot get Swagger Url", e);
    }
    return swaggerURL;
  }
}
