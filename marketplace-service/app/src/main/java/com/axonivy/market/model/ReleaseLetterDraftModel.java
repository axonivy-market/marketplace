package com.axonivy.market.model;

import com.axonivy.market.entity.ReleaseLetterDraft;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReleaseLetterDraftModel {

  private String id;
  private String releaseLetterId;
  private String draftContent;

  public static ReleaseLetterDraftModel from(ReleaseLetterDraft releaseLetterDraft) {
    return ReleaseLetterDraftModel.builder()
        .id(releaseLetterDraft.getId())
        .releaseLetterId(releaseLetterDraft.getReleaseLetterId())
        .draftContent(releaseLetterDraft.getDraftContent())
        .build();
  }
}
