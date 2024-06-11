package com.axonivy.market.factory;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.*;
import org.springframework.util.CollectionUtils;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.github.model.Meta;
import com.axonivy.market.github.util.GithubUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

@Log4j2
public class ProductFactory {

    public static final String META_FILE = "meta.json";
    public static final String LOGO_FILE = "logo.png";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static final String IMAGES_FOLDER_PATH = "/images";

    public static Product mappingByGHContent(Product product, GHContent content) {
        var contentName = content.getName();
        if (contentName.endsWith(META_FILE)) {
            ProductFactory.mappingByMetaJson(product, content);
        }
        if (contentName.endsWith(LOGO_FILE)) {
            product.setLogoUrl(GithubUtils.getDownloadUrl(content));
        }

        return product;
    }

    public static Product mappingByMetaJson(Product product, GHContent ghContent) {
        Meta meta = null;
        try {
            meta = jsonDecode(ghContent);
        } catch (Exception e) {
            log.error("Mapping from Meta file by GHContent failed", e);
            return product;
        }

        product.setKey(meta.getId());
        product.setName(meta.getName());
        product.setMarketDirectory(extractParentDirectory(ghContent));
        product.setListed(meta.getListed());
        product.setType(meta.getType());
        product.setTags(meta.getTags());
        product.setVersion(meta.getVersion());
        product.setShortDescription(meta.getDescription());
        product.setVendor(meta.getVendor());
        product.setVendorImage(meta.getVendorImage());
        product.setVendorUrl(meta.getVendorUrl());
        product.setPlatformReview(meta.getPlatformReview());
        product.setStatusBadgeUrl(meta.getStatusBadgeUrl());
        product.setLanguage(meta.getLanguage());
        product.setIndustry(meta.getIndustry());
        product.setContactUs(meta.getContactUs());
        product.setCompatibility(meta.getCompatibility());
        product.setCost(meta.getCost());
        extractSourceUrl(product, meta);
        updateLatestReleaseDateForProduct(product);
        return product;
    }

    private static void updateLatestReleaseDateForProduct(Product product) {
        if (StringUtils.isBlank(product.getRepositoryName())) {
            return;
        }
        try {
            var productRepo = GithubUtils.getGHRepoByPath(product.getRepositoryName());
            var lastTag = CollectionUtils.firstElement(productRepo.listTags().toList());
            product.setNewestPublishDate(lastTag.getCommit().getCommitDate());
            product.setNewestReleaseVersion(lastTag.getName());

            updateReadmeContentsFromTag(product, productRepo, lastTag);
        } catch (Exception e) {
            log.error("Cannot find repository by path {} {} {}", product.getRepositoryName(), product.getKey(), e);
        }
    }

    private static void updateReadmeContentsFromTag(Product product, GHRepository productRepo, GHTag tag) throws IOException {

        //TODO: REFACTOR CHECKING FOLDER PRODUCT
        List<GHContent> contents = productRepo.getDirectoryContent("/", tag.getName());

        GHContent productFolder = null;
        for (GHContent content : contents) {
            if (content.isDirectory() && content.getName().endsWith("-product")) {
                productFolder = content;
                log.error("product folder 1 {}", productFolder);
                break;
            }
        }
        log.error("product folder {}", productFolder);
        if (productFolder == null) {
            log.error("No '-product' folder found in the repository.");
        }

        //TODO: REFACTOR GET README CONTENT AS STRING
        String productFolderPath = productFolder.getPath();
            GHContent readmeContent = productRepo.getFileContent(productFolderPath + "/README.md", tag.getName());
        String downloadUrl = GithubUtils.getDownloadUrl(readmeContent);

        log.error("README.md Download URL: " + GithubUtils.getDownloadUrl(readmeContent));


        URL url = new URL(downloadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine).append("\n");
        }

        in.close();
        connection.disconnect();
        String updatedContent = content.toString();

        //TODO: REFACTOR ADD IMAGE FOLDER AS MAP AND COMPARE README CONTENT
        var imageFolderContents = productRepo.getDirectoryContent(productFolderPath + IMAGES_FOLDER_PATH, tag.getName());
        Map<String, String> imageUrls = new HashMap<>();
        for (GHContent imageContent : imageFolderContents) {
            if (imageContent.isFile()) {
                imageUrls.putIfAbsent(imageContent.getName(), imageContent.getDownloadUrl());
            }
        }

        String updatedReadmeContent = replaceImageNamesWithUrls(updatedContent, imageUrls);
        String[] parts = updatedReadmeContent.split("## Setup|## Demo");

        if (parts.length >= 2) {
            product.setDescription(parts[0].trim());
            product.setSetup(parts[1].trim());
        }
        if (parts.length >= 3) {
            product.setDemo(parts[2].trim());
        }

//        log.error("updated {}", updatedReadmeContent);
    }

    private static String replaceImageNamesWithUrls(String content, Map<String, String> imageUrls) {
        for (Map.Entry<String, String> entry : imageUrls.entrySet()) {
            content = content.replaceAll("images/" + Pattern.quote(entry.getKey()), entry.getValue());
        }
        return content;
    }

    private static String extractParentDirectory(GHContent ghContent) {
        return ghContent.getPath().replace(ghContent.getName(), EMPTY);
    }

    private static void extractSourceUrl(Product product, Meta meta) {
        var sourceUrl = meta.getSourceUrl();
        if (StringUtils.isBlank(sourceUrl)) {
            return;
        }
        var urlLength = sourceUrl.length();
        var orgIndex = sourceUrl.indexOf(GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME);
        var repositoryPath = StringUtils.substring(sourceUrl, orgIndex, urlLength);
        product.setRepositoryName(repositoryPath);
        product.setSourceUrl(sourceUrl);
    }

    private static Meta jsonDecode(GHContent ghContent) throws Exception {
        return MAPPER.readValue(ghContent.read().readAllBytes(), Meta.class);
    }
}
