package com.axonivy.market.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum WorkFlowType {
    CI,
    DEV;

    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }
}
