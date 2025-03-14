package com.axonivy.market.service.impl;

import com.axonivy.market.entity.MavenArtifactModel;
import com.axonivy.market.model.MavenModel;
import com.axonivy.market.repository.MavenArtifactModelRepository;
import com.axonivy.market.service.MavenArtifactModelService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
public class MavenArtifactModelServiceImpl implements MavenArtifactModelService {

  private final MavenArtifactModelRepository mavenArtifactModelRepository;

  public MavenArtifactModelServiceImpl(
      MavenArtifactModelRepository mavenArtifactModelRepository) {this.mavenArtifactModelRepository =
      mavenArtifactModelRepository;}

  @Override
  public MavenModel fetchMavenArtifactModels(String productId) {
    List<MavenArtifactModel> mavenArtifactModels = this.mavenArtifactModelRepository.findByProductId(productId);

    if (mavenArtifactModels.isEmpty()) {
      return null;
    }

    List<MavenArtifactModel> productArtifactModels = mavenArtifactModels.stream()
        .filter(model -> !model.isAdditionalVersion())
        .toList();

    List<MavenArtifactModel> additionalProductArtifactModel = mavenArtifactModels.stream()
        .filter(MavenArtifactModel::isAdditionalVersion)
        .toList();

    return new MavenModel(productArtifactModels, additionalProductArtifactModel);
  }
}
