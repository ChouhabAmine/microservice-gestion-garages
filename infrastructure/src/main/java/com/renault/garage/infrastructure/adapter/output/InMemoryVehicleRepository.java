package com.renault.garage.infrastructure.adapter.output;

import com.renault.garage.domain.model.Vehicle;
import com.renault.garage.domain.ports.VehicleRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryVehicleRepository implements VehicleRepository {
    private final Map<String, Vehicle> data = new ConcurrentHashMap<>();
    @Override public Mono<Vehicle> save(Vehicle v){
        if (v.getId()==null) v.setId(java.util.UUID.randomUUID().toString());
        data.put(v.getId(), v);
        return Mono.just(v);
    }
    @Override public Mono<Void> deleteById(String id){ data.remove(id); return Mono.empty(); }
    @Override public Mono<Vehicle> findById(String id){ return Mono.justOrEmpty(data.get(id)); }
    @Override public Flux<Vehicle> findAllByGarageId(String garageId){
        return Flux.fromStream(data.values().stream().filter(v -> java.util.Objects.equals(v.getGarageId(), garageId)));
    }
    @Override public Flux<Vehicle> findAllByTypeAcrossGarages(String type){
        return Flux.fromStream(data.values().stream().filter(v -> v.getFuelType()!=null && v.getFuelType().toString().equalsIgnoreCase(type)));
    }
    @Override public Flux<Vehicle> findAllByModelAcrossGarages(String model){
        return Flux.fromStream(data.values().stream().filter(v -> v.getFuelType()!=null && v.getModel().equalsIgnoreCase(model)));
    }
    @Override public Mono<Long> countByGarageId(String garageId){
        long c = data.values().stream().filter(v -> Objects.equals(v.getGarageId(), garageId)).count();
        return Mono.just(c);
    }
    @Override
    public Flux<Vehicle> findByType(String type) {
        return Flux.fromIterable(data.values())
                .filter(v -> v.getFuelType() != null && v.getFuelType().toString().equalsIgnoreCase(type));
    }
}
