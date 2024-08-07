package com.axonivy.market.github.model;

import com.axonivy.market.enums.FileStatus;
import com.axonivy.market.enums.FileType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class GitHubFile {
  private String fileName;
  private String previousFilename;
  private String path;
  private FileType type;
  private FileStatus status;
  private Date commitDate;
}
