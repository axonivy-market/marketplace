package com.axonivy.market.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.axonivy.market.model.Message;

@RestController
@RequestMapping("/")
public class AppController {

  @GetMapping
  public ResponseEntity<Message> init() {
    var message = new Message();
    message.setMessage("Welcome to Markketplace API");
    message.setHelpText("This is a REST APIs for Markketplace website");
    return new ResponseEntity<Message>(message, HttpStatus.OK);
  }
}
