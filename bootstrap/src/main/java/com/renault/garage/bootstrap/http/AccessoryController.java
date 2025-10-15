package com.renault.garage.bootstrap.http;

import com.renault.garage.application.IAccessoryService;
import com.renault.garage.domain.model.Accessory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;


@Validated
@RestController
@RequestMapping("/vehicles/{vehicleId}/accessories")
public class AccessoryController {

    private final IAccessoryService accessoryService;

    public AccessoryController(IAccessoryService accessoryService) {
        this.accessoryService = accessoryService;
    }


    @GetMapping
    public Flux<Accessory> listAccessories(@PathVariable
            @NotBlank(message = "vehicleId est obligatoire")
            String vehicleId) {
        return accessoryService.listByVehicle(vehicleId);
    }


    @PostMapping
    public Mono<ResponseEntity<Accessory>> addAccessory(
            @PathVariable("vehicleId")
            @NotBlank(message = "vehicleId est obligatoire")
            String vehicleId,
            @RequestBody @Valid Accessory accessory) {

        return accessoryService.addToVehicle(vehicleId, accessory)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    @PutMapping("/{accessoryId}")
    public Mono<ResponseEntity<Accessory>> updateAccessory(
            @PathVariable
            @NotBlank(message = "vehicleId est obligatoire")
             String vehicleId,
            @PathVariable
            @NotBlank(message = "accessoryId est obligatoire")
            String accessoryId,
            @RequestBody @Valid Accessory accessory) {
        return accessoryService.updateOnVehicle(vehicleId, accessoryId, accessory)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }


    @DeleteMapping("/{accessoryId}")
    public Mono<ResponseEntity<Object>> deleteAccessory(
            @PathVariable
            @NotBlank(message = "vehicleId est obligatoire")
            String vehicleId,
            @PathVariable
            @NotBlank(message = "accessoryId est obligatoire")
            String accessoryId) {

        return accessoryService.removeFromVehicle(vehicleId, accessoryId)
                .thenReturn(ResponseEntity.noContent().build())
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity.<Void>status(HttpStatus.NOT_FOUND).build()));
    }

}
