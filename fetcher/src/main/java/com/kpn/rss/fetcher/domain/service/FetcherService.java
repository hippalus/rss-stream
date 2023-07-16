package com.kpn.rss.fetcher.domain.service;

import com.kpn.rss.fetcher.domain.model.Item;

import java.util.Set;

public interface FetcherService {

    Set<Item> fetchNewItems(final Set<Item> lastItems);

}
