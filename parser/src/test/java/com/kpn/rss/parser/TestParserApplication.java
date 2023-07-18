package com.kpn.rss.parser;

import com.kpn.rss.parser.common.IntegrationTestConfiguration;
import org.springframework.boot.SpringApplication;

public class TestParserApplication {

    public static void main(final String[] args) {
        SpringApplication.from(ParserApplication::main)
                .with(IntegrationTestConfiguration.class)
                .run(args);
    }

}
