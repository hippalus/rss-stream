package com.kpn.rss.fetcher.infra.service;

import com.kpn.rss.fetcher.domain.model.Item;
import com.kpn.rss.fetcher.domain.service.FeedService;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.kpn.rss.fetcher.infra.configuration.FeedProviderConfigurations.ProviderId;

//TODO: use Bloom filter or Offset storage or distributed cache like Redis
@Repository
public class FeedServiceInMemoryAdapter implements FeedService {

    private static final Map<ProviderId, Set<Item>> items = new ConcurrentHashMap<>();

    @Override
    public Set<Item> findByProvider(final ProviderId providerId) {
        return items.getOrDefault(providerId, new HashSet<>());
    }

    @Override
    public void save(final ProviderId providerId, final Set<Item> newItems) {
        if (CollectionUtils.isEmpty(newItems)) {
            return;
        }
        final Set<Item> itemSet = this.findByProvider(providerId);
        itemSet.addAll(newItems);
        items.put(providerId, itemSet);
    }
}
