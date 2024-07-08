package com.axonivy.market.constants;

import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GitHubConstants {
	public static final String AXONIVY_MARKET_ORGANIZATION_NAME = "axonivy-market";
	public static final String AXONIVY_MARKETPLACE_REPO_NAME = "market";
	public static final String AXONIVY_MARKETPLACE_PATH = "market";
	public static final String DEFAULT_BRANCH = "feature/MARP-463-Multilingualism-for-Website";
	public static final String PRODUCT_JSON_FILE_PATH_FORMAT = "%s/product.json";
	public static final List<String> PRODUCT_README_FILES = List.of("README.md", "README_DE.md");
	public static final String README_FILE_LOCALE_REGEX = "_(..)";
}