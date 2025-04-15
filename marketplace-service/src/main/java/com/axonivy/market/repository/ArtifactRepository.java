package com.axonivy.market.repository;

import com.axonivy.market.entity.Artifact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtifactRepository extends JpaRepository<Artifact, String> {

  @Query("SELECT DISTINCT a FROM Artifact a LEFT JOIN FETCH a.archivedArtifacts WHERE a.id IN :ids")
  List<Artifact> findAllByIdInAndFetchArchivedArtifacts(@Param("ids") List<String> ids);

}
