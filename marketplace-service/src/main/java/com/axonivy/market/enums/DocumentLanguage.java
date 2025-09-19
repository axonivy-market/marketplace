package com.axonivy.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocumentLanguage {

    ENGLISH("en","/en/"), JAPANESE("ja","/ja/");

    private final String code;

    private final String path;

}
