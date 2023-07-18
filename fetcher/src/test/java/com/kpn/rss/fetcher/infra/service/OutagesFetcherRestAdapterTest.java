package com.kpn.rss.fetcher.infra.service;

import com.kpn.rss.fetcher.domain.model.Item;
import com.kpn.rss.fetcher.domain.service.FetcherService;
import com.kpn.rss.fetcher.infra.service.client.OutagesRssClient;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class OutagesFetcherRestAdapterTest {

    @Mock
    private OutagesRssClient outagesRssClient;

    private FetcherService fetcherService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.fetcherService = new OutagesFetcherRestAdapter(this.outagesRssClient);
    }

    @Test
    void fetchNewItems_shouldReturnNewItems() {

        final byte[] rssData = readFileFull("outages.xml").getBytes();
        final ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(rssData, HttpStatus.OK);
        when(this.outagesRssClient.outages()).thenReturn(Mono.just(responseEntity));

        final Set<Item> result = this.fetcherService.fetchNewItems(Set.of());

        verify(this.outagesRssClient, times(1)).outages();

        assertEquals(123, result.size());
    }

    public static String readFileFull(final String path) {
        return readFileLines(path).collect(Collectors.joining(" "));
    }

    @SneakyThrows
    public static Stream<String> readFileLines(final String path) {
        final URI resource = ClassLoader.getSystemResource(path).toURI();
        return Files.lines(Paths.get(resource));
    }
}
