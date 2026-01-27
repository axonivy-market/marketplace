package com.axonivy.market.service;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.entity.ReleaseLetter;
import com.axonivy.market.model.ReleaseLetterModelRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReleaseLetterService {
  Page<ReleaseLetter> findAllReleaseLetters(Pageable pageable);

  ReleaseLetter findReleaseLetterById(String id);

  ReleaseLetter createReleaseLetter(ReleaseLetterModelRequest releaseLetterModelRequest);
}
