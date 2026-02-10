package com.axonivy.market.repository;

import com.axonivy.market.entity.GithubUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GithubUserRepository extends JpaRepository<GithubUser, String> {
  GithubUser searchByGitHubId(String gitHubId);
}
