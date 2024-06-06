package com.axonivy.market.service.impl;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.factory.ProductFactory;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.github.model.Meta;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.model.ProductDetailArtifactModel;
import com.axonivy.market.service.ProductDetailActionService;
import com.axonivy.market.service.VersionService;
import lombok.extern.log4j.Log4j2;
import org.kohsuke.github.GHContent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class ProductDetailActionServiceImpl implements ProductDetailActionService {
    private final VersionService versionService;
    private final GHAxonIvyMarketRepoService gitHubService;

    public ProductDetailActionServiceImpl(VersionService versionService, GHAxonIvyMarketRepoService gitHubService) {
        this.versionService = versionService;
        this.gitHubService = gitHubService;
    }

    @Override
    public ProductDetailArtifactModel getArtifacts(String productId, Boolean isShowDevVersion, String designerVersion) {
        List<String> versions = versionService.getVersionsToDisplay(productId, isShowDevVersion, designerVersion);
        for (String version : versions) {

        }
        return null;
    }

    public String getProductModuleFromRepoNameAndVersion(String repoName, String Version) {
        GHContent content = gitHubService.getContentFromGHRepo(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME, "portal/meta.json");
        try {
            Meta metaFile = ProductFactory.jsonDecode(content);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<MavenArtifact> getArtifactFromRepoName(String repoName) {
        GHContent content = gitHubService.getContentFromGHRepo(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME, "portal/meta.json");
        List<MavenArtifact> artifacts = new ArrayList<>();
        try {
            Meta metaFile = ProductFactory.jsonDecode(content);
            artifacts.addAll(metaFile.getMavenArtifacts());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return artifacts;
    }


}
