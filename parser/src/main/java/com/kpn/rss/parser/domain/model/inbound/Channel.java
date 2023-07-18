package com.kpn.rss.parser.domain.model.inbound;

import lombok.Builder;

@Builder
public record Channel(
        String language,
        String copyright,
        String title,
        String description,
        String link,
        String generator
) {
}