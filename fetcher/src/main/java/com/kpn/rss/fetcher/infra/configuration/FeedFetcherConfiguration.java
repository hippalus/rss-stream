package com.kpn.rss.fetcher.infra.configuration;

import com.kpn.rss.fetcher.infra.service.client.OutagesRssClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
public class FeedFetcherConfiguration {

    private final FeedProviderConfigurations feedProviderConfigurations;

    @Bean
    public OutagesRssClient outagesRssClient() {
        final WebClient webClient = this.feedProviderConfigurations.createWebClient(FeedProviderConfigurations.ProviderId.OUTAGES);

        final var httpServiceProxyFactory = HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(webClient))
                .build();

        return httpServiceProxyFactory.createClient(OutagesRssClient.class);
    }
}
