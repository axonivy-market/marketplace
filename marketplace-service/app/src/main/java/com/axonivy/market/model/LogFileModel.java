package com.axonivy.market.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogFileModel {
  private String fileName;
  private long size;
  private String date;
}
