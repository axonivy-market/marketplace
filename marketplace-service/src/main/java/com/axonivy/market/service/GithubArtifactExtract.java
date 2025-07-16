package com.axonivy.market.service;

import java.io.File;
import java.io.InputStream;

public interface GithubArtifactExtract {
  File extractZipToTempDir(InputStream zipStream, String name);
}
