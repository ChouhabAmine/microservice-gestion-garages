package com.renault.garage.bootstrap.http.integration;

import com.renault.garage.application.IGarageService;
import com.renault.garage.domain.model.Garage;
import com.renault.garage.infrastructure.events.KafkaVehicleEventConsumer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class GarageControllerIntegrationTest {

    @Autowired
    WebTestClient webTestClient;

    // Mock du service (le contrôleur parle à ce bean — pas d'appel HTTP externe)
    @MockBean
    IGarageService garageService;

    // Optionnel : si ton contexte déclare un consumer Kafka, on l'empêche de démarrer en test
    @MockBean
    KafkaVehicleEventConsumer kafkaVehicleEventConsumer;

    private Garage g(String id, String name) {
        Garage g = new Garage();
        g.setId(id);
        g.setName(name);
        g.setAddress("Addr: Boulevarad Anfa " );
        g.setTelephone("0600000001" );
        g.setEmail("amine@gmail.com");
        return g;
    }

    @Test
    void create_shouldReturn201_andBody() {
        Garage input = g("g1", "Renault Casa");
        Garage saved = g("g1", "Renault Casa");

        Mockito.when(garageService.create(any())).thenReturn(Mono.just(saved));

        webTestClient.post()
                .uri("/garages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(input)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("g1")
                .jsonPath("$.name").isEqualTo("Renault Casa");
    }

    @Test
    void get_shouldReturn200_whenFound() {
        Mockito.when(garageService.get("g1")).thenReturn(Mono.just(g("g1", "Alpha")));

        webTestClient.get()
                .uri("/garages/{id}", "g1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("g1")
                .jsonPath("$.name").isEqualTo("Alpha");
    }

    @Test
    void get_shouldReturn404_whenNotFound() {
        Mockito.when(garageService.get("missing")).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/garages/{id}", "missing")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void update_shouldReturn200_andBody() {
        Garage patch = g("g1", "New Name");
        Garage updated = g("g1", "New Name");

        Mockito.when(garageService.update(eq("g1"), any())).thenReturn(Mono.just(updated));

        webTestClient.patch()
                .uri("/garages/{id}", "g1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(patch)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("g1")
                .jsonPath("$.name").isEqualTo("New Name");
    }

    @Test
    void delete_shouldReturn204_whenDeleted() {
        Mockito.when(garageService.delete("g1")).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/garages/{id}", "g1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void delete_shouldReturn404_whenServiceThrowsIllegalArgumentException() {
        Mockito.when(garageService.delete("missing"))
                .thenReturn(Mono.error(new IllegalArgumentException("not found")));

        webTestClient.delete()
                .uri("/garages/{id}", "missing")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void list_shouldSortAndPaginate() {
        // Jeu de données non trié (le contrôleur trie par name,asc par défaut)
        Garage g1 = g("g1", "Renault Casa Nord");
        Garage g2 = g("g2", "Renault Casa Sud");
        Garage g3 = g("g3", "Renault Casa Centre Ville");
        Mockito.when(garageService.list()).thenReturn(Flux.just(g1, g2, g3));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/garages")
                        .queryParam("page", 0)
                        .queryParam("size", 2)
                        .queryParam("sort", "name,asc")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("Renault Casa Centre Ville")
                .jsonPath("$[1].name").isEqualTo("Renault Casa Nord");
    }
}

