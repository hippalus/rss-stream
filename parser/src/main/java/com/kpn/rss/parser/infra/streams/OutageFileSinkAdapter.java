package com.kpn.rss.parser.infra.streams;

import com.kpn.rss.parser.domain.model.Outage;
import com.kpn.rss.parser.infra.configuration.ApplicationConfiguration;
import com.kpn.rss.parser.infra.service.OutageFileWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.kpn.rss.parser.infra.streams.Constants.BUSINESS_OUTAGES_STORE;
import static com.kpn.rss.parser.infra.streams.Constants.CUSTOMER_OUTAGES_STORE;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutageFileSinkAdapter {

    private final ApplicationConfiguration applicationConfiguration;
    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;
    private final OutageFileWriter outageFileWriter;

    //TODO:Use a distributed scheduler like Quartz to avoid multiple instance executions
    @Scheduled(fixedDelayString = "200")
    public void flushBusinessOutages() {
        final ReadOnlyKeyValueStore<String, Outage> businessOutagesStore = this.getStore(BUSINESS_OUTAGES_STORE);

        final List<Outage> businessOutages = getOutages(businessOutagesStore);

        this.outageFileWriter.writeToJsonFile(this.applicationConfiguration.getFiles().getBusinessOutagesFile(), businessOutages);
    }

    //TODO:Use a distributed scheduler like Quartz to avoid multiple instance executions
    @Scheduled(fixedDelayString = "200")
    public void flushCustomerOutages() {
        final ReadOnlyKeyValueStore<String, Outage> customerOutagesStore = this.getStore(CUSTOMER_OUTAGES_STORE);

        final List<Outage> customerOutages = getOutages(customerOutagesStore);

        this.outageFileWriter.writeToJsonFile(this.applicationConfiguration.getFiles().getCustomerOutagesFile(), customerOutages);
    }

    private ReadOnlyKeyValueStore<String, Outage> getStore(final String storeName) {
        return Objects.requireNonNull(this.streamsBuilderFactoryBean.getKafkaStreams())
                .store(StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore()));
    }

    private static List<Outage> getOutages(final ReadOnlyKeyValueStore<String, Outage> store) {
        final List<Outage> outages = new ArrayList<>();
        try (final KeyValueIterator<String, Outage> iterator = store.all()) {
            iterator.forEachRemaining(kv -> outages.add(kv.value));
        }
        return outages;
    }
}
