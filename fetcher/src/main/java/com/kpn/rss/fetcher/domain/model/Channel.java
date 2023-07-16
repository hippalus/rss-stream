package com.kpn.rss.fetcher.domain.model;

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