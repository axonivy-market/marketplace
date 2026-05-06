package com.axonivy.market.github.service;

import com.axonivy.market.github.model.GitHubFile;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;

import java.util.List;
import java.util.Map;

public interface GHAxonIvyMarketRepoService {

  /**
   * <p>
   * Fetches all market items from the AxonIvy market repository. Returns a map grouping
   * market items by directory, allowing organized browsing of available products
   * and plugins in the marketplace.
   * </p>
   *
   * @return {@link Map<String, List<GHContent>>} - map of market items grouped by directory;
   *         keys are directory names, values are lists of content files
   * @author ntqdinh
   */
  Map<String, List<GHContent>> fetchAllMarketItems();

  /**
   * <p>
   * Retrieves the last commit to the market repository made after a specific timestamp.
   * Used for incremental synchronization to identify recent changes to market items.
   * </p>
   *
   * @param  lastCommitTime
   *              type {@link long} - Unix timestamp to find commits after
   * @return {@link GHCommit} - the last commit matching the time criteria; returns null if no
   *         commits found after the specified timestamp
   * @author ntqdinh
   */
  GHCommit getLastCommit(long lastCommitTime);

  /**
   * <p>
   * Fetches market items between two specific Git commit SHAs. Returns list of files
   * with changes between the commits, useful for tracking modifications over time.
   * </p>
   *
   * @param  fromSHA1
   *              type {@link String} - the starting commit SHA (inclusive)
   * @param  toSHA1
   *              type {@link String} - the ending commit SHA (inclusive)
   * @return {@link List<GitHubFile>} - list of files with changes between the two commits
   * @author ntqdinh
   */
  List<GitHubFile> fetchMarketItemsBySHA1Range(String fromSHA1, String toSHA1);

  /**
   * <p>
   * Retrieves the GitHub repository object representing the AxonIvy market repository.
   * Provides access to repository metadata and configuration.
   * </p>
   *
   * @return {@link GHRepository} - the market repository object
   * @author ntqdinh
   */
  GHRepository getRepository();

  /**
   * <p>
   * Retrieves market item files from a specific path in the repository. Returns all files
   * and subdirectories within the specified market item directory.
   * </p>
   *
   * @param  itemPath
   *              type {@link String} - the path to the market item directory (e.g., "portal", "connectors/salesforce")
   * @return {@link List<GHContent>} - list of files and subdirectories in the market item; returns empty
   *         list if path not found or is empty
   * @author ntqdinh
   */
  List<GHContent> getMarketItemByPath(String itemPath);
}
