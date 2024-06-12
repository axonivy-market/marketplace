package com.axonivy.market.repository;

import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MavenArtifactVersionRepository extends MongoRepository<MavenArtifactVersion, String> {
}
