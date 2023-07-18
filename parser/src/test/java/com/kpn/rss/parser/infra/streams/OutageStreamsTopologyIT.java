package com.kpn.rss.parser.infra.streams;

import com.kpn.rss.parser.common.BusinessOutagesTestConsumer;
import com.kpn.rss.parser.common.CustomerOutagesTestConsumer;
import com.kpn.rss.parser.common.IT;
import com.kpn.rss.parser.domain.model.Outage;
import com.kpn.rss.parser.domain.model.OutageStatus;
import com.kpn.rss.parser.domain.model.inbound.Channel;
import com.kpn.rss.parser.domain.model.inbound.Item;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.kpn.rss.parser.infra.streams.Constants.BUSINESS_OUTAGES_STORE;
import static com.kpn.rss.parser.infra.streams.Constants.CUSTOMER_OUTAGES_STORE;

@IT
class OutageStreamsTopologyIT {

    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Autowired
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @Autowired
    private BusinessOutagesTestConsumer businessOutagesTestConsumer;

    @Autowired
    private CustomerOutagesTestConsumer customerOutagesTestConsumer;


    @BeforeEach
    void setUp() {
        this.businessOutagesTestConsumer.start();
        this.customerOutagesTestConsumer.start();
    }

    @AfterEach
    void tearDown() {
        this.businessOutagesTestConsumer.stop();
        this.customerOutagesTestConsumer.stop();
    }

    @Test
    void testOutageStreamsTopology() {

        final Channel channel = Channel.builder()
                .language("nl-NL")
                .copyright("Copyright 2008 KPN")
                .title("KPN JAMES")
                .description("KPN Storingen / werkzaamheden")
                .link("")
                .generator("KPN J.A.M.E.S")
                .build();

        final Item customer = Item.builder()
                .title("gepland Onderhoud 1706SC")
                .category("Storing")
                .ticketNumber(null)
                .postalCodes("1706SC;")
                .expectedEndDate(null)
                .categoryJames("DTAK")
                .locations("KPN;Glas;")
                .description("Als gevolg van gepland onderhoud, zullen werkzaamheden worden verricht aan de zender X. In verband met deze werkzaamheden is de uitzending van de genoemde zender voor kortere of langere tijd onderbrok...")
                .link(null)
                .channel(channel)
                .build();


        final Item business = Item.builder()
                .title("Test dummy ZM storing zipcode21")
                .category("Storing")
                .ticketNumber(null)
                .postalCodes("3333AA;4000AS;")
                .expectedEndDate(null)
                .categoryJames("DTAK")
                .locations("ZMST")
                .description("hellow storing message<br/>Starttijd: 2015-02-05 16:55&#160;Eindtijd: onbekend&#160;")
                .link(null)
                .channel(channel)
                .build();


        this.kafkaTemplate.send("outages", UUID.randomUUID().toString(), business).join();
        this.kafkaTemplate.send("outages", UUID.randomUUID().toString(), customer).join();

        this.customerOutagesTestConsumer.await(120, 1);
        this.businessOutagesTestConsumer.await(120, 1);

        final ReadOnlyKeyValueStore<String, Outage> businessOutagesStore =
                Objects.requireNonNull(this.streamsBuilderFactoryBean.getKafkaStreams())
                        .store(StoreQueryParameters.fromNameAndType(BUSINESS_OUTAGES_STORE, QueryableStoreTypes.keyValueStore()));

        final ReadOnlyKeyValueStore<String, Outage> customerOutagesStore =
                Objects.requireNonNull(this.streamsBuilderFactoryBean.getKafkaStreams())
                        .store(StoreQueryParameters.fromNameAndType(CUSTOMER_OUTAGES_STORE, QueryableStoreTypes.keyValueStore()));

        Awaitility.await().atMost(Duration.ofSeconds(120)).until(() -> !getOutages(businessOutagesStore).isEmpty());
        Awaitility.await().atMost(Duration.ofSeconds(120)).until(() -> !getOutages(customerOutagesStore).isEmpty());

        final Set<Outage> businessOutages = getOutages(businessOutagesStore);
        final Set<Outage> customerOutages = getOutages(customerOutagesStore);

        Assertions.assertThat(businessOutages.stream().findFirst().get())
                .returns(business.description(), Outage::description)
                .returns(business.postalCodes(), Outage::postalCodes)
                .returns(business.title(), Outage::title)
                .returns("onbekend", Outage::endDate)
                .returns("2015-02-05 16:55", Outage::startDate)
                .returns(OutageStatus.CURRENT, Outage::status);

        Assertions.assertThat(customerOutages.stream().findFirst().get())
                .returns(customer.description(), Outage::description)
                .returns(customer.postalCodes(), Outage::postalCodes)
                .returns(customer.title(), Outage::title)
                .returns("onbekend", Outage::endDate)
                .returns("onbekend", Outage::startDate)
                .returns(OutageStatus.CURRENT, Outage::status);
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


}
