package com.kpn.rss.parser.infra.streams;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;

public record Topic<K, V>(String name, Serde<K> keySerde, Serde<V> valueSerde) {

    public Consumed<K, V> consumed() {
        return Consumed.with(this.keySerde, this.valueSerde);
    }

    public Produced<K, V> produced() {
        return Produced.with(this.keySerde, this.valueSerde);
    }
}
