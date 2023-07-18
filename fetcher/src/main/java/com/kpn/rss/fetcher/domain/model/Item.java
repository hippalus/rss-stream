package com.kpn.rss.fetcher.domain.model;

import lombok.Builder;

import java.util.Base64;
import java.util.StringJoiner;

import static java.nio.charset.StandardCharsets.UTF_8;

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

    private static final String BASE64_FIELD_DELIMITER = "|";

    public String toBase64() {
        return Base64.getEncoder().encodeToString(
                new StringJoiner(BASE64_FIELD_DELIMITER)
                        .add(this.title)
                        .add(this.link)
                        .add(this.title)
                        .add(this.category)
                        .add(this.ticketNumber)
                        .add(this.postalCodes)
                        .add(this.expectedEndDate)
                        .add(this.categoryJames)
                        .add(this.locations)
                        .add(this.description)
                        .add(this.link)
                        .toString()
                        .getBytes(UTF_8)
        );
    }
}
