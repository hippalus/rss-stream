package com.kpn.rss.fetcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FetcherApplication {

    public static void main(final String[] args) {
        SpringApplication.run(FetcherApplication.class, args);
    }

}
