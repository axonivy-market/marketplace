package com.axonivy.market.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DownloadOption {
  private boolean isForced;
  private String workingDirectory;
  private boolean shouldGrantPermission;
  public static DownloadOption defaultOption() {
    return new DownloadOption(false, null, false);
  }
}
