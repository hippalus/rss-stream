package com.kpn.rss.parser.domain.model;

import lombok.Builder;

@Builder
public record Outage(
        String title,
        String postalCodes,
        String startDate,
        String endDate,
        OutageStatus status,
        String description
) {

}
