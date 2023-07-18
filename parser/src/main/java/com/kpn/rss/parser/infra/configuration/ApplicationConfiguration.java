package com.kpn.rss.parser.infra.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "rss-stream-parser-app")
public class ApplicationConfiguration {

    private Map<TopicId, TopicProperties> topics = new EnumMap<>(TopicId.class);
    private FileProperties files = new FileProperties();

    @Data
    @Configuration
    public static class TopicProperties {
        private String name;
        private int partition;
        private int replication;
    }

    @Data
    @Configuration
    public static class FileProperties {
        private String businessOutagesFile;
        private String customerOutagesFile;
        private boolean indentOutputEnabled;
    }

    public enum TopicId {
        OUTAGES,
        BUSINESS_OUTAGES,
        CUSTOMER_OUTAGES
    }

    public TopicProperties getTopicConfig(final TopicId topicId) {
        return this.topics.get(topicId);
    }
}
