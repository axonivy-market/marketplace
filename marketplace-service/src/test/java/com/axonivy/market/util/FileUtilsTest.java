package com.axonivy.market.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@ExtendWith(MockitoExtension.class)
class FileUtilsTest {

  private static final String FILE_PATH = "src/test/resources/test-file.xml";

  @Test
  void testCreateFile() throws IOException {
    File createdFile = FileUtils.createFile(FILE_PATH);
    Assertions.assertTrue(createdFile.exists(), "File should exist");
    Assertions.assertTrue(createdFile.isFile(), "Should be a file");

    createdFile.delete();
  }

  @Test
  void testWriteFile() throws IOException {
    File createdFile = FileUtils.createFile(FILE_PATH);
    String content = "Hello, world!";
    FileUtils.writeToFile(createdFile, content);

    String fileContent = Files.readString(createdFile.toPath());
    Assertions.assertEquals(content, fileContent, "File content should match the written content");

    createdFile.delete();

  }

}
