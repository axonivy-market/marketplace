package com.axonivy.market.stable.repository;

import com.axonivy.market.core.repository.CoreMetadataRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public interface MetadataRepository extends CoreMetadataRepository {
}
