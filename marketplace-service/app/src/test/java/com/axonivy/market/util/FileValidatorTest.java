package com.axonivy.market.util;

import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileValidatorTest {

  @Test
  void testValidateImageFileSuccess() throws IOException {
    MultipartFile mockFile = mock(MultipartFile.class);
    when(mockFile.isEmpty()).thenReturn(false);
    when(mockFile.getSize()).thenReturn(5120L);
    when(mockFile.getContentType()).thenReturn("image/jpeg");
    when(mockFile.getOriginalFilename()).thenReturn("photo.jpg");

    assertDoesNotThrow(() -> FileValidator.validateImageFile(mockFile),
        "Valid image file should pass validation");
  }

  @Test
  void testValidateFileSizeExceeded() throws IOException {
    MultipartFile mockFile = mock(MultipartFile.class);
    when(mockFile.getSize()).thenReturn(20971520L);

    IOException exception = assertThrows(IOException.class,
        () -> FileValidator.validateFileSize(mockFile, 10 * 1024 * 1024),
        "File size exceeding max should throw IOException");
    assertTrue(exception.getMessage().contains("File size exceeds"));
  }

  @Test
  void testValidateMimeTypeValid() throws IOException {
    MultipartFile mockFile = mock(MultipartFile.class);
    when(mockFile.getContentType()).thenReturn("image/png");

    assertDoesNotThrow(() -> FileValidator.validateMimeType(mockFile, 
        Set.of("image/jpeg", "image/png", "image/gif")),
        "Valid mime type should pass validation");
  }

  @Test
  void testValidateMimeTypeInvalid() throws IOException {
    MultipartFile mockFile = mock(MultipartFile.class);
    when(mockFile.getContentType()).thenReturn("application/pdf");

    IOException exception = assertThrows(IOException.class,
        () -> FileValidator.validateMimeType(mockFile, 
            Set.of("image/jpeg", "image/png")),
        "Invalid mime type should throw IOException");
    assertTrue(exception.getMessage().contains("Invalid file type"));
  }

  @Test
  void testValidateFilenameValid() throws IOException {
    assertDoesNotThrow(() -> FileValidator.validateFilename("image.jpg"),
        "Valid filename should pass validation");
  }

  @Test
  void testValidateFilenameWithTraversal() throws IOException {
    IOException exception = assertThrows(IOException.class,
        () -> FileValidator.validateFilename("../../../etc/passwd.jpg"),
        "Filename with path traversal should throw IOException");
    assertTrue(exception.getMessage().contains("path traversal"));
  }

  @Test
  void testValidateFilenameAbsolutePath() throws IOException {
    IOException exception = assertThrows(IOException.class,
        () -> FileValidator.validateFilename("/etc/passwd"),
        "Filename with absolute path should throw IOException");
    assertTrue(exception.getMessage().contains("absolute path"));
  }

  @Test
  void testValidateFilenameHiddenDotFile() throws IOException {
    IOException exception = assertThrows(IOException.class,
        () -> FileValidator.validateFilename(".hiddenfile"),
        "Hidden dot filename should throw IOException");
    assertTrue(exception.getMessage().contains("hidden files are not allowed"));
  }

  @Test
  void testIsTraversalWithDoubleDotSlash() {
    IOException exception = assertThrows(IOException.class,
        () -> FileValidator.validateFilename("folder/../../../secret.jpg"),
        "Traversal pattern with ../ should be detected");
    assertTrue(exception.getMessage().contains("path traversal"));
  }

  @Test
  void testIsTraversalWithDoubleBackslash() {
    IOException exception = assertThrows(IOException.class,
        () -> FileValidator.validateFilename("folder\\..\\..\\secret.jpg"),
        "Traversal pattern with ..\\ should be detected");
    assertTrue(exception.getMessage().contains("path traversal"));
  }

  @Test
  void testIsHiddenDotFileSimple() {
    IOException exception = assertThrows(IOException.class,
        () -> FileValidator.validateFilename(".secret.jpg"),
        "Hidden file starting with dot should be detected");
    assertTrue(exception.getMessage().contains("hidden files are not allowed"));
  }

  @Test
  void testIsHiddenDotFileInPath() {
    IOException exception = assertThrows(IOException.class,
        () -> FileValidator.validateFilename("folder/.hidden"),
        "Hidden file in path should be detected");
    assertTrue(exception.getMessage().contains("hidden files are not allowed"));
  }

  @Test
  void testValidateFilenameEmpty() throws IOException {
    IOException exception = assertThrows(IOException.class,
        () -> FileValidator.validateFilename(""),
        "Empty filename should throw IOException");
    assertTrue(exception.getMessage().contains("Filename is missing"));
  }

}
