package com.renault.garage.domain.ports;

import com.renault.garage.domain.model.Garage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GarageRepository {
    Mono<Garage> save(Garage g);
    Mono<Void> deleteById(String id);
    Mono<Garage> findById(String id);
    Flux<Garage> findAll();

}
