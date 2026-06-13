package com.axonivy.market.repository;

import com.axonivy.market.entity.GithubUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GithubUserRepository extends JpaRepository<GithubUser, String> {
  GithubUser searchByGitHubId(String gitHubId);

  Optional<GithubUser> findByUsernameIgnoreCase(String username);
}
