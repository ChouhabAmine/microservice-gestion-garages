package com.renault.garage.domain.ports;

import com.renault.garage.domain.events.VehicleCreatedEvent;
import reactor.core.publisher.Mono;

public interface VehicleEventPublisher {
    Mono<Void> publish(VehicleCreatedEvent event);
}
