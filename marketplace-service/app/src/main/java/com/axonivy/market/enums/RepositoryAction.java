package com.axonivy.market.enums;

/**
 * Represents the available actions that can be performed on a repository.
 */
public enum RepositoryAction {
  /** Marks the repository as archived, making it read-only and hidden from active listings. */
  ARCHIVE,

  /** Restores a previously archived repository back to active state. */
  UNARCHIVE
}
