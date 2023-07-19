package com.kpn.rss.parser.infra.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.kpn.rss.parser.domain.model.Outage;
import com.kpn.rss.parser.infra.configuration.ApplicationConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.JacksonUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutageFileWriter {
    private static final ObjectMapper OBJECT_MAPPER = JacksonUtils.enhancedObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private static final ObjectWriter OBJECT_WRITER_WITH_DEFAULT_PRETTY_PRINTER = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();

    private final ApplicationConfiguration applicationConfiguration;

    public void writeToJsonFile(final String pathname, final List<Outage> outages) {
        try {
            final File file = new File(pathname);

            if (this.applicationConfiguration.getFiles().isIndentOutputEnabled()) {
                OBJECT_WRITER_WITH_DEFAULT_PRETTY_PRINTER.writeValue(file, outages);
            } else {
                OBJECT_MAPPER.writeValue(file, outages);
            }
        } catch (final IOException e) {
            log.error("Exception occurred while writing the outages to file {}", pathname, e);
        }
    }
}
