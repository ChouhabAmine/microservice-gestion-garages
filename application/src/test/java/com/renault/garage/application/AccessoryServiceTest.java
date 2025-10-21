package com.renault.garage.application;

import com.renault.garage.application.service.AccessoryServiceImpl;
import com.renault.garage.domain.model.Accessory;
import com.renault.garage.domain.model.AccessoryType;
import com.renault.garage.domain.model.FuelType;
import com.renault.garage.domain.model.Vehicle;
import com.renault.garage.domain.ports.AccessoryRepository;
import com.renault.garage.domain.ports.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class AccessoryServiceTest {

    private VehicleRepository vehicleRepo;
    private AccessoryRepository accessoryRepo;
    private AccessoryServiceImpl service;

    @BeforeEach
    void setUp() {
        vehicleRepo = Mockito.mock(VehicleRepository.class);
        accessoryRepo = Mockito.mock(AccessoryRepository.class);
        service = new AccessoryServiceImpl(vehicleRepo, accessoryRepo);
    }

    @Test
    void addToVehicle_shouldSaveAccessory_whenVehicleExists() {
        // given
        String vehicleId = "v1";
        var accessory = new Accessory("a1", vehicleId, "GPS", "Navigation system", 1500.0, AccessoryType.GPS);

        Mockito.when(vehicleRepo.findById(vehicleId))
                .thenReturn(Mono.just(new Vehicle(vehicleId, "g1", "Renault", "Mégane 4",2025,FuelType.DIESEL )));
        Mockito.when(accessoryRepo.save(vehicleId, accessory))
                .thenReturn(Mono.just(accessory));

        // when
        var result = service.addToVehicle(vehicleId, accessory);

        // then
        StepVerifier.create(result)
                .expectNext(accessory)
                .verifyComplete();

        Mockito.verify(vehicleRepo).findById(vehicleId);
        Mockito.verify(accessoryRepo).save(vehicleId, accessory);
    }


    @Test
    void updateOnVehicle_shouldUpdateAccessory_whenVehicleExists() {
        String vehicleId = "v1";
        String accessoryId = "a1";
        var accessory = new Accessory(null, vehicleId, "Radio", "Audio system", 300.0, AccessoryType.INTERIOR);

        Mockito.when(vehicleRepo.findById(vehicleId))
                .thenReturn(Mono.just(new Vehicle(vehicleId, "g1", "Renault", "Mégane 4",2025,FuelType.DIESEL )));
        Mockito.when(accessoryRepo.save(vehicleId, accessory))
                .thenReturn(Mono.just(accessory));

        var result = service.updateOnVehicle(vehicleId, accessoryId, accessory);

        StepVerifier.create(result)
                .expectNext(accessory)
                .verifyComplete();

        Mockito.verify(vehicleRepo).findById(vehicleId);
        Mockito.verify(accessoryRepo).save(vehicleId, accessory);
    }

    @Test
    void removeFromVehicle_shouldDeleteAccessory_whenVehicleExists() {
        String vehicleId = "v1";
        String accessoryId = "a1";

        Mockito.when(vehicleRepo.findById(vehicleId))
                .thenReturn(Mono.just(new Vehicle(vehicleId, "g1", "Renault", "Mégane 4",2025,FuelType.DIESEL )));
        Mockito.when(accessoryRepo.deleteById(accessoryId)).thenReturn(Mono.empty());

        var result = service.removeFromVehicle(vehicleId, accessoryId);

        StepVerifier.create(result)
                .verifyComplete();

        Mockito.verify(vehicleRepo).findById(vehicleId);
        Mockito.verify(accessoryRepo).deleteById(accessoryId);
    }

    @Test
    void listByVehicle_shouldReturnAccessories_whenVehicleExists() {
        String vehicleId = "v1";
        var a1 = new Accessory("a1", vehicleId, "GPS", "Navigation", 1500.0, AccessoryType.GPS);
        var a2 = new Accessory("a2", vehicleId, "Radio", "Audio", 300.0, AccessoryType.INTERIOR);

        Mockito.when(vehicleRepo.findById(vehicleId))
                .thenReturn(Mono.just(new Vehicle(vehicleId, "g1", "Renault", "Mégane 4",2025,FuelType.DIESEL )));
        Mockito.when(accessoryRepo.findByVehicleId(vehicleId))
                .thenReturn(Flux.just(a1, a2));

        var result = service.listByVehicle(vehicleId);

        StepVerifier.create(result)
                .expectNext(a1, a2)
                .verifyComplete();

        Mockito.verify(vehicleRepo).findById(vehicleId);
        Mockito.verify(accessoryRepo).findByVehicleId(vehicleId);
    }
}
