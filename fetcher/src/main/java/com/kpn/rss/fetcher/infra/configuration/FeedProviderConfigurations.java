package com.kpn.rss.fetcher.infra.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Data
@ConfigurationProperties(prefix = "fetcher")
public class FeedProviderConfigurations {

    private Map<ProviderId, FeedProviderConfiguration> rssFeedProviders = new HashMap<>();

    @Autowired
    private WebClient.Builder builder;


    public WebClient createWebClient(final ProviderId providerId) {
        final FeedProviderConfiguration configuration = this.getConfiguration(providerId);
        return this.builder.baseUrl(configuration.getUrl()).build();
    }

    public FeedProviderConfiguration getConfiguration(final ProviderId providerId) {
        return this.rssFeedProviders.get(providerId);
    }

    public String getOutputTopic(final ProviderId providerId) {
        return this.rssFeedProviders.get(providerId).getOutputTopic();
    }

    @Data
    @Configuration
    public static class FeedProviderConfiguration {
        private String url;
        private String outputTopic;
        private Duration fetchPeriod = Duration.ofSeconds(20);
    }

    public enum ProviderId {
        OUTAGES
    }
}
