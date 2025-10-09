package com.axonivy.market.model;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentInfoResponse {

  private List<DocumentVersion> versions;
  private List<DocumentLanguage> languages;

  @Getter
  @Builder
  @AllArgsConstructor
  public static class DocumentVersion {
    private String version;
    private String url;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  public static class DocumentLanguage {
    private String language;
    private String url;
  }


}
