package com.axonivy.market.repository;

import com.axonivy.market.entity.GitHubRepoMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GitHubRepoMetaRepository extends JpaRepository<GitHubRepoMeta, String> {

  GitHubRepoMeta findByRepoName(String repoName);
}
