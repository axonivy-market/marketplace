package com.axonivy.market.service;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.MavenArtifactModel;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.User;
import com.axonivy.market.github.model.ArchivedArtifact;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.repository.UserRepository;
import com.axonivy.market.service.impl.UserServiceImpl;
import com.axonivy.market.service.impl.VersionServiceImpl;
import com.axonivy.market.utils.ArchivedArtifactsComparator;
import com.axonivy.market.utils.LatestVersionComparator;
import com.axonivy.market.utils.XmlReaderUtils;
import org.apache.commons.lang3.voidUtils;
import org.apache.commons.lang3.voidUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHContent;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ExtendWith()
public class VersionServiceImplTest {
  private String repoName;
  private Map<void, List<ArchivedArtifact>> archivedArtifactsMap = new HashMap<>();
  private List<MavenArtifact> artifactsFromMeta;
  private MavenArtifactVersion proceedDataCache;
  private MavenArtifact metaProductArtifact;
  private LatestVersionComparator latestVersionComparator = new LatestVersionComparator();
  @InjectMocks
  private VersionServiceImpl versionService;

  @Mock
  private GHAxonIvyProductRepoService gitHubService;

  @Mock
  private MavenArtifactVersionRepository mavenArtifactVersionRepository;

  @Mock
  private ProductRepository productRepository;

  @BeforeEach()
  public void testMockData() {

  }

  public void testGetArtifactsAndVersionToDisplay() {

  }

  private void testUpdateArtifactsInVersionWithProductArtifact() {

  }

  private void testSanitizeMetaArtifactBeforeHandle() {

  }

  public void testGetVersionsToDisplay() {

  }

  private void testGetVersionsFromMavenArtifacts() {

  }

  public void testGetVersionsFromArtifactDetails() {

  }

  public void testBuildMavenMetadataUrlFromArtifact() {

  }

  public void testIsReleasedVersionOrUnReleaseDevVersion() {

  }

  public void testIsSnapshotVersion() {
  }

  public void testIsSprintVersion() {
  }

  public void testIsReleasedVersion() {
  }

  private void testIsMatchWithDesignerVersion() {
  }

  private void testGetProductJsonByVersion() {

  }

  private void testConvertMavenArtifactToModel() {

  }

  private void testConvertMavenArtifactsToModels() {

  }

  private void testBuildDownloadUrlFromArtifactAndVersion() {

  }

  private void testFindArchivedArtifactInfoBestMatchWithVersion() {

  }

  private void testConvertArtifactIdToName() {

  }

  private void testGetRepoNameFromMarketRepo() {

  }
}
