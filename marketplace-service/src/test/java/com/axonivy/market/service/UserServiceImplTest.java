package com.axonivy.market.service;

import com.axonivy.market.entity.User;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.repository.UserRepository;
import com.axonivy.market.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserServiceImpl userService;

  private User user;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId("1");
    user.setName("John Doe");
    user.setUsername("john.doe@example.com");
  }

  @Test
  void testGetAllUsers() {
    when(userRepository.findAll()).thenReturn(Collections.singletonList(user));

    List<User> result = userService.getAllUsers();
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(user.getId(), result.get(0).getId());
    verify(userRepository, times(1)).findAll();
  }

  @Test
  void testFindUser() throws NotFoundException {
    String userId = "1";

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    User result = userService.findUser(userId);
    assertNotNull(result);
    assertEquals(userId, result.getId());
    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  void testFindUser_NotFound() {
    String userId = "1";

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.findUser(userId));
    assertEquals(ErrorCode.USER_NOT_FOUND.getCode(), exception.getCode());
    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  void testCreateUser() {
    when(userRepository.save(any(User.class))).thenReturn(user);

    User result = userService.createUser(user);
    assertNotNull(result);
    assertEquals(user.getId(), result.getId());
    assertEquals(user.getName(), result.getName());
    assertEquals(user.getUsername(), result.getUsername());
    verify(userRepository, times(1)).save(user);
  }
}
