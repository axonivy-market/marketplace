package com.axonivy.market.github.service;

import java.util.List;
import java.util.Map;

import org.kohsuke.github.GHContent;

public interface GHAxonIvyMarketRepoService {

  Map<String, List<GHContent>> fetchAllMarketItems();
}
