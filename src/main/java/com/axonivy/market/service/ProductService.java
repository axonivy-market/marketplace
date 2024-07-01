package com.axonivy.market.service;

import org.kohsuke.github.GHTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.axonivy.market.entity.Product;

public interface ProductService {
	Page<Product> findProducts(String type, String keyword, Pageable pageable);

	boolean syncLatestDataFromMarketRepo();

	Product fetchProductDetail(String id, String type);

	String getCompatibilityFromNumericTag(GHTag oldestTag);
}
