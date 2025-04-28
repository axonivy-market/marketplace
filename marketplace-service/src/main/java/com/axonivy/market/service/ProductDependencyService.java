package com.axonivy.market.service;

public interface ProductDependencyService {
 int syncIARDependenciesForProducts(Boolean resetSync, String productId);
}
