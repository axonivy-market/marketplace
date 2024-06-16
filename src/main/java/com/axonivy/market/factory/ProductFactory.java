package com.axonivy.market.factory;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.*;
import org.springframework.util.CollectionUtils;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.github.model.Meta;
import com.axonivy.market.github.util.GithubUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ProductFactory {

    public static final String META_FILE = "meta.json";
    public static final String LOGO_FILE = "logo.png";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static final String NON_NUMERIC_CHAR = "[^0-9.]";

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
        product.setType(StringUtils.capitalize(meta.getType()));
        product.setTags(meta.getTags());
        product.setShortDescription(meta.getDescription());
        product.setVendor(StringUtils.isBlank(meta.getVendor()) ? "Axon Ivy AG" : meta.getVendor());
        product.setVendorUrl(StringUtils.isBlank(meta.getVendorUrl()) ? "https://www.axonivy.com" : meta.getVendorUrl());
        product.setPlatformReview(meta.getPlatformReview());
        product.setStatusBadgeUrl(meta.getStatusBadgeUrl());
        product.setLanguage(meta.getLanguage());
        product.setIndustry(meta.getIndustry());
        product.setContactUs(BooleanUtils.isTrue(meta.getContactUs()) ? meta.getContactUs() : false);
        product.setCost(StringUtils.isBlank(meta.getCost()) ? "Free" : StringUtils.capitalize(meta.getCost()));
        extractSourceUrl(product, meta);
        updateLatestReleaseDateForProduct(product);
        extractCompatibilityFromOldestTag(product, meta);
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
        } catch (Exception e) {
            log.error("Cannot find repository by path {} {}", product.getRepositoryName(), e);
        }
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

    private static void extractCompatibilityFromOldestTag(Product product, Meta meta) {
        try {
            GHTag oldestTag = CollectionUtils.lastElement(GithubUtils.getTagsFromRepo(product.getRepositoryName()));
            if (oldestTag != null) {
                String compatibility = getCompatibilityFromNumericTag(oldestTag);
                product.setCompatibility(StringUtils.isBlank(meta.getCompatibility()) ? compatibility : meta.getCompatibility());
            }
        } catch (Exception e) {
            log.error("Cannot find repository by path {}", e);
        }
    }

    // Cover 3 cases after removing non-numeric characters (8, 11.1 and 10.0.2)
    private static String getCompatibilityFromNumericTag(GHTag oldestTag) {
        String numericTag = oldestTag.getName().replaceAll(NON_NUMERIC_CHAR, "");
        if (!numericTag.contains(".")) {
            return numericTag + ".0+";
        }
        int firstDot = numericTag.indexOf(".");
        int secondDot = numericTag.indexOf(".", firstDot + 1);
        if (secondDot == -1) {
            return numericTag + "+";
        }
        return numericTag.substring(0, secondDot) + "+";
    }

    private static Meta jsonDecode(GHContent ghContent) throws Exception {
        return MAPPER.readValue(ghContent.read().readAllBytes(), Meta.class);
    }
}
