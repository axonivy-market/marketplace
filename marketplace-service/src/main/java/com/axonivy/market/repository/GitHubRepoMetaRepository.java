package com.axonivy.market.repository;

import com.axonivy.market.entity.GitHubRepoMeta;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GitHubRepoMetaRepository extends MongoRepository<GitHubRepoMeta, String> {

  GitHubRepoMeta findByRepoName(String repoName);
}
