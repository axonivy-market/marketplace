package com.axonivy.market.constants;

/**
 * <p>
 * Directory constants defining directory paths for file storage, caching, and repository management.
 * </p>
 *
 * @since 15/04/2026
 * @author ntqdinh
 */
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DirectoryConstants {
  public static final String DATA_DIR = "data";
  public static final String WORK_DIR = "work";
  public static final String CACHE_DIR = "market-cache";
  public static final String DATA_CACHE_DIR = DATA_DIR + "/" + CACHE_DIR;
  public static final String GITHUB_REPO_DIR = "data/work/github";
  public static final String DOC_DIR = "doc";
}
