package com.axonivy.market.controller;

import com.axonivy.market.core.controller.CoreAppController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.axonivy.market.constants.RequestMappingConstants.ROOT;

@RestController
@RequestMapping(ROOT)
public class AppController extends CoreAppController {
}
