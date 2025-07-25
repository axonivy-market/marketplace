package com.axonivy.market.repository;

import com.axonivy.market.entity.GithubRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GithubRepoRepository extends JpaRepository<GithubRepo, String> {
  Optional<GithubRepo> findByName(String name);
}
