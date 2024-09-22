package com.axonivy.market.repository;

import com.axonivy.market.entity.MavenVersionSync;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MavenVersionSyncRepository extends MongoRepository<MavenVersionSync, String> {
}
