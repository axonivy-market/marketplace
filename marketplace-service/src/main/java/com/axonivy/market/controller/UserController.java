package com.axonivy.market.controller;

import com.axonivy.market.entity.User;
import com.axonivy.market.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.axonivy.market.constants.RequestMappingConstants.USER_MAPPING;

@RestController
@RequestMapping(USER_MAPPING)
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public ResponseEntity<List<User>> getAllUser() {
    return ResponseEntity.ok(userService.getAllUsers());
  }
}