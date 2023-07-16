package com.kpn.rss.fetcher.domain.service;

import com.kpn.rss.fetcher.domain.model.Item;

public interface FeedItemPublisher {

    void publish(String target, Object key, Item value);
}
