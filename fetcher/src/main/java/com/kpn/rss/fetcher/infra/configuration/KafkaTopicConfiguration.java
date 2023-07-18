package com.kpn.rss.fetcher.infra.configuration;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Map;

import static com.kpn.rss.fetcher.infra.configuration.FeedProviderConfigurations.ProviderId;
import static com.kpn.rss.fetcher.infra.configuration.FeedProviderConfigurations.TopicProperties;


@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfiguration {

    private final KafkaProperties kafkaProperties;
    private final FeedProviderConfigurations feedProviderConfigurations;

    @Bean
    public KafkaAdmin admin() {
        final Map<String, Object> configs = this.kafkaProperties.buildAdminProperties();
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic outagesTopic() {
        final TopicProperties outageTopic = this.feedProviderConfigurations.getOutputTopic(ProviderId.OUTAGES);
        return TopicBuilder.name(outageTopic.getName())
                .replicas(outageTopic.getPartition())
                .partitions(outageTopic.getPartition())
                .build();
    }

}
