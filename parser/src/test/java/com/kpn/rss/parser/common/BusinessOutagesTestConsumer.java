package com.kpn.rss.parser.common;

import com.kpn.rss.parser.domain.model.Outage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Service;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Import(KafkaTestConfiguration.class)
@RequiredArgsConstructor
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class BusinessOutagesTestConsumer {

    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    private final Deque<ConsumerRecord<String, Outage>> eventsQueue = new ConcurrentLinkedDeque<>();

    @KafkaListener(
            id = "BusinessOutagesKafkaTestConsumer",
            topics = "business-outages",
            autoStartup = "false",
            containerFactory = "testKafkaListenerContainerFactory",
            groupId = "BusinessOutagesKafkaTestConsumer"
    )
    public void consume(final ConsumerRecord<String, Outage> event) {
        this.consumeInternal(event);
    }

    public void start() {
        this.kafkaListenerEndpointRegistry.getListenerContainer("BusinessOutagesKafkaTestConsumer").start();
    }

    public void stop() {
        this.kafkaListenerEndpointRegistry.getListenerContainer("BusinessOutagesKafkaTestConsumer").stop();
    }

    private void consumeInternal(final ConsumerRecord<String, Outage> event) {
        log.info("Event received:: {}", event);
        this.keepEvent(event);
        log.info("Event added to the kafka event list as {}th entry, event: {}", this.eventCount(), event);
    }

    public void await(final long maxWaitSecond, final int expectedEventSize) {
        Awaitility.await().atMost(maxWaitSecond, TimeUnit.SECONDS).until(() -> this.eventCount() >= expectedEventSize);

        log.info(
                "Tried {} seconds. Expected event count is {} and actual event size is {}",
                maxWaitSecond, expectedEventSize, this.eventCount()
        );
    }

    private void keepEvent(final ConsumerRecord<String, Outage> event) {
        this.eventsQueue.add(event);
    }

    public int eventCount() {
        return this.eventsQueue.size();
    }

    public void reset() {
        this.eventsQueue.clear();
    }

    public ConsumerRecord<String, Outage> pop() {
        final var event = this.eventsQueue.pollFirst();
        log.info("Last event from the consumed list is popped for validation. Event: {}", event);
        return event;
    }

    public List<ConsumerRecord<String, Outage>> popAll() {
        final var eventsAsList = this.eventsQueue.stream().toList();
        log.info("All {} events from the consumed list is popped for validation.", this.eventCount());
        this.eventsQueue.clear();
        return eventsAsList;
    }


}
