package com.axonivy.market.service;

import com.axonivy.market.core.exceptions.model.NotFoundException;
import com.axonivy.market.entity.GithubUser;

import java.util.List;

public interface GithubUserService {

  /**
   * <p>
   * Retrieves all GitHub users registered in the marketplace system. Returns complete user profiles
   * including user ID, username, profile information, and login history.
   * </p>
   *
   * @return {@link List<GithubUser>} - list of all registered GitHub users in the system; returns empty
   *         list if no users have registered
   * @author tvtphuc
   */
  List<GithubUser> getAllUsers();

  /**
   * <p>
   * Creates a new user record in the marketplace system from GitHub user information. Stores user profile
   * data including GitHub ID, username, profile URL, and other public information for tracking user activity
   * and enabling marketplace features.
   * </p>
   *
   * @param  githubUser
   *              type {@link GithubUser} - the GitHub user object with user ID, username, and profile information
   * @return {@link GithubUser} - the created user record with generated internal ID and timestamps
   * @author tvtphuc
   */
  GithubUser createUser(GithubUser githubUser);

  /**
   * <p>
   * Retrieves a specific GitHub user by their internal system ID. Returns complete user profile information
   * for authenticated API calls and user-specific operations.
   * </p>
   *
   * @param  id
   *              type {@link String} - the internal user ID (system UUID, not GitHub ID)
   * @return {@link GithubUser} - the GitHub user object with complete profile information
   * @throws NotFoundException - if user with the specified ID does not exist in the system
   * @author tvtphuc
   */
  GithubUser findUser(String id) throws NotFoundException;
}
