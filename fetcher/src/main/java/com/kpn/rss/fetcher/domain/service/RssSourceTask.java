package com.kpn.rss.fetcher.domain.service;

import com.kpn.rss.fetcher.domain.model.Item;
import com.kpn.rss.fetcher.infra.configuration.FeedProviderConfigurations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import static com.kpn.rss.fetcher.infra.configuration.FeedProviderConfigurations.ProviderId;

//TODO: Can be Kafka Source Connector. @see https://github.com/kaliy/kafka-connect-rss
@Slf4j
@Service
@RequiredArgsConstructor
public class RssSourceTask {

    private final FetcherService fetcherService;
    private final FeedService feedService;
    private final FeedItemPublisher eventPublisher;
    private final FeedProviderConfigurations feedProviderConfigurations;

    //TODO: Use a distributed scheduler like Quartz the avoid multiple instance executions
    @Scheduled(fixedRateString = "${fetcher.rss-feed-providers.outages.fetch-period}")
    public void poll() {
        Arrays.stream(ProviderId.values()).parallel().forEach(this::poll);
    }

    private void poll(final ProviderId providerId) {
        log.info("Polling for new messages from {}", providerId);

        final Set<Item> lastFeedsById = this.feedService.findByProvider(providerId);
        final Set<Item> newEvents = this.fetcherService.fetchNewItems(lastFeedsById);

        log.info("Got {} new items from {}", newEvents.size(), providerId);

        this.feedService.save(providerId, newEvents);

        final String outputTopic = this.feedProviderConfigurations.getOutputTopicName(providerId);

        //TODO: may be needs to transactional outbox.
        newEvents.forEach(
                item -> this.eventPublisher.publish(
                        outputTopic,
                        // TODO: any other partition key or custom partitioner
                        UUID.nameUUIDFromBytes(item.toBase64().getBytes(StandardCharsets.UTF_8)),
                        item
                )
        );
    }


}
