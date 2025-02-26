package com.axonivy.market.repository;

import com.axonivy.market.entity.MavenArtifactVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MavenArtifactVersionRepository extends JpaRepository<MavenArtifactVersion, String> {

}
