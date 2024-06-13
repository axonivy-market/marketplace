package com.axonivy.market.github.util;

import java.io.IOException;
import java.util.List;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.PagedIterable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GithubUtils {

  public static long getGHCommitDate(GHCommit commit) {
    long commitTime = 0l;
    if (commit != null) {
      try {
        commitTime = commit.getCommitDate().getTime();
      } catch (Exception e) {
        log.error("Check last commit failed", e);
      }
    }
    return commitTime;
  }

  public static String getDownloadUrl(GHContent content) {
    try {
      return content.getDownloadUrl();
    } catch (IOException e) {
      log.error("Cannot get DownloadURl from GHContent: ", e);
    }
    return "";
  }

  public static <T> List<T> mapPagedIteratorToList(PagedIterable<T> paged) {
    if (paged != null) {
      try {
        return paged.toList();
      } catch (IOException e) {
        log.error("Cannot parse to list for pagediterable: ", e);
      }
    }
    return List.of();
  }
}
