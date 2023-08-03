package com.kpn.rss.parser.domain.model.inbound;

import lombok.Builder;

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

}
