package com.axonivy.market.core.service.impl;

import com.axonivy.market.core.criteria.ProductSearchCriteria;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.enums.Language;
import com.axonivy.market.core.enums.TypeOption;
import com.axonivy.market.core.repository.CoreProductRepository;
import com.axonivy.market.core.service.CoreProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.axonivy.market.core.enums.DocumentField.SHORT_DESCRIPTIONS;

@Log4j2
@Service
@RequiredArgsConstructor
public class CoreProductServiceImpl implements CoreProductService {
  private final CoreProductRepository coreProductRepo;


  @Override
  public Page<Product> findProducts(String type, String keyword, String language, Boolean isRESTClient,
      Pageable pageable) {
    final var typeOption = TypeOption.of(type);
    var searchCriteria = new ProductSearchCriteria();
    searchCriteria.setListed(true);
    searchCriteria.setKeyword(keyword);
    searchCriteria.setType(typeOption);
    searchCriteria.setLanguage(Language.of(language));
    if (BooleanUtils.isTrue(isRESTClient)) {
      searchCriteria.setExcludeFields(List.of(SHORT_DESCRIPTIONS));
    }
    return coreProductRepo.searchByCriteria(searchCriteria, pageable);
  }
}
