package com.axonivy.market.factory;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.github.model.Meta;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHContent;

import java.io.IOException;


@Log4j2
public class ProductFactory {

    public static final String META_FILE = "meta.json";
    public static final String LOGO_FILE = "logo.png";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Product mappingByGHContent(Product product, GHContent content) {
        var contentName = content.getName();
        try {
            if (contentName.endsWith(META_FILE)) {
                ProductFactory.mappingByMetaJson(product, content);
            }

            if (contentName.endsWith(LOGO_FILE)) {
                product.setLogoUrl(content.getDownloadUrl());
            }
        } catch (IOException e) {
            log.warn("Mapping from GHContent failed", e);
        }
        return product;
    }

    public static Product mappingByMetaJson(Product product, GHContent ghContent)
            throws IOException {
        var meta = jsonDecode(ghContent);
        if (meta == null) {
            return product;
        }
        product.setKey(meta.getId());
        product.setName(meta.getName());
        product.setMarketDirectory(ghContent.getPath().replace(ghContent.getName(), ""));
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
        extractSourceUrl(product, meta);
        return product;
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

    public static Meta jsonDecode(GHContent ghContent) throws IOException {
        if (ghContent != null) {
            return MAPPER.readValue(ghContent.read().readAllBytes(), Meta.class);
        }
        return null;
    }

}
