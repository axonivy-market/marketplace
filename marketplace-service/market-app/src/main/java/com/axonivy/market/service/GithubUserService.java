package com.axonivy.market.service;

import com.axonivy.market.core.exceptions.model.NotFoundException;
import com.axonivy.market.entity.GithubUser;

import java.util.List;

public interface GithubUserService {
  List<GithubUser> getAllUsers();

  GithubUser createUser(GithubUser githubUser);

  GithubUser findUser(String id) throws NotFoundException;
}
