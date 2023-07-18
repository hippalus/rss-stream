package com.kpn.rss.parser.domain.model.inbound;

import com.kpn.rss.parser.domain.model.OutageType;
import lombok.Builder;

import java.util.Objects;

@Builder
public record Item(
        String title,
        String category,
        String ticketNumber,
        String postalCodes,
        String expectedEndDate,
        String categoryJames,
        String locations,
        String description,
        String link,
        Channel channel
) {

    public OutageType outageType() {
        final String locations = this.locations();
        //TODO: can use generic  predicates for customization
        if (Objects.nonNull(locations) && (locations.contains("ZMOH") || locations.contains("ZMST"))) {
            return OutageType.BUSINESS;
        }
        return OutageType.CUSTOMER;
    }

    public boolean isBusinessOutage() {
        return this.outageType() == OutageType.BUSINESS;
    }

    public boolean isCustomerOutage() {
        return this.outageType() == OutageType.CUSTOMER;
    }
}
