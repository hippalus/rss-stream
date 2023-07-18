package com.kpn.rss.parser.infra.streams;

import com.kpn.rss.parser.domain.model.Outage;
import com.kpn.rss.parser.domain.model.OutageType;
import com.kpn.rss.parser.domain.model.inbound.Item;
import com.kpn.rss.parser.domain.service.OutageProcessor;
import com.kpn.rss.parser.domain.service.OutageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyDescription;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.kpn.rss.parser.infra.streams.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutageStreamsTopology {
    private final StreamsBuilder streamsBuilder;
    private final OutageProcessor outageProcessor;
    private final OutageService outageService;
    private final Topic<String, Item> outagesTopic;
    private final Topic<String, Outage> businessOutagesTopic;
    private final Topic<String, Outage> customerOutagesTopic;

    @PostConstruct
    public void build() {

        final Map<String, KStream<String, Item>> outageKStreamsByType =
                this.streamsBuilder.stream(this.outagesTopic.name(), this.outagesTopic.consumed())
                        .split(Named.as(BRANCH_PREFIX))
                        .branch((key, rssItem) -> rssItem.isBusinessOutage(), Branched.as(BUSINESS))
                        .branch((key, rssItem) -> rssItem.isCustomerOutage(), Branched.as(CUSTOMER))
                        .noDefaultBranch();

        outageKStreamsByType.get(BRANCH_BUSINESS)
                .mapValues(this.outageProcessor::createOutageFromItem)
                .to(this.businessOutagesTopic.name(), this.businessOutagesTopic.produced());

        outageKStreamsByType.get(BRANCH_CUSTOMER)
                .mapValues(this.outageProcessor::createOutageFromItem)
                .to(this.customerOutagesTopic.name(), this.customerOutagesTopic.produced());

        // Define state stores
        final StoreBuilder<KeyValueStore<String, Outage>> businessStoreBuilder =
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore(BUSINESS_OUTAGES_STORE),
                        this.businessOutagesTopic.keySerde(),
                        this.businessOutagesTopic.valueSerde()
                ).withLoggingDisabled();

        final StoreBuilder<KeyValueStore<String, Outage>> customerStoreBuilder =
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore(CUSTOMER_OUTAGES_STORE),
                        this.customerOutagesTopic.keySerde(),
                        this.customerOutagesTopic.valueSerde()
                ).withLoggingDisabled();

        this.streamsBuilder.addGlobalStore(
                businessStoreBuilder,
                this.businessOutagesTopic.name(),
                this.businessOutagesTopic.consumed(),
                () -> new OutageCollector(BUSINESS_OUTAGES_STORE, OutageType.CUSTOMER, this.outageService)
        );

        this.streamsBuilder.addGlobalStore(
                customerStoreBuilder,
                this.customerOutagesTopic.name(),
                this.customerOutagesTopic.consumed(),
                () -> new OutageCollector(CUSTOMER_OUTAGES_STORE, OutageType.CUSTOMER, this.outageService)
        );
        final Topology topology = this.streamsBuilder.build();

        final TopologyDescription topologyDescription = topology.describe();
        log.info(topologyDescription.toString());
    }

}
