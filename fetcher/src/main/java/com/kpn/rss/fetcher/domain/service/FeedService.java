package com.kpn.rss.fetcher.domain.service;

import com.kpn.rss.fetcher.domain.model.Item;

import java.util.Set;

import static com.kpn.rss.fetcher.infra.configuration.FeedProviderConfigurations.ProviderId;

public interface FeedService {

    Set<Item> findByProvider(ProviderId providerId);

    void save(ProviderId providerId, Set<Item> items);
}
