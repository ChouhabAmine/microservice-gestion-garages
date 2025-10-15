package com.renault.garage.application;

import com.renault.garage.domain.model.Accessory;
import com.renault.garage.domain.ports.AccessoryRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IAccessoryService {

    Mono<Accessory> addToVehicle(String vehicleId, Accessory accessory);
    Mono<Accessory> updateOnVehicle(String vehicleId, String accessoryId, Accessory accessory);
    Mono<Void> removeFromVehicle(String vehicleId, String accessoryId);
    Flux<Accessory> listByVehicle(String vehicleId);
}
