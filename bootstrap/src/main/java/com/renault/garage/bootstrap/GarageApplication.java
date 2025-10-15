package com.renault.garage.bootstrap;



import com.renault.garage.domain.ports.VehicleEventPublisher;
import com.renault.garage.infrastructure.events.KafkaVehicleEventConsumer;
import com.renault.garage.infrastructure.events.KafkaVehicleEventPublisher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "com.renault.garage")
public class GarageApplication {


    @Bean
    public VehicleEventPublisher vehicleEventPublisher(
            @Value("${kafka.bootstrap-servers:localhost:9092}") String bootstrap,
            @Value("${kafka.topics.vehicle-created:vehicle-created}") String topic) {
        return new KafkaVehicleEventPublisher(bootstrap, topic);
    }

    @Bean
    public KafkaVehicleEventConsumer kafkaVehicleEventConsumer(
            @Value("${kafka.bootstrap-servers:localhost:9092}") String bootstrap,
            @Value("${kafka.topics.vehicle-created:vehicle-created}") String topic,
            @Value("${kafka.group-id:garage-consumer}") String groupId) {
        return new KafkaVehicleEventConsumer(bootstrap, topic, groupId);
    }

    public static void main(String[] args) {
        var ctx = SpringApplication.run(GarageApplication.class, args);
        ctx.getBean(KafkaVehicleEventConsumer.class).start();
    }
}
