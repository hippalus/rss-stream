package com.kpn.rss.fetcher.infra.service;

import com.kpn.rss.fetcher.domain.model.Item;
import com.kpn.rss.fetcher.domain.service.FeedItemPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedItemPublisherKafkaAdapter implements FeedItemPublisher {

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Override
    public void publish(final String target, final Object key, final Item value) {
        this.kafkaTemplate.send(target, key, value)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send item {}", key, ex);
                    } else {
                        log.info("Item {} has been sent successfully!", key);
                    }
                });
    }
}
