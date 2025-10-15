package com.renault.garage.domain.ports;

import com.renault.garage.domain.model.Accessory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccessoryRepository {
    Mono<Accessory> findById(String id);
    Flux<Accessory> findByName(String name);
    Flux<Accessory> findByVehicleId(String vehicleId);
    Mono<Accessory> save(String vehicleId, Accessory accessory);
    Mono<Void> deleteById(String id);
}
