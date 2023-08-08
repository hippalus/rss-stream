package com.kpn.rss.parser.infra.configuration;

import com.kpn.rss.parser.domain.model.Outage;
import com.kpn.rss.parser.domain.model.inbound.Item;
import com.kpn.rss.parser.infra.streams.Topic;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

import static com.kpn.rss.parser.infra.configuration.ApplicationConfiguration.TopicId;
import static com.kpn.rss.parser.infra.configuration.ApplicationConfiguration.TopicProperties;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfiguration {

    private final KafkaProperties kafkaProperties;
    private final ApplicationConfiguration applicationConfiguration;

    @Bean
    public KafkaAdmin admin() {
        final Map<String, Object> configs = this.kafkaProperties.buildAdminProperties();
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic outages() {
        final TopicProperties outagesTopic = this.applicationConfiguration.getTopicConfig(TopicId.OUTAGES);
        return newTopic(outagesTopic);
    }

    @Bean
    public Topic<String, Item> outagesTopic(final NewTopic outages) {
        final JsonSerde<Item> itemJsonSerde = itemJsonSerde();

        return new Topic<>(outages.name(), Serdes.String(), itemJsonSerde);
    }

    @Bean
    public NewTopic businessOutages() {
        final TopicProperties businessTopic = this.applicationConfiguration.getTopicConfig(TopicId.BUSINESS_OUTAGES);

        return newTopic(businessTopic);
    }

    @Bean
    public Topic<String, Outage> businessOutagesTopic() {
        final NewTopic businessOutages = this.businessOutages();
        final JsonSerde<Outage> outageJsonSerde = outageSerde();

        return new Topic<>(businessOutages.name(), Serdes.String(), outageJsonSerde);
    }

    @Bean
    public NewTopic customerOutages() {
        final TopicProperties customerTopic = this.applicationConfiguration.getTopicConfig(TopicId.CUSTOMER_OUTAGES);
        return newTopic(customerTopic);
    }

    @Bean
    public Topic<String, Outage> customerOutagesTopic() {
        final NewTopic customerOutages = this.customerOutages();
        final JsonSerde<Outage> outageJsonSerde = outageSerde();

        return new Topic<>(customerOutages.name(), Serdes.String(), outageJsonSerde);
    }

    private static NewTopic newTopic(final TopicProperties customerTopic) {
        return TopicBuilder.name(customerTopic.getName())
                .partitions(customerTopic.getPartition())
                .replicas(customerTopic.getReplication())
                .build();
    }

    private static JsonSerde<Outage> outageSerde() {
        final JsonSerializer<Outage> outageJsonSerializer = new JsonSerializer<>();
        final JsonDeserializer<Outage> outageJsonDeserializer = new JsonDeserializer<>(Outage.class, false);
        return new JsonSerde<>(outageJsonSerializer, outageJsonDeserializer);
    }

    private static JsonSerde<Item> itemJsonSerde() {
        final JsonSerializer<Item> itemJsonSerializer = new JsonSerializer<>();
        final JsonDeserializer<Item> itemJsonDeserializer = new JsonDeserializer<>(Item.class, false);
        return new JsonSerde<>(itemJsonSerializer, itemJsonDeserializer);
    }
}
