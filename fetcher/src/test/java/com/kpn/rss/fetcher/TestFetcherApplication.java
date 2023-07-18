package com.kpn.rss.fetcher;

import com.kpn.rss.fetcher.common.IntegrationTestConfiguration;
import org.springframework.boot.SpringApplication;

public class TestFetcherApplication {

    public static void main(final String[] args) {
        SpringApplication.from(FetcherApplication::main)
                .with(IntegrationTestConfiguration.class)
                .run(args);
    }

}
