package com.axonivy.market.service.impl;

import com.axonivy.market.entity.User;
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
}
