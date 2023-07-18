package com.kpn.rss.parser.infra.streams;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.kpn.rss.parser.domain.model.Outage;
import com.kpn.rss.parser.infra.configuration.ApplicationConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.support.JacksonUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.kpn.rss.parser.infra.streams.Constants.BUSINESS_OUTAGES_STORE;
import static com.kpn.rss.parser.infra.streams.Constants.CUSTOMER_OUTAGES_STORE;


@Slf4j
@Service
@RequiredArgsConstructor
public class OutageFileSinkAdapter {

    private static final ObjectMapper OBJECT_MAPPER = JacksonUtils.enhancedObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private static final ObjectWriter OBJECT_WRITER_WITH_DEFAULT_PRETTY_PRINTER = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();

    private final ApplicationConfiguration applicationConfiguration;
    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    //TODO: Use a distributed scheduler like Quartz the avoid multiple instance executions
    @Scheduled(fixedDelayString = "200")
    public void flushBusinessOutages() {
        final ReadOnlyKeyValueStore<String, Outage> businessOutagesStore =
                Objects.requireNonNull(this.streamsBuilderFactoryBean.getKafkaStreams())
                        .store(StoreQueryParameters.fromNameAndType(BUSINESS_OUTAGES_STORE, QueryableStoreTypes.keyValueStore()));

        final Set<Outage> businessOutages = getOutages(businessOutagesStore);

        this.writeToJsonFile(this.applicationConfiguration.getFiles().getBusinessOutagesFile(), businessOutages);
    }

    //TODO: Use a distributed scheduler like Quartz the avoid multiple instance executions
    @Scheduled(fixedDelayString = "200")
    public void flushCustomerOutages() {
        final ReadOnlyKeyValueStore<String, Outage> customerOutagesStore =
                Objects.requireNonNull(this.streamsBuilderFactoryBean.getKafkaStreams())
                        .store(StoreQueryParameters.fromNameAndType(CUSTOMER_OUTAGES_STORE, QueryableStoreTypes.keyValueStore()));

        final Set<Outage> customerOutages = getOutages(customerOutagesStore);

        this.writeToJsonFile(this.applicationConfiguration.getFiles().getCustomerOutagesFile(), customerOutages);
    }

    private static Set<Outage> getOutages(final ReadOnlyKeyValueStore<String, Outage> store) {
        try (final KeyValueIterator<String, Outage> iterator = store.all()) {

            final Set<Outage> outages = new HashSet<>();
            while (iterator.hasNext()) {
                outages.add(iterator.next().value);
            }

            return outages;
        }
    }

    private void writeToJsonFile(final String pathname, final Set<Outage> outages) {
        try {
            final File file = new File(pathname);

            if (this.applicationConfiguration.getFiles().isIndentOutputEnabled()) {
                OBJECT_WRITER_WITH_DEFAULT_PRETTY_PRINTER.writeValue(file, outages);
            } else {
                OBJECT_MAPPER.writeValue(file, outages);
            }
        } catch (final Exception e) {
            log.error("Exception has been occurred while writing the outages to file {}", pathname, e);
        }
    }
}
