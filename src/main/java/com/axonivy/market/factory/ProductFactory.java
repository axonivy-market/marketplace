package com.axonivy.market.factory;

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
        // log.warn(meta);
        product.setKey(meta.getId());
        product.setName(meta.getName());
        product.setListed(meta.getListed());
        product.setType(meta.getType());
        product.setTags(meta.getTags());
        product.setVersion(meta.getVersion());
        product.setShortDescript(meta.getDescription());
        product.setVendor(StringUtils.isBlank(meta.getVendor()) ? "Axon Ivy AG" : meta.getVendor());
        product.setVendorImage(
                StringUtils.isBlank(meta.getVendorImage()) ? "/images/misc/axonivy-logo-black.svg" : meta.getVendor());
        product.setVendorUrl(StringUtils.isBlank(meta.getVendorUrl()) ? "https://www.axonivy.com" : meta.getVendorUrl());
        product.setPlatformReview(StringUtils.isBlank(meta.getPlatformReview()) ? "4.0" : meta.getPlatformReview());
        product.setSourceUrl(meta.getSourceUrl());
        product.setStatusBadgeUrl(meta.getStatusBadgeUrl());
        product.setLanguage(meta.getLanguage());
        product.setIndustry(meta.getIndustry());
        // TODO mapping default data
        // product.setCost() = ghContent->cost ?? 'Free';
        // product.setCompatibility(meta.get) = ghContent->compatibility ?? '';
        // product.setValidate(meta.get) = ghContent->validate ?? false;
        // product.setContactUs(meta) = ghContent->contactUs ?? false;
        return product;
    }

    public static Meta jsonDecode(GHContent ghContent) throws IOException {
        if (ghContent != null) {
            return MAPPER.readValue(ghContent.read().readAllBytes(), Meta.class);
        }
        return null;
    }


}
