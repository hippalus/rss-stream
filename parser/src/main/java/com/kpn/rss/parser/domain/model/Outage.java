package com.kpn.rss.parser.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

@Builder
public record Outage(
        String title,
        String postalCodes,
        String startDate,
        String endDate,
        OutageStatus status,
        String description,
        @JsonIgnore
        OutageType type
) {

}
