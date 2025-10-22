package com.renault.garage.bootstrap.http.unitaire;

import com.renault.garage.application.IVehicleService;
import com.renault.garage.bootstrap.http.VehicleController;
import com.renault.garage.domain.model.FuelType;
import com.renault.garage.domain.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class VehicleControllerTest {

    private IVehicleService service;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(IVehicleService.class);
        var controller = new VehicleController(service);

        // Mode standalone — pas besoin de démarrer tout le contexte Spring
        webTestClient = WebTestClient.bindToController(controller)
                .configureClient()
                .baseUrl("")
                .build();
    }

    // ---------- POST /garages/{garageId}/vehicles ----------
    @Test
    void add_shouldReturn201WithCreatedVehicle() {
        String garageId = "g1";
        var payload = vehicle(null, "Clio", "Renault", "g1");
        var saved   = vehicle("v123", "Clio", "Renault", "g1");

        when(service.addToGarage(eq(garageId), any(Vehicle.class)))
                .thenReturn(Mono.just(saved));

        webTestClient.post()
                .uri("/garages/{garageId}/vehicles", garageId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("v123")
                .jsonPath("$.model").isEqualTo("Clio")
                .jsonPath("$.brand").isEqualTo("Renault");

        verify(service).addToGarage(eq(garageId), any(Vehicle.class));
    }

    @Test
    void add_shouldReturn400WhenServiceErrors() {
        String garageId = "g1";
        var payload = vehicle(null, "Clio", "Renault", "g1");

        when(service.addToGarage(eq(garageId), any(Vehicle.class)))
                .thenReturn(Mono.error(new RuntimeException("bad request")));

        webTestClient.post()
                .uri("/garages/{garageId}/vehicles", garageId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isBadRequest();

        verify(service).addToGarage(eq(garageId), any(Vehicle.class));
    }

    // ---------- GET /garages/{garageId}/vehicles ----------
    @Test
    void listByGarage_shouldReturn200WithResults() {
        String garageId = "g1";
        var v1 = vehicle("v1", "Clio", "Renault", "g1");
        var v2 = vehicle("v2", "Megane", "Renault", "g1");

        when(service.listByGarage(garageId)).thenReturn(Flux.just(v1, v2));

        webTestClient.get()
                .uri("/garages/{garageId}/vehicles", garageId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("v1")
                .jsonPath("$[0].model").isEqualTo("Clio")
                .jsonPath("$[1].id").isEqualTo("v2")
                .jsonPath("$[1].model").isEqualTo("Megane");

        verify(service).listByGarage(garageId);
    }

    @Test
    void listByGarage_shouldReturn200WithEmptyArray() {
        String garageId = "g1";
        when(service.listByGarage(garageId)).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/garages/{garageId}/vehicles", garageId)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("[]");

        verify(service).listByGarage(garageId);
    }

    // ---------- GET /vehicles/{id} ----------
    @Test
    void get_shouldReturn200WithVehicle() {
        String id = "v1";
        var v = vehicle(id, "Clio", "Renault", "g1");
        when(service.get(id)).thenReturn(Mono.just(v));

        webTestClient.get()
                .uri("/vehicles/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("v1")
                .jsonPath("$.model").isEqualTo("Clio");

        verify(service).get(id);
    }

    // (Note : si service.get(...) renvoie Mono.empty(), le contrôleur renvoie 200 avec corps vide.)

    // ---------- PUT /vehicles/{id} ----------
    @Test
    void update_shouldReturn200WithUpdatedVehicle() {
        String id = "v1";
        var payload = vehicle(id, "Clio RS", "Renault", "g1");
        var updated = vehicle(id, "Clio RS", "Renault", "g1");

        when(service.update(eq(id), any(Vehicle.class))).thenReturn(Mono.just(updated));

        webTestClient.put()
                .uri("/vehicles/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("v1")
                .jsonPath("$.model").isEqualTo("Clio RS");

        verify(service).update(eq(id), any(Vehicle.class));
    }

    // ---------- DELETE /vehicles/{id} ----------
    @Test
    void delete_shouldReturn204WhenDeleted() {
        String id = "v1";
        when(service.delete(id)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/vehicles/{id}", id)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(service).delete(id);
    }

    @Test
    void delete_shouldReturn404WhenServiceThrowsIllegalArgument() {
        String id = "v404";
        when(service.delete(id)).thenReturn(Mono.error(new IllegalArgumentException("not found")));

        webTestClient.delete()
                .uri("/vehicles/{id}", id)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().isEmpty();

        verify(service).delete(id);
    }

    // -------- Helpers --------
    private static Vehicle vehicle(String id, String model, String brand, String garageId) {
        var v = new Vehicle();
        v.setId(id);
        v.setModel(model);
        v.setBrand(brand);
        v.setGarageId(garageId);
        v.setFuelType(FuelType.PETROL);
        v.setYear(2024);
        return v;
    }
}
