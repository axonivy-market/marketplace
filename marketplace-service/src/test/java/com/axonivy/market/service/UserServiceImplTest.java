package com.axonivy.market.service;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axonivy.market.entity.User;
import com.axonivy.market.repository.UserRepository;
import com.axonivy.market.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @InjectMocks
  private UserServiceImpl employeeService;

  @Mock
  private UserRepository userRepository;

  @Test
  void testFindAllUser() {
    // Mock data and service
    User mockUser = new User();
    mockUser.setId("123");
    mockUser.setUsername("tvtTest");
    mockUser.setPassword("12345");
    List<User> mockResultReturn = List.of(mockUser);
    Mockito.when(userRepository.findAll()).thenReturn(mockResultReturn);

    // exercise
    List<User> result = employeeService.getAllUsers();

    // Verify
    Assertions.assertEquals(result, mockResultReturn);
  }
}
