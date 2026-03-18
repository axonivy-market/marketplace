package com.axonivy.market.stable.repository;

import com.axonivy.market.core.repository.CoreMavenArtifactVersionRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public interface MavenArtifactVersionRepository extends CoreMavenArtifactVersionRepository {
}
