package com.axonivy.market.repository;

import com.axonivy.market.entity.Feedback;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductCustomSortRepository extends MongoRepository<Feedback, String> {
}
