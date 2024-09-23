package com.axonivy.market.repository;

import com.axonivy.market.entity.MetadataSync;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MavenVersionSyncRepository extends MongoRepository<MetadataSync, String> {
}
