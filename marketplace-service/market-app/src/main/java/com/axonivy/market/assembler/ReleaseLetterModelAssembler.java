package com.axonivy.market.assembler;

import com.axonivy.market.controller.ReleaseLetterController;
import com.axonivy.market.entity.ReleaseLetter;
import com.axonivy.market.model.ReleaseLetterModel;
import lombok.extern.log4j.Log4j2;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Log4j2
@Component
public class ReleaseLetterModelAssembler implements RepresentationModelAssembler<ReleaseLetter, ReleaseLetterModel> {
  @Override
  public ReleaseLetterModel toModel(ReleaseLetter releaseLetter) {
    var resource = new ReleaseLetterModel();
    resource.add(linkTo(methodOn(ReleaseLetterController.class)
        .findReleaseLetterById(releaseLetter.getId())).withSelfRel());
    resource.setContent(releaseLetter.getContent());
    resource.setSprint(releaseLetter.getSprint());
    resource.setActive(releaseLetter.isActive());

    return resource;
  }
}
