package com.kpn.rss.parser.infra.streams;

import com.kpn.rss.parser.domain.model.Outage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
@RequiredArgsConstructor
public class OutageCollector implements Processor<String, Outage, Void, Void> {

    private final String stateStoreName;
    private KeyValueStore<String, Outage> store;

    @Override
    public void init(final ProcessorContext<Void, Void> context) {
        this.store = context.getStateStore(this.stateStoreName);
    }

    @Override
    public void process(final Record<String, Outage> record) {
        this.store.put(record.key(), record.value());
    }

    @Override
    public void close() {
        //NOOP
    }

}
