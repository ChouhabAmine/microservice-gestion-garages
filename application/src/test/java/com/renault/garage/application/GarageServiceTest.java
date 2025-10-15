package com.renault.garage.application;

import com.renault.garage.application.service.GarageServiceImpl;
import com.renault.garage.domain.model.Garage;
import com.renault.garage.domain.ports.GarageRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class GarageServiceTest {

    static class FakeGarageRepo implements GarageRepository {
        Map<String, Garage> store = new ConcurrentHashMap<>();
        @Override public Mono<Garage> save(Garage g) {
            if (g.getId()==null) g.setId("g-"+store.size());
            store.put(g.getId(), g); return Mono.just(g);
        }
        @Override public Mono<Void> deleteById(String id) { store.remove(id); return Mono.empty(); }
        @Override public Mono<Garage> findById(String id) { return Mono.justOrEmpty(store.get(id)); }
        @Override public Flux<Garage> findAll() { return Flux.fromIterable(store.values()); }
    }


    @Test
    void create_and_get() {
        var repo = new FakeGarageRepo();
        var service = new GarageServiceImpl(repo);
        var g = new Garage(null, "Garage A", "1 rue X", "0612345678", "a@ex.com");

        StepVerifier.create(service.create(g).flatMap(saved -> service.get(saved.getId())))
                .expectNextMatches(found -> "Garage A".equals(found.getName()))
                .verifyComplete();
    }

    @Test
    void update_replaces_fields() {
        var repo = new FakeGarageRepo();
        var service = new GarageServiceImpl(repo);
        var g = new Garage(null, "G1", "addr", "0600000000", "a@ex.com");

        var updated = new Garage(null, "G2", "addr2", "0600000001", "b@ex.com");

        StepVerifier.create(service.create(g)
                .flatMap(saved -> service.update(saved.getId(), updated))
                .flatMap(saved -> service.get(saved.getId())))
            .expectNextMatches(found -> "G2".equals(found.getName()) && "addr2".equals(found.getAddress()))
            .verifyComplete();
    }
}
