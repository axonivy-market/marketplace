package com.axonivy.market.repository;

import com.axonivy.market.entity.productjsonfilecontent.ProductJsonContent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductJsonContentRepository extends MongoRepository<ProductJsonContent, String> {

  ProductJsonContent findByNameAndTag(String name , String tag);

  boolean existsByNameAndTag(String name , String tag);

}
