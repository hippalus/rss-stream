package com.kpn.rss.feedsimulator;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rss")
public class RssFeedController {

    private final ResourceLoader resourceLoader;

    @GetMapping(value = "/outages.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<byte[]> outages() throws IOException {
        final Resource resource = this.resourceLoader.getResource("classpath:outages.xml");
        final byte[] outagesBytes = resource.getInputStream().readAllBytes();
        return ResponseEntity.ok().body(outagesBytes);
    }

    //TODO:  Add POST, PUT or PATCH endpoints for crud operation of rss feeds to make realistic simulation.
}
