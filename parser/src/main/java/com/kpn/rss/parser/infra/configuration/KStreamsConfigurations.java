package com.kpn.rss.parser.infra.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.CooperativeStickyAssignor;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.DefaultProductionExceptionHandler;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.streams.RecoveringDeserializationExceptionHandler;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Configuration
@EnableKafka
@EnableKafkaStreams
@RequiredArgsConstructor
public class KStreamsConfigurations {
    // Constant configurations
    private static final String CONSUMER_AUTO_OFFSET_RESET_CONFIG = "earliest";
    private static final String CONSUMER_SESSION_TIMEOUT_MS_CONFIG = "60000";
    private static final String NUM_STREAM_THREADS_CONFIG = "3"; //TODO: get from env
    private static final String METRICS_RECORDING_LEVEL_CONFIG = "DEBUG";


    private final KafkaProperties kafkaProperties;


    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration defaultKafkaStreamsConfig() {
        final Map<String, Object> config = this.buildStreamConfig();

        return new KafkaStreamsConfiguration(config);
    }

    @Bean
    public StreamsBuilderFactoryBeanConfigurer streamsBuilderFactoryBeanConfigurer() {
        return streamsBuilderFactoryBean -> {
            streamsBuilderFactoryBean.setStateListener((newState, oldState) -> log.warn("State transition from {} to {} ", oldState, newState));
        };
    }

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer() {
        return new DeadLetterPublishingRecoverer(
                this.errorKafkaTemplate(),
                (consumerRecord, ex) -> new TopicPartition("recoverer.DLT", consumerRecord.partition())
        );
    }

    @Bean
    public KafkaTemplate<Object, Object> errorKafkaTemplate() {
        return new KafkaTemplate<>(this.errorProducerFactory());
    }

    @Bean
    public ProducerFactory<Object, Object> errorProducerFactory() {
        final Map<String, Object> producerProperties = this.kafkaProperties.buildProducerProperties();
        final Map<Class<?>, Serializer<?>> serializers = this.buildSerializerMap();

        final var delegatingByTypeSerializer = new DelegatingByTypeSerializer(serializers);

        return new DefaultKafkaProducerFactory<>(
                producerProperties,
                delegatingByTypeSerializer,
                delegatingByTypeSerializer
        );
    }

    private Map<Class<?>, Serializer<?>> buildSerializerMap() {
        final var serializers = new HashMap<Class<?>, Serializer<?>>(4);

        serializers.put(UUID.class, new UUIDSerializer());
        serializers.put(String.class, new StringSerializer());
        serializers.put(JsonNode.class, new JsonSerializer<>());
        serializers.put(byte[].class, new ByteArraySerializer());

        return serializers;
    }

    private Map<String, Object> buildStreamConfig() {
        final Map<String, Object> config = this.kafkaProperties.buildStreamsProperties();
        // Common configurations
        config.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
        config.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, "1000");

        // Consumer configurations
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, CONSUMER_AUTO_OFFSET_RESET_CONFIG);
        config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, CONSUMER_SESSION_TIMEOUT_MS_CONFIG);
        config.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, CooperativeStickyAssignor.class.getName());

        // Stream configurations
        config.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, NUM_STREAM_THREADS_CONFIG);
        config.put(StreamsConfig.METRICS_RECORDING_LEVEL_CONFIG, METRICS_RECORDING_LEVEL_CONFIG);
        config.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class);

        // Serialization configurations
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class);

        // Exception Handler configurations
        config.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, RecoveringDeserializationExceptionHandler.class);
        config.put(StreamsConfig.DEFAULT_PRODUCTION_EXCEPTION_HANDLER_CLASS_CONFIG, DefaultProductionExceptionHandler.class);

        return config;
    }

}
