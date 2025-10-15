package com.renault.garage.infrastructure.adapter.output;

import com.renault.garage.domain.model.Garage;
import com.renault.garage.domain.ports.GarageRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGarageRepository implements GarageRepository {

    private final Map<String, Garage> data = new ConcurrentHashMap<>();

    @Override
    public Mono<Garage> save(Garage g) {
        if (g.getId() == null) g.setId(UUID.randomUUID().toString());
        data.put(g.getId(), g);
        return Mono.just(g);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        data.remove(id);
        return Mono.empty();
    }

    @Override
    public Mono<Garage> findById(String id) {
        return Mono.justOrEmpty(data.get(id));
    }

    @Override
    public Flux<Garage> findAll() {
        return Flux.fromIterable(data.values());
    }


}
