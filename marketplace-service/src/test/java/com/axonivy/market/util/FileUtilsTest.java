package com.axonivy.market.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileUtilsTest {

  private static final String FILE_PATH = "src/test/resources/test-file.xml";

  @Test
  void testCreateFile() throws IOException {
    File createdFile = FileUtils.createFile(FILE_PATH);
    assertTrue(createdFile.exists(), "File should exist");
    assertTrue(createdFile.isFile(), "Should be a file");

    createdFile.delete();
  }

  @Test
  void testFailedToCreateDirectory() throws IOException {
    File createdFile = new File("testDirAsFile");
    try {
      if (!createdFile.exists()) {
        assertTrue(createdFile.createNewFile(), "Setup failed: could not create file");
      }

      IOException exception = assertThrows(IOException.class, () -> {
        FileUtils.createFile("testDirAsFile/subDir/testFile.txt");
      });
      assertTrue(exception.getMessage().contains("Failed to create directory"),
          "Exception message does not contain expected text");
    } catch (IOException e) {
      fail("Setup failed: " + e.getMessage());
    } finally {
      createdFile.delete();
    }
  }

  @Test
  void testWriteFile() throws IOException {
    File createdFile = FileUtils.createFile(FILE_PATH);
    String content = "Hello, world!";
    FileUtils.writeToFile(createdFile, content);

    String fileContent = Files.readString(createdFile.toPath());
    assertEquals(content, fileContent, "File content should match the written content");

    createdFile.delete();

  }

}
