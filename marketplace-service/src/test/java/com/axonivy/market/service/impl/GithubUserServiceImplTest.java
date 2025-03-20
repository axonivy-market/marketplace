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
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(githubUser.getId(), result.get(0).getId());
    verify(githubUserRepository, times(1)).findAll();
  }

  @Test
  void testFindUser() throws NotFoundException {
    String userId = "1";

    when(githubUserRepository.findById(userId)).thenReturn(Optional.of(githubUser));

    GithubUser result = userService.findUser(userId);
    assertNotNull(result);
    assertEquals(userId, result.getId());
    verify(githubUserRepository, times(1)).findById(userId);
  }

  @Test
  void testFindUser_NotFound() {
    String userId = "1";

    when(githubUserRepository.findById(userId)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.findUser(userId));
    assertEquals(ErrorCode.USER_NOT_FOUND.getCode(), exception.getCode());
    verify(githubUserRepository, times(1)).findById(userId);
  }

  @Test
  void testCreateUser() {
    when(githubUserRepository.save(any(GithubUser.class))).thenReturn(githubUser);

    GithubUser result = userService.createUser(githubUser);
    assertNotNull(result);
    assertEquals(githubUser.getId(), result.getId());
    assertEquals(githubUser.getName(), result.getName());
    assertEquals(githubUser.getUsername(), result.getUsername());
    verify(githubUserRepository, times(1)).save(githubUser);
  }
}
