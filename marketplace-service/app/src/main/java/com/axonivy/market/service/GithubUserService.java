package com.axonivy.market.service;

import com.axonivy.market.core.exceptions.model.NotFoundException;
import com.axonivy.market.entity.GithubUser;

import java.util.List;

public interface GithubUserService {

  /**
   * <p>
   * Get all users
   * </p>
   *
   * @param
   *              type {@link }
   * @return {@link List<GithubUser>}
   * @author tvtphuc
   */
  List<GithubUser> getAllUsers();

  /**
   * <p>
   * Create a new user by GitHub user
   * </p>
   *
   * @param  githubUser
   *              type {@link GithubUser}
   * @return {@link GithubUser}
   * @author tvtphuc
   */
  GithubUser createUser(GithubUser githubUser);

  /**
   * <p>
   * Find user by GitHub id
   * </p>
   *
   * @param  id
   *              type {@link String}
   * @return {@link GithubUser}
   * @author tvtphuc
   */
  GithubUser findUser(String id) throws NotFoundException;
}
