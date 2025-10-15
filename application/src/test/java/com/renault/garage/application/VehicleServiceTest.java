package com.renault.garage.application;

import com.renault.garage.application.service.VehicleServiceImpl;
import com.renault.garage.domain.events.VehicleCreatedEvent;
import com.renault.garage.domain.model.FuelType;
import com.renault.garage.domain.model.Vehicle;
import com.renault.garage.domain.ports.VehicleEventPublisher;
import com.renault.garage.domain.ports.VehicleRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

class VehicleServiceTest {

    static class FakeVehicleRepo implements VehicleRepository {
        Map<String, Vehicle> store = new ConcurrentHashMap<>();
        @Override public Mono<Vehicle> save(Vehicle v) {
            if (v.getId()==null) v.setId(UUID.randomUUID().toString());
            store.put(v.getId(), v); return Mono.just(v);
        }
        @Override public Mono<Void> deleteById(String id) { store.remove(id); return Mono.empty(); }
        @Override public Mono<Vehicle> findById(String id) { return Mono.justOrEmpty(store.get(id)); }
        @Override public Flux<Vehicle> findAllByGarageId(String garageId) {
            return Flux.fromStream(store.values().stream().filter(v -> garageId.equals(v.getGarageId())));
        }
        @Override public Flux<Vehicle> findAllByModelAcrossGarages(String model) {
            return Flux.fromStream(store.values().stream().filter(v -> model.equalsIgnoreCase(v.getModel())));
        }

        @Override
        public Flux<Vehicle> findAllByTypeAcrossGarages(String type) {
            return Flux.fromStream(store.values().stream().filter(v -> type.equalsIgnoreCase(v.getFuelType().toString())));
        }

        @Override public Mono<Long> countByGarageId(String garageId) {
            return Mono.just(store.values().stream().filter(v -> garageId.equals(v.getGarageId())).count());
        }

        @Override
        public Flux<Vehicle> findByType(String type) {
            return Flux.fromStream(store.values().stream().filter(v -> type.equalsIgnoreCase(v.getFuelType().toString())));
        }
    }

    static class CapturingPublisher implements VehicleEventPublisher {
        AtomicReference<VehicleCreatedEvent> last = new AtomicReference<>();
        @Override public Mono<Void> publish(VehicleCreatedEvent event) {
            last.set(event); return Mono.empty();
        }
    }

    @Test
    void add_vehicle_publishes_event_and_sets_garage() {
        var repo = new FakeVehicleRepo();
        var pub = new CapturingPublisher();
        var service = new VehicleServiceImpl(repo, pub);

        var v = new Vehicle(null, null, "Renault", "Clio", 2020, FuelType.PETROL);

        StepVerifier.create(service.addToGarage("g1", v)
                .flatMap(saved -> repo.findById(saved.getId())))
            .expectNextMatches(saved -> "g1".equals(saved.getGarageId()) && "Clio".equals(saved.getModel()))
            .verifyComplete();

        assert pub.last.get() != null;
        assert "Clio".equals(pub.last.get().model());
    }

    @Test
    void capacity_exceeded_throws_error() {
        var repo = new FakeVehicleRepo();
        var pub = new CapturingPublisher();
        var service = new VehicleServiceImpl(repo, pub);

        for (int i=0;i<50;i++) {
            service.addToGarage("g2", new Vehicle(null, null, "Renault", "M"+i, 2023, FuelType.PETROL)).block();
        }
        var extra = new Vehicle(null, null, "Renault", "TooMuch", 2023, FuelType.PETROL);

        StepVerifier.create(service.addToGarage("g2", extra))
            .expectErrorMatches(err -> err instanceof IllegalStateException && err.getMessage().contains("capacity"))
            .verify();
    }
}
