package com.kpn.rss.parser.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OutageStatus {
    CURRENT("Actueel"),
    PLANNED("Gepland"),
    RESOLVED("Opgelost");

    @JsonValue
    private final String status;

}
