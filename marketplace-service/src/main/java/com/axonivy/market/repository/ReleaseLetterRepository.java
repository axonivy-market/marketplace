package com.axonivy.market.repository;

import com.axonivy.market.entity.ReleaseLetter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReleaseLetterRepository extends JpaRepository<ReleaseLetter, String> {
}
