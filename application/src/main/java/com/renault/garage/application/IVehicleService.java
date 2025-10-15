package com.renault.garage.application;

import com.renault.garage.domain.events.VehicleCreatedEvent;
import com.renault.garage.domain.model.FuelType;
import com.renault.garage.domain.model.Vehicle;
import com.renault.garage.domain.ports.VehicleEventPublisher;
import com.renault.garage.domain.ports.VehicleRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IVehicleService {

    public Mono<Vehicle> addToGarage(String garageId, Vehicle v);
    public Mono<Vehicle> update(String id, Vehicle v);
    public Mono<Void> delete(String id);
    public Flux<Vehicle> listByGarage(String garageId);
    public Flux<Vehicle> listByTypeAcrossGarages(String type);
    public Mono<Vehicle> get(String id);
}
