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

    @JsonIgnore
    public boolean isBusinessOutage() {
        return this.type() == OutageType.BUSINESS;
    }

    @JsonIgnore
    public boolean isCustomerOutage() {
        return this.type() == OutageType.CUSTOMER;
    }

}
