package com.renault.garage.bootstrap.http.unitaire;

import com.renault.garage.application.IGarageSearchService;
import com.renault.garage.bootstrap.http.GarageSearchController;
import com.renault.garage.domain.model.Garage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GarageSearchControllerTest {

    private IGarageSearchService searchService;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        searchService = Mockito.mock(IGarageSearchService.class);
        var controller = new GarageSearchController(searchService);

        webTestClient = WebTestClient.bindToController(controller)
                .configureClient()
                .baseUrl("/garages/search")
                .build();
    }

    // -------- /garages/search/by-vehicle-type --------

    @Test
    void byVehicleType_shouldReturn200WithResults() {
        var g1 = garage("g1", "Renault Casa Sud", "Casa");
        var g2 = garage("g2", "Renault Agadir", "Agadir");

        when(searchService.searchByVehicleType(eq("SUV")))
                .thenReturn(Flux.just(g1, g2));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/by-vehicle-type").queryParam("type", "SUV").build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("g1")
                .jsonPath("$[0].name").isEqualTo("Renault Casa Sud")
                .jsonPath("$[1].id").isEqualTo("g2")
                .jsonPath("$[1].name").isEqualTo("Renault Agadir");

        verify(searchService).searchByVehicleType("SUV");
    }

    @Test
    void byVehicleType_shouldReturn200WithEmptyArrayWhenNoResults() {
        when(searchService.searchByVehicleType(eq("sedan")))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/by-vehicle-type").queryParam("type", "sedan").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("[]");

        verify(searchService).searchByVehicleType("sedan");
    }

    @Test
    void byVehicleType_shouldReturn400WhenParamMissing() {
        webTestClient.get()
                .uri("/by-vehicle-type") // param 'type' manquant
                .exchange()
                .expectStatus().isBadRequest();

        verifyNoInteractions(searchService);
    }

    // -------- /garages/search/by-accessory --------

    @Test
    void byAccessory_shouldReturn200WithResults() {
        var g1 = garage("g1", "Renault Casa Sud", "Casa");
        when(searchService.searchByAccessory(eq("GPS")))
                .thenReturn(Flux.just(g1));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/by-accessory").queryParam("name", "GPS").build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("g1")
                .jsonPath("$[0].name").isEqualTo("Renault Casa Sud");

        verify(searchService).searchByAccessory("GPS");
    }

    @Test
    void byAccessory_shouldReturn200WithEmptyArrayWhenNoResults() {
        when(searchService.searchByAccessory(eq("Camera")))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/by-accessory").queryParam("name", "Camera").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("[]");

        verify(searchService).searchByAccessory("Camera");
    }

    @Test
    void byAccessory_shouldReturn400WhenParamMissing() {
        webTestClient.get()
                .uri("/by-accessory") // param 'name' manquant
                .exchange()
                .expectStatus().isBadRequest();

        verifyNoInteractions(searchService);
    }

    // -------- Helpers --------
    private static Garage garage(String id, String name, String address) {
        var g = new Garage();
        g.setId(id);
        g.setName(name);
        g.setAddress(address);
        return g;
    }
}

