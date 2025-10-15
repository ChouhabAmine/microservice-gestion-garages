package com.renault.garage.bootstrap.http;

import com.renault.garage.application.IVehicleService;
import com.renault.garage.domain.model.Vehicle;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import jakarta.validation.constraints.NotBlank;

@Validated
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class VehicleController {

    private final IVehicleService service;

    public VehicleController(IVehicleService service) {
        this.service = service;
    }

    @PostMapping(
            value = "/garages/{garageId}/vehicles",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<Vehicle>> add(
            @PathVariable
            @NotBlank(message = "garageId est obligatoire")
            String garageId,
            @Valid @RequestBody Vehicle v) {

        return service.addToGarage(garageId, v)
                .map(vehicle -> ResponseEntity.status(HttpStatus.CREATED).body(vehicle))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build()));
    }


    @GetMapping("/garages/{garageId}/vehicles")
    public Flux<Vehicle> listByGarage(@PathVariable
            @NotBlank(message = "garageId est obligatoire")
            String garageId) {
        return service.listByGarage(garageId);
    }

    @GetMapping("/vehicles/{id}")
    public Mono<Vehicle> get(@PathVariable
            @NotBlank(message = "id est obligatoire")
            String id) {
        return service.get(id);
    }

    @PutMapping(
            value = "/vehicles/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<Vehicle> update(
            @PathVariable
            @NotBlank(message = "id est obligatoire")
            String id,
            @RequestBody  @Valid Vehicle v) {
        return service.update(id, v);
    }

    @DeleteMapping("/vehicles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Object>> delete(@PathVariable
            @NotBlank(message = "id est obligatoire")
             String id) {
        return service.delete(id)
                .then(Mono.just(ResponseEntity.noContent().build()))
                .onErrorResume(IllegalArgumentException.class,
                        e -> Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()));
    }
}