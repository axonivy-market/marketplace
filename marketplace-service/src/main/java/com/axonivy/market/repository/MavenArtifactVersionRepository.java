package com.axonivy.market.repository;

import com.axonivy.market.entity.MavenArtifactVersion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MavenArtifactVersionRepository extends MongoRepository<MavenArtifactVersion, String> {
}
