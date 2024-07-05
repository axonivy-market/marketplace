package com.axonivy.market.service;

import java.util.List;

import com.axonivy.market.entity.User;
import com.axonivy.market.exceptions.model.NotFoundException;

public interface UserService {
  List<User> getAllUsers();
  User createUser(User user);
  User findUser(String id) throws NotFoundException;
}
