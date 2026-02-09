package com.axonivy.market.core.repository;

import com.axonivy.market.core.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoreImageRepository extends JpaRepository<Image, String> {
}
