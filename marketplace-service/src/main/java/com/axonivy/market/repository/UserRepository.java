package com.axonivy.market.repository;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    User searchByGitHubId(String gitHubId);
}
