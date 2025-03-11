package com.axonivy.market.repository;

import com.axonivy.market.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
  User searchByGitHubId(String gitHubId);
}
