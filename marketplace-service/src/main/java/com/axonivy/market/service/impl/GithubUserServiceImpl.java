package com.axonivy.market.service.impl;

import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.repository.GithubUserRepository;
import com.axonivy.market.service.GithubUserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GithubUserServiceImpl implements GithubUserService {

  private final GithubUserRepository githubUserRepository;

  public GithubUserServiceImpl(GithubUserRepository githubUserRepository) {
    this.githubUserRepository = githubUserRepository;
  }

  @Override
  public List<GithubUser> getAllUsers() {
    return githubUserRepository.findAll();
  }

  @Override
  public GithubUser findUser(String id) throws NotFoundException {
    return githubUserRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, "Not found user with id: " + id));
  }

  @Override
  public GithubUser createUser(GithubUser githubUser) {
    return githubUserRepository.save(githubUser);
  }
}
