package com.kpn.rss.fetcher.infra.service.client;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;

@HttpExchange
public interface OutagesRssClient {

    @GetExchange(value = "/rss/outages.xml", accept = MediaType.APPLICATION_XML_VALUE)
    Mono<ResponseEntity<byte[]>> outages();
}
