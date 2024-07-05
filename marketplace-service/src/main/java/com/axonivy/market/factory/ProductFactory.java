package com.axonivy.market.factory;

import static com.axonivy.market.constants.CommonConstants.LOGO_FILE;
import static com.axonivy.market.constants.CommonConstants.META_FILE;
import static com.axonivy.market.constants.CommonConstants.SLASH;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHContent;
import org.springframework.util.CollectionUtils;

import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.Language;
import com.axonivy.market.github.model.Meta;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.model.DisplayValue;
import com.axonivy.market.model.MultilingualismValue;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductFactory {
	private static final ObjectMapper MAPPER = new ObjectMapper();

	public static Product mappingByGHContent(Product product, GHContent content) {
		if (content == null) {
			return product;
		}

		var contentName = content.getName();
		if (StringUtils.endsWith(contentName, META_FILE)) {
			mappingByMetaJSONFile(product, content);
		}
		if (StringUtils.endsWith(contentName, LOGO_FILE)) {
			product.setLogoUrl(GitHubUtils.getDownloadUrl(content));
		}
		return product;
	}

	public static Product mappingByMetaJSONFile(Product product, GHContent ghContent) {
		Meta meta = null;
		try {
			meta = jsonDecode(ghContent);
		} catch (Exception e) {
			log.error("Mapping from Meta file by GHContent failed", e);
			return product;
		}

		product.setId(meta.getId());
		product.setNames(mappingMultilingualismValueByMetaJSONFile(meta.getNames()));
		product.setMarketDirectory(extractParentDirectory(ghContent));
		product.setListed(meta.getListed());
		product.setType(meta.getType());
		product.setTags(meta.getTags());
		product.setVersion(meta.getVersion());
		product.setShortDescriptions(mappingMultilingualismValueByMetaJSONFile(meta.getDescriptions()));
		product.setVendor(meta.getVendor());
		product.setVendorImage(meta.getVendorImage());
		product.setVendorUrl(meta.getVendorUrl());
		product.setPlatformReview(meta.getPlatformReview());
		product.setStatusBadgeUrl(meta.getStatusBadgeUrl());
		product.setLanguage(meta.getLanguage());
		product.setIndustry(meta.getIndustry());
		extractSourceUrl(product, meta);
		product.setArtifacts(meta.getMavenArtifacts());
		return product;
	}

  private static MultilingualismValue mappingMultilingualismValueByMetaJSONFile(List<DisplayValue> list) {
    MultilingualismValue value = new MultilingualismValue();
    if (!CollectionUtils.isEmpty(list)) {
      for (DisplayValue name : list) {
        if (Language.EN.getValue().equalsIgnoreCase(name.getLocale())) {
          value.setEn(name.getValue());
        } else if (Language.DE.getValue().equalsIgnoreCase(name.getLocale())) {
          value.setDe(name.getValue());
        }
      }
    }

    return value;
  }

	private static String extractParentDirectory(GHContent ghContent) {
		var path = StringUtils.defaultIfEmpty(ghContent.getPath(), EMPTY);
		return path.replace(ghContent.getName(), EMPTY);
	}

	public static void extractSourceUrl(Product product, Meta meta) {
		var sourceUrl = meta.getSourceUrl();
		if (StringUtils.isBlank(sourceUrl)) {
			return;
		}
		String[] tokens = sourceUrl.split(SLASH);
		var tokensLength = tokens.length;
		var repositoryPath = sourceUrl;
		if (tokensLength > 1) {
			repositoryPath = String.join(SLASH, tokens[tokensLength - 2], tokens[tokensLength - 1]);
		}
		product.setRepositoryName(repositoryPath);
		product.setSourceUrl(sourceUrl);
	}

	private static Meta jsonDecode(GHContent ghContent) throws IOException {
		return MAPPER.readValue(ghContent.read().readAllBytes(), Meta.class);
	}
}