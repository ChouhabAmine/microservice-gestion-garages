package com.renault.garage.bootstrap.http;

import com.renault.garage.application.IGarageService;
import com.renault.garage.application.service.GarageServiceImpl;
import com.renault.garage.domain.model.Garage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // 👈 module JavaTime
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class GarageControllerTest {

    private WebTestClient webTestClient;
    private IGarageService garageService;

    @BeforeEach
    void setup() {
        garageService = Mockito.mock(GarageServiceImpl.class);
        var controller = new GarageController(garageService);

        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        webTestClient = WebTestClient.bindToController(controller)
                .httpMessageCodecs(c -> {
                    c.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(mapper));
                    c.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(mapper));
                })
                .configureClient()
                .codecs(c -> {
                    c.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(mapper));
                    c.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(mapper));
                })
                .build();
    }

    @Test
    void get_returns_garage() {
        Mockito.when(garageService.get("g1"))
                .thenReturn(Mono.just(new Garage("g1","G","addr","06","g@ex.com")));

        webTestClient.get().uri("/garages/g1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("g1")
                .jsonPath("$.name").isEqualTo("G");
    }

    @Test
    void list_returns_sorted_page() {
        Mockito.when(garageService.list())
                .thenReturn(Flux.just(
                        new Garage("g1","B","addr","06","b@ex.com"),
                        new Garage("g2","A","addr","06","a@ex.com")
                ));

        webTestClient.get().uri("/garages?sort=name&asc&page=0&size=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("A");
    }
}
