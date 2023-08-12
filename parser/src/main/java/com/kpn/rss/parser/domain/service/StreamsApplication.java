package com.kpn.rss.parser.domain.service;

import org.apache.kafka.streams.StreamsBuilder;

public interface StreamsApplication {

    void buildTopology(StreamsBuilder streamsBuilder);
}
