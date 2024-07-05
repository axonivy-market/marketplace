package com.axonivy.market.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.axonivy.market.entity.GitHubRepoMeta;

public interface GitHubRepoMetaRepository extends MongoRepository<GitHubRepoMeta, String> {

  GitHubRepoMeta findByRepoName(String repoName);
}
