package com.axonivy.market.service.impl;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ReleaseLetter;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.model.FeedbackProjection;
import com.axonivy.market.model.ReleaseLetterModel;
import com.axonivy.market.model.ReleaseLetterModelRequest;
import com.axonivy.market.repository.ReleaseLetterRepository;
import com.axonivy.market.service.ReleaseLetterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class ReleaseLetterServiceImpl implements ReleaseLetterService {
  private final ReleaseLetterRepository releaseLetterRepository;

  @Override
  public Page<ReleaseLetter> findAllReleaseLetters(Pageable pageable) {
    return releaseLetterRepository.findAll(pageable);
  }

  @Override
  public ReleaseLetter findReleaseLetterById(String id) {
    return releaseLetterRepository.findById(id).orElseThrow(
        () -> new NotFoundException(ErrorCode.RELEASE_LETTER_NOT_FOUND, "Not found release letter with id: " + id));
  }

  @Override
  public ReleaseLetter findReleaseLetterByReleaseVersion(String releaseVersion) {
    return releaseLetterRepository.findByReleaseVersion(releaseVersion).orElseThrow(
        () -> new NotFoundException(ErrorCode.RELEASE_LETTER_NOT_FOUND,
            "Not found release letter with release version: " + releaseVersion));
  }

  @Override
  public ReleaseLetter createReleaseLetter(ReleaseLetterModelRequest releaseLetterModelRequest) {
    ReleaseLetter releaseLetter =
        ReleaseLetter.builder().content(releaseLetterModelRequest.getContent()).releaseVersion(
            releaseLetterModelRequest.getReleaseVersion()).build();

    return releaseLetterRepository.save(releaseLetter);
  }

  @Override
  public ReleaseLetter updateReleaseLetter(String releaseVersion, ReleaseLetterModelRequest releaseLetterModelRequest) {
    var foundReleaseLetter = findReleaseLetterByReleaseVersion(releaseVersion);
    foundReleaseLetter.setContent(releaseLetterModelRequest.getContent());
    foundReleaseLetter.setReleaseVersion(releaseLetterModelRequest.getReleaseVersion());

    return releaseLetterRepository.save(foundReleaseLetter);
  }

  
}
