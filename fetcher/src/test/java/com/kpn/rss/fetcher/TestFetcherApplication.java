package com.kpn.rss.fetcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestFetcherApplication {

    public static void main(final String[] args) {
        SpringApplication.from(FetcherApplication::main).with(TestFetcherApplication.class).run(args);
    }

}
