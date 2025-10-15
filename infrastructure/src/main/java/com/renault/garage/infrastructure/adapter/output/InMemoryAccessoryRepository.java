package com.renault.garage.infrastructure.adapter.output;

import com.renault.garage.domain.model.Accessory;
import com.renault.garage.domain.ports.AccessoryRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

@Repository
public class InMemoryAccessoryRepository implements AccessoryRepository {
    private final Map<String, Accessory> store = new ConcurrentHashMap<>();

    @Override
    public Mono<Accessory> findById(String id) {
        return Mono.justOrEmpty(store.get(id));
    }

    @Override
    public Flux<Accessory> findByName(String name) {
        return Flux.fromIterable(
                store.values()
                        .stream()
                        .filter(a -> a.getName() != null && a.getName().equalsIgnoreCase(name))
                        .collect(toList())
        );
    }


    @Override
    public Flux<Accessory> findByVehicleId(String vehicleId) {
        return Flux.fromIterable(store.values()
                .stream()
                .filter(a -> vehicleId.equals(a.getVehicleId()))
                .collect(toList()));
    }

    @Override
    public Mono<Accessory> save(String vehicleId, Accessory accessory) {
        if (accessory.getId() == null || accessory.getId().isBlank()) {
            accessory.setId(java.util.UUID.randomUUID().toString());
        }
        accessory.setVehicleId(vehicleId);
        store.put(accessory.getId(), accessory);
        return Mono.just(accessory);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        store.remove(id);
        return Mono.empty();
    }

}
