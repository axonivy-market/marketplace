package com.axonivy.market.controller;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axonivy.market.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  @Mock
  UserService userService;

  @InjectMocks
  UserController userController;

  @Test
  void testGetAllUser() {
    var result = userController.getAllUser();
    assertNotEquals(null, result);
  }
}
