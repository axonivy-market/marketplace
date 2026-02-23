package com.axonivy.market.assembler;

import com.axonivy.market.entity.ReleaseLetter;
import com.axonivy.market.model.ReleaseLetterModel;
import lombok.extern.log4j.Log4j2;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class ReleaseLetterModelAssembler implements RepresentationModelAssembler<ReleaseLetter, ReleaseLetterModel> {

  @Override
  public ReleaseLetterModel toModel(ReleaseLetter releaseLetter) {
    var resource = new ReleaseLetterModel();
    resource.setContent(releaseLetter.getContent());
    resource.setSprint(releaseLetter.getSprint());
    resource.setLatest(releaseLetter.isLatest());
    resource.setCreatedAt(releaseLetter.getCreatedAt());

    return resource;
  }
}
