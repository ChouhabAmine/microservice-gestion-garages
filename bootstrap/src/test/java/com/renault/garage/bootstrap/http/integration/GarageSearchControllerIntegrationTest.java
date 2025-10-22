package com.renault.garage.bootstrap.http.integration;

import com.renault.garage.infrastructure.events.KafkaVehicleEventConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class GarageSearchControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    // Evite que Kafka fasse échouer le démarrage en test
    @MockBean
    private KafkaVehicleEventConsumer kafkaVehicleEventConsumer;

    @Test
    void shouldCreateGarageVehicleAccessory_thenSearchByAccessoryAndType() {
        var garageIdRef = new AtomicReference<String>();
        var vehicleIdRef = new AtomicReference<String>();
        var accessoryIdRef = new AtomicReference<String>();

        // 1) Créer un garage
        webTestClient.post()
                .uri("/garages")
                .bodyValue(Map.of(
                        "name", "Renault Casa Sud",
                        "address", "Boulevard Mohammed V, Casablanca",
                        "telephone", "0611223375",
                        "email", "contact@renault-casa.ma"
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.id").value(id -> garageIdRef.set(String.valueOf(id)))
                .jsonPath("$.name").isEqualTo("Renault Casa Sud");

        String garageId = garageIdRef.get();
        assertNotNull(garageId);
        assertFalse(garageId.isBlank());

        // 2) Ajouter un véhicule au garage
        webTestClient.post()
                .uri("/garages/{garageId}/vehicles", garageId)
                .bodyValue(Map.of(
                        "brand", "Renault",
                        "model", "Megane",
                        "type", "SUV",
                        "year", 2023,
                        "mileage", 15000,
                        "status", "AVAILABLE",
                        "fuelType","PETROL"

                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.id").value(id -> vehicleIdRef.set(String.valueOf(id)));

        String vehicleId = vehicleIdRef.get();
        assertNotNull(vehicleId);
        assertFalse(vehicleId.isBlank());

        // 3) Ajouter un accessoire au véhicule
        webTestClient.post()
                .uri("/vehicles/{vehicleId}/accessories", vehicleId)
                .bodyValue(Map.of(
                        "name", "GPS",
                        "description", "Système de navigation intégré",
                        "type","GPS"
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.id").value(id -> accessoryIdRef.set(String.valueOf(id)));

        String accessoryId = accessoryIdRef.get();
        assertNotNull(accessoryId);
        assertFalse(accessoryId.isBlank());

        // 4) Recherche par accessoire
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/garages/search/by-accessory")
                        .queryParam("name", "GPS")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(garageId)
                .jsonPath("$[0].name").isEqualTo("Renault Casa Sud");

        // 5) Recherche par type de véhicule
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/garages/search/by-vehicle-type")
                        .queryParam("type", "PETROL")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(garageId)
                .jsonPath("$[0].name").isEqualTo("Renault Casa Sud");
    }

    @Test
    void shouldReturn400WhenMissingQueryParams() {
        // by-accessory sans "name"
        webTestClient.get()
                .uri("/garages/search/by-accessory")
                .exchange()
                .expectStatus().isBadRequest();

        // by-vehicle-type sans "type"
        webTestClient.get()
                .uri("/garages/search/by-vehicle-type")
                .exchange()
                .expectStatus().isBadRequest();
    }
}
