package com.axonivy.market.service.impl;

import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.repository.GithubUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GithubUserServiceImplTest {

  @Mock
  private GithubUserRepository githubUserRepository;

  @InjectMocks
  private GithubUserServiceImpl userService;

  private GithubUser githubUser;

  @BeforeEach
  void setUp() {
    githubUser = new GithubUser();
    githubUser.setId("1");
    githubUser.setName("John Doe");
    githubUser.setUsername("john.doe@example.com");
  }

  @Test
  void testGetAllUsers() {
    when(githubUserRepository.findAll()).thenReturn(Collections.singletonList(githubUser));

    List<GithubUser> result = userService.getAllUsers();

    assertNotNull(result, "Result list from getAllUsers() should not be null");
    assertEquals(1, result.size(), "Result list should contain exactly one user");
    assertEquals(githubUser.getId(), result.get(0).getId(),
        "The ID of the returned user should match the mocked githubUser");
    verify(githubUserRepository, times(1)).findAll();
  }

  @Test
  void testFindUser() throws NotFoundException {
    String userId = "1";

    when(githubUserRepository.findById(userId)).thenReturn(Optional.of(githubUser));

    GithubUser result = userService.findUser(userId);

    assertNotNull(result, "Returned user should not be null");
    assertEquals(userId, result.getId(), "The returned user ID should match the requested userId");
    verify(githubUserRepository, times(1)).findById(userId);
  }

  @Test
  void testFindUserNotFound() {
    String userId = "1";

    when(githubUserRepository.findById(userId)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> userService.findUser(userId),
        "Expected NotFoundException when user is not found"
    );

    assertEquals(ErrorCode.USER_NOT_FOUND.getCode(), exception.getCode(),
        "Exception code should indicate USER_NOT_FOUND");

    verify(githubUserRepository, times(1)).findById(userId);
  }

  @Test
  void testCreateUser() {
    when(githubUserRepository.save(any(GithubUser.class))).thenReturn(githubUser);

    GithubUser result = userService.createUser(githubUser);

    assertNotNull(result, "Created user should not be null");
    assertEquals(githubUser.getId(), result.getId(), "User ID should match the saved entity");
    assertEquals(githubUser.getName(), result.getName(), "User name should match the saved entity");
    assertEquals(githubUser.getUsername(), result.getUsername(), "Username should match the saved entity");

    verify(githubUserRepository, times(1)).save(githubUser);
  }
}
