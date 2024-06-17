package com.axonivy.market.controller;

import static com.axonivy.market.constants.RequestMappingConstants.ROOT;
import static com.axonivy.market.constants.RequestMappingConstants.SWAGGER_URL;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.axonivy.market.model.Message;

@RestController
@RequestMapping(ROOT)
public class AppController {

  @GetMapping
  public ResponseEntity<Message> root() {
    var swaggerURL = ServletUriComponentsBuilder.fromCurrentContextPath().path(SWAGGER_URL).toUriString();
    var message = new Message();
    message.setErrorCode("0");
    message.setMessageDetails("Welcome to Marketplace API");
    message.setHelpText(
        "This is a REST APIs for Marketplace website - No user interface. Try with %s".formatted(swaggerURL));

    return new ResponseEntity<>(message, HttpStatus.OK);
  }
}
