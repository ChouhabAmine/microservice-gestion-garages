package com.renault.garage.infrastructure.events;

import com.renault.garage.domain.events.VehicleCreatedEvent;
import com.renault.garage.domain.ports.VehicleEventPublisher;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

import java.util.HashMap;
import java.util.Map;

public class KafkaVehicleEventPublisher implements VehicleEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(KafkaVehicleEventPublisher.class);
    private final KafkaSender<String, String> sender;
    private final String topic;

    public KafkaVehicleEventPublisher(String bootstrapServers, String topic) {
        this.topic = topic;
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        SenderOptions<String, String> options = SenderOptions.create(props);
        this.sender = KafkaSender.create(options);
    }

    @Override
    public Mono<Void> publish(com.renault.garage.domain.events.VehicleCreatedEvent event) {
        String key = event.garageId();
        String value = String.format("{\"vehicleId\":\"%s\",\"garageId\":\"%s\",\"model\":\"%s\",\"brand\":\"%s\"}",
                event.vehicleId(), event.garageId(), event.model(), event.brand());

        return sender.send(Mono.just(SenderRecord.create(topic, null, null, key, value, null)))
                .doOnError(err -> log.error("Failed to publish event", err))
                .then();
    }
}
