package com.axonivy.market.repository;

import java.util.List;

public interface CustomProductModuleContentRepository {

  List<String> findTagsByProductId(String id);
}