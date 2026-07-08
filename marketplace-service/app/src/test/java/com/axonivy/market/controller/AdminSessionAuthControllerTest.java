package com.axonivy.market.controller;

import com.axonivy.market.model.UserInfo;
import com.axonivy.market.service.AdminSessionAuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class AdminSessionAuthControllerTest {
  @Mock
  private AdminSessionAuthService adminSessionAuthService;

  @InjectMocks
  private AdminSessionAuthController controller;

  @Test
  void sessionReturnsUnauthorizedWhenUserMissing() {
    ResponseEntity<UserInfo> response = controller.session(null);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void sessionReturnsCurrentUserWhenPresent() {
    UserInfo currentUser = new UserInfo();
    currentUser.setUsername("octopus");

    ResponseEntity<UserInfo> response = controller.session(currentUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("octopus", response.getBody().getUsername());
  }
}
