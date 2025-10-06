package com.axonivy.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum DocumentLanguage {

    ENGLISH("en"), JAPANESE("ja");

    private final String code;

    public static List<String> getCodes() {
        return Stream.of(DocumentLanguage.values()).map(DocumentLanguage::getCode).toList();
    }

    public static DocumentLanguage fromCode(String code) {
        return Arrays.stream(values())
                .filter(lang -> lang.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported language: " + code));
    }

}
