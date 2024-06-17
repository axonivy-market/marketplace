package com.axonivy.market.controller;

import static com.axonivy.market.constants.RequestMappingConstants.USER_MAPPING;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.axonivy.market.entity.User;
import com.axonivy.market.service.UserService;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(USER_MAPPING)
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public ResponseEntity<List<User>> getAllUsers() {
    return ResponseEntity.ok(userService.getAllUsers());
  }

  @GetMapping("/{id}")
  public ResponseEntity<User> findUser(@PathVariable("id") String id) {
    return ResponseEntity.ok(userService.findUser(id));
  }

  @PostMapping
  public ResponseEntity<User> createUser(@RequestBody User user) {
    User newUser = userService.createUser(user);

    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(newUser.getId())
            .toUri();

    return ResponseEntity.created(location).build();
  }
}
