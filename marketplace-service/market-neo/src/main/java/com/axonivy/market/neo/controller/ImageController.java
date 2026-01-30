package com.axonivy.market.neo.controller;

import com.axonivy.market.core.controller.CoreImageController;
import com.axonivy.market.core.service.CoreImageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.axonivy.market.core.constants.CoreRequestMappingConstants.IMAGE;

@RestController
@RequestMapping(IMAGE)
@Tag(name = "Image Controllers", description = "API collection to get image's detail.")
public class ImageController extends CoreImageController {
  public ImageController(CoreImageService coreImageService) {
    super(coreImageService);
  }
}
