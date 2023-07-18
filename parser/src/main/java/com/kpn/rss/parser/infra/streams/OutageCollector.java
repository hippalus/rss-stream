package com.kpn.rss.parser.infra.streams;

import com.kpn.rss.parser.domain.model.Outage;
import com.kpn.rss.parser.domain.model.OutageType;
import com.kpn.rss.parser.domain.service.OutageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class OutageCollector implements Processor<String, Outage, Void, Void> {

    private final String stateStoreName;
    private final OutageType outageType;
    private final OutageService outageService;

    private KeyValueStore<String, Outage> store;
    private ScheduledExecutorService executorService;

    @Override
    public void init(final ProcessorContext<Void, Void> context) {
        this.store = context.getStateStore(this.stateStoreName);
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.executorService.scheduleAtFixedRate(this::writeOutages, 0, 200, TimeUnit.MILLISECONDS);
    }

    private void writeOutages() {
        try (final KeyValueIterator<String, Outage> iterator = this.store.all()) {

            final Set<Outage> outages = new HashSet<>();
            while (iterator.hasNext()) {
                outages.add(iterator.next().value);
            }

            switch (Objects.requireNonNull(this.outageType)) {
                case CUSTOMER -> this.outageService.saveCustomerOutages(outages);
                case BUSINESS -> this.outageService.saveBusinessOutages(outages);
            }
        }
    }

    @Override
    public void process(final Record<String, Outage> record) {
        this.store.put(record.key(), record.value());
    }

    @Override
    public void close() {
        try {
            this.executorService.shutdown();
            if (!this.executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                log.info("Executor did not terminate in the specified time.");
                final List<Runnable> droppedTasks = this.executorService.shutdownNow();
                log.info("Executor was abruptly shut down. {}  tasks will not be executed", droppedTasks.size());
            }
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("Exception occurred while waiting for executor service to shutdown", ex);
        }
    }

}
