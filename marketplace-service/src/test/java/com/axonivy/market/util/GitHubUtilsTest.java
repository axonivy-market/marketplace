package com.axonivy.market.util;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.github.util.GitHubUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.axonivy.market.constants.MetaConstants.META_FILE;
import static com.axonivy.market.constants.ProductJsonConstants.LOGO_FILE;

@ExtendWith(MockitoExtension.class)
class GitHubUtilsTest extends BaseSetup {

  @Test
  void testSortMetaJsonFirst() {
    int result = GitHubUtils.sortMetaJsonFirst(META_FILE, LOGO_FILE);
    Assertions.assertEquals(-1, result,
        "meta.json file comes first if first argument is meta.json file");

    result = GitHubUtils.sortMetaJsonFirst(LOGO_FILE, META_FILE);
    Assertions.assertEquals(1, result,
        "meta.json file comes first if second argument is meta.json file");

    result = GitHubUtils.sortMetaJsonFirst(LOGO_FILE, LOGO_FILE);
    Assertions.assertEquals(0, result,
        "Resolve to sorting alphabetically if none are meta.json files");
  }
}
