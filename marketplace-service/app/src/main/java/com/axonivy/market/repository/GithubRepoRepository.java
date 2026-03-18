package com.axonivy.market.repository;

import com.axonivy.market.core.repository.CoreGithubRepoRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public interface GithubRepoRepository extends CoreGithubRepoRepository, CustomGithubRepoRepository {
}
