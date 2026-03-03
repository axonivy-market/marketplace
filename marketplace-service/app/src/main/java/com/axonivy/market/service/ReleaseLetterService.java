package com.axonivy.market.service;

import com.axonivy.market.entity.ReleaseLetter;
import com.axonivy.market.model.ReleaseLetterModelRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReleaseLetterService {
  Page<ReleaseLetter> findAllReleaseLetters(Pageable pageable, boolean isReadOnly);

  ReleaseLetter findReleaseLetterById(String id);

  Page<ReleaseLetter> findLatestReleaseLetter(Pageable pageable);

  ReleaseLetter findReleaseLetterBySprint(String releaseVersion);

  ReleaseLetter createReleaseLetter(ReleaseLetterModelRequest releaseLetterModelRequest);

  ReleaseLetter updateReleaseLetter(String id, ReleaseLetterModelRequest releaseLetterModelRequest);

  void deleteReleaseLetterBySprint(String sprint);

  void deleteReleaseLetterById(String id);
}
