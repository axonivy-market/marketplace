package com.axonivy.market.service;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axonivy.market.github.service.AbstractGithubService;
import com.axonivy.market.github.service.impl.GHAxonIvyMarketRepoServiceImpl;

@ExtendWith(MockitoExtension.class)
class GHAxonIvyMarketRepoServiceImplTest {
  @Mock
  private GHOrganization organization;
  @Mock
  private GHRepository repository;
  @Mock
  AbstractGithubService abstractGithubService;

  @Mock
  GHAxonIvyMarketRepoServiceImpl axonIvyMarketRepoServiceImpl;

  @Test
  void testFetchAllMarketItems() throws IOException {

    axonIvyMarketRepoServiceImpl.fetchAllMarketItems();

  }
}
