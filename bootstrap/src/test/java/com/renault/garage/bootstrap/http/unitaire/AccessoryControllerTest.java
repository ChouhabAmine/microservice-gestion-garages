package com.renault.garage.bootstrap.http.unitaire;

import com.renault.garage.application.IAccessoryService;
import com.renault.garage.bootstrap.http.AccessoryController;
import com.renault.garage.domain.model.Accessory;
import com.renault.garage.domain.model.AccessoryType;
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
import static org.mockito.Mockito.when;

class AccessoryControllerTest {

    private IAccessoryService accessoryService;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        accessoryService = Mockito.mock(IAccessoryService.class);
        var controller = new AccessoryController(accessoryService);

        // WebTestClient en « standalone » (pas besoin de démarrer tout Spring)
        webTestClient = WebTestClient.bindToController(controller)
                .configureClient()
                .baseUrl("/vehicles") // on préfixe la base une seule fois
                .build();
    }

    // ---------- GET /vehicles/{vehicleId}/accessories ----------
    @Test
    void listAccessories_shouldReturn200WithList() {
        String vehicleId = "v1";
        var a1 = accessory("a1", "GPS", vehicleId);
        var a2 = accessory("a2", "Camera", vehicleId);

        when(accessoryService.listByVehicle(vehicleId))
                .thenReturn(Flux.just(a1, a2));

        webTestClient.get()
                .uri("/{vehicleId}/accessories", vehicleId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("a1")
                .jsonPath("$[0].name").isEqualTo("GPS")
                .jsonPath("$[1].id").isEqualTo("a2")
                .jsonPath("$[1].name").isEqualTo("Camera");
    }

    // ---------- POST /vehicles/{vehicleId}/accessories ----------
    @Test
    void addAccessory_shouldReturn201WithCreatedAccessory() {
        String vehicleId = "v1";
        var payload = accessory(null, "GPS", vehicleId);
        var saved = accessory("a123", "GPS", vehicleId);

        when(accessoryService.addToVehicle(eq(vehicleId), any(Accessory.class)))
                .thenReturn(Mono.just(saved));

        webTestClient.post()
                .uri("/{vehicleId}/accessories", vehicleId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("a123")
                .jsonPath("$.name").isEqualTo("GPS");
    }

    // ---------- PUT /vehicles/{vehicleId}/accessories/{accessoryId} ----------
    @Test
    void updateAccessory_shouldReturn200WhenUpdated() {
        String vehicleId = "v1";
        String accessoryId = "a1";
        var payload = accessory(accessoryId, "GPS-Pro", vehicleId);
        var updated = accessory(accessoryId, "GPS-Pro", vehicleId);

        when(accessoryService.updateOnVehicle(eq(vehicleId), eq(accessoryId), any(Accessory.class)))
                .thenReturn(Mono.just(updated));

        webTestClient.put()
                .uri("/{vehicleId}/accessories/{accessoryId}", vehicleId, accessoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("a1")
                .jsonPath("$.name").isEqualTo("GPS-Pro");
    }

    @Test
    void updateAccessory_shouldReturn404WhenServiceErrors() {
        String vehicleId = "v1";
        String accessoryId = "a404";
        var payload = accessory(accessoryId, "X", vehicleId);

        // Le contrôleur mappe TOUTE erreur à 404 via onErrorResume(...)
        when(accessoryService.updateOnVehicle(eq(vehicleId), eq(accessoryId), any(Accessory.class)))
                .thenReturn(Mono.error(new RuntimeException("not found")));

        webTestClient.put()
                .uri("/{vehicleId}/accessories/{accessoryId}", vehicleId, accessoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().isEmpty();
    }

    // ---------- DELETE /vehicles/{vehicleId}/accessories/{accessoryId} ----------
    @Test
    void deleteAccessory_shouldReturn204WhenDeleted() {
        String vehicleId = "v1";
        String accessoryId = "a1";

        when(accessoryService.removeFromVehicle(vehicleId, accessoryId))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/{vehicleId}/accessories/{accessoryId}", vehicleId, accessoryId)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    void deleteAccessory_shouldReturn404WhenNotFound() {
        String vehicleId = "v1";
        String accessoryId = "a404";

        // Le contrôleur mappe IllegalArgumentException -> 404
        when(accessoryService.removeFromVehicle(vehicleId, accessoryId))
                .thenReturn(Mono.error(new IllegalArgumentException("not found")));

        webTestClient.delete()
                .uri("/{vehicleId}/accessories/{accessoryId}", vehicleId, accessoryId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().isEmpty();
    }

    // -------- Helpers --------
    private static Accessory accessory(String id, String name, String vehicleId) {
        var a = new Accessory();
        a.setId(id);
        a.setName(name);
        a.setVehicleId(vehicleId);
        a.setType(AccessoryType.AUDIO);
        a.setDescription("Audio");

        return a;
    }
}
