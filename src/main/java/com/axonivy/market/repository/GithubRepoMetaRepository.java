package com.axonivy.market.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.axonivy.market.entity.GithubRepoMeta;

public interface GithubRepoMetaRepository extends MongoRepository<GithubRepoMeta, String>{

}
