package com.axonivy.market.stable.controller;

import com.axonivy.market.core.controller.CoreAppController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.axonivy.market.stable.constants.RequestMappingConstants.ROOT;

@RestController
@RequestMapping(ROOT)
public class AppController extends CoreAppController {
}
