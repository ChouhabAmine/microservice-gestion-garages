package com.renault.garage.domain.ports;

import com.renault.garage.domain.model.Vehicle;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Locale;

public interface VehicleRepository {
    Mono<Vehicle> save(Vehicle v);
    Mono<Void> deleteById(String id);
    Mono<Vehicle> findById(String id);
    Flux<Vehicle> findAllByGarageId(String garageId);
    Flux<Vehicle> findAllByModelAcrossGarages(String model);
    Flux<Vehicle> findAllByTypeAcrossGarages(String type);
    Mono<Long> countByGarageId(String garageId);
    Flux<Vehicle> findByType(String type);
}
