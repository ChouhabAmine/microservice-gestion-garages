package com.renault.garage.bootstrap.http;

import com.renault.garage.application.IGarageSearchService;
import com.renault.garage.application.IGarageService;
import com.renault.garage.domain.model.Garage;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import org.springframework.validation.annotation.Validated;

@Validated
@RestController
@RequestMapping(value = "/garages/search", produces = MediaType.APPLICATION_JSON_VALUE)
public class GarageSearchController {

    private final IGarageSearchService service;

    public GarageSearchController(IGarageSearchService service) {
        this.service = service;
    }

    @GetMapping("/by-vehicle-type")
    public Flux<Garage> byVehicleType(@RequestParam("type")
                                          @NotBlank(message = "type est obligatoire")
                                          String type) {

        return service.searchByVehicleType(type);
    }


    @GetMapping("/by-accessory")
    public Flux<Garage> byAccessory(@RequestParam("name")
                                        @NotBlank(message = "accessoire est obligatoire")
                                        String accessoryName) {
        return service.searchByAccessory(accessoryName);
    }
}