package com.axonivy.market.service;

import com.axonivy.market.entity.User;
import com.axonivy.market.exceptions.model.NotFoundException;

import java.util.List;

public interface UserService {
  List<User> getAllUsers();
  User createUser(User user);
  User findUser(String id) throws NotFoundException;
}
