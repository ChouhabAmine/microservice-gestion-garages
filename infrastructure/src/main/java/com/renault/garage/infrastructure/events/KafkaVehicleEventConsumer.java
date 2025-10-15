package com.renault.garage.infrastructure.events;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class KafkaVehicleEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaVehicleEventConsumer.class);

    private final KafkaReceiver<String, String> receiver;
    private Disposable subscription;

    public KafkaVehicleEventConsumer(String bootstrapServers, String topic, String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        ReceiverOptions<String, String> options = ReceiverOptions.<String, String>create(props)
                .subscription(Collections.singleton(topic));
        this.receiver = KafkaReceiver.create(options);
    }

    public void start() {
        if (subscription == null) {
            subscription = receiver.receive()
                    .doOnNext(record -> log.info("Consumed vehicle event: key={}, value={}", record.key(), record.value()))
                    .subscribe();
        }
    }

    public void stop() {
        if (subscription != null) {
            subscription.dispose();
            subscription = null;
        }
    }
}
