package com.axonivy.market.controller;

import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.axonivy.market.core.constants.CoreRequestMappingConstants.IMAGE;

@RestController
@RequestMapping(IMAGE)
public class FileController {

  @GetMapping("/read")
  public String readFile(@RequestParam("name") String fileName) throws IOException {
    File file = new File("/app/data/" + fileName);
    return new String(Files.readAllBytes(file.toPath()));
  }

}
