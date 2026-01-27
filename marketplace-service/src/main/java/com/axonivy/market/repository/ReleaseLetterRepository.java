package com.axonivy.market.repository;

import com.axonivy.market.entity.ReleaseLetter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReleaseLetterRepository extends JpaRepository<ReleaseLetter, String> {
  Optional<ReleaseLetter> findByReleaseVersion(String releaseVersion);
}
