package com.renault.garage.application;

import com.renault.garage.domain.model.Garage;
import com.renault.garage.domain.ports.GarageRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IGarageService {


    public Mono<Garage> create(Garage g);
    public Mono<Garage> update(String id, Garage g);
    public Mono<Void> delete(String id);
    public Mono<Garage> get(String id);
    public Flux<Garage> list();
}
