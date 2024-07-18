package com.axonivy.market.service.impl;

import com.axonivy.market.entity.User;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.repository.UserRepository;
import com.axonivy.market.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  public UserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  @Override
  public User findUser(String id) throws NotFoundException {
    return userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, "Not found user with id: " + id));
  }

  @Override
  public User createUser(User user) {
    return userRepository.save(user);
  }
}
