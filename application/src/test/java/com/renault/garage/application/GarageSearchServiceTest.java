package com.renault.garage.application;

import com.renault.garage.application.service.GarageSearchServiceImpl;
import com.renault.garage.domain.model.Accessory;
import com.renault.garage.domain.model.AccessoryType;
import com.renault.garage.domain.model.FuelType;
import com.renault.garage.domain.model.Garage;
import com.renault.garage.domain.model.Vehicle;
import com.renault.garage.domain.ports.AccessoryRepository;
import com.renault.garage.domain.ports.GarageRepository;
import com.renault.garage.domain.ports.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class GarageSearchServiceTest {

    private GarageRepository garageRepository;
    private VehicleRepository vehicleRepo;
    private AccessoryRepository accessoryRepo;
    private GarageSearchServiceImpl service;

    @BeforeEach
    void setUp() {
        garageRepository = Mockito.mock(GarageRepository.class);
        vehicleRepo = Mockito.mock(VehicleRepository.class);
        accessoryRepo = Mockito.mock(AccessoryRepository.class);
        service = new GarageSearchServiceImpl(garageRepository, vehicleRepo, accessoryRepo);
    }

    // ========= searchByVehicleType =========

    @Test
    void searchByVehicleType_shouldReturnDistinctGarages_whenVehiclesHaveValidGarageIds() {
        String type = "SUV";

        // Vehicles (dont un avec garageId vide -> filtré)
        var v1 = new Vehicle("v1", "g1", "Renault", "Austral", 2024, FuelType.HYBRID);
        var v2 = new Vehicle("v2", "g1", "Renault", "Espace", 2023, FuelType.HYBRID); // même garage g1 (dédup)
        var v3 = new Vehicle("v3", "",   "BMW", "X3", 2022, FuelType.DIESEL);          // filtré (garageId vide)
        var v4 = new Vehicle("v4", "g2", "Peugeot", "3008", 2024, FuelType.PETROL);

        Mockito.when(vehicleRepo.findByType(type)).thenReturn(Flux.just(v1, v2, v3, v4));

        // Garages g1 & g2
        var g1 = garage("g1","Garage Renault Nord");
        var g2 = garage("g2","Garage Renault Sud");

        Mockito.when(garageRepository.findById("g1")).thenReturn(Mono.just(g1));
        Mockito.when(garageRepository.findById("g2")).thenReturn(Mono.just(g2));

        var result = service.searchByVehicleType(type)
                .map(Garage::getId)       // on compare sur les IDs pour éviter l’ordre non déterministe de flatMap
                .collectList();

        StepVerifier.create(result)
                .expectNextMatches(list -> list.size() == 2 && list.containsAll(List.of("g1", "g2")))
                .verifyComplete();

        Mockito.verify(vehicleRepo).findByType(type);
        Mockito.verify(garageRepository, Mockito.atLeastOnce()).findById("g1");
        Mockito.verify(garageRepository).findById("g2");

    }

    @Test
    void searchByVehicleType_shouldReturnEmpty_whenNoGarageFoundForVehicles() {
        String type = "SUV";
        var v1 = new Vehicle("v1", "g404", "Brand", "Model", 2020, FuelType.DIESEL);

        Mockito.when(vehicleRepo.findByType(type)).thenReturn(Flux.just(v1));
        Mockito.when(garageRepository.findById("g404")).thenReturn(Mono.empty()); // garage introuvable

        StepVerifier.create(service.searchByVehicleType(type))
                .verifyComplete(); // aucun élément

        Mockito.verify(vehicleRepo).findByType(type);
        Mockito.verify(garageRepository).findById("g404");
    }

    @Test
    void searchByVehicleType_shouldFilterOutNullOrBlankGarageIds() {
        String type = "SUV";
        var withNull = new Vehicle("v1", null,  "Brand", "M1", 2020, FuelType.PETROL);
        var withBlank = new Vehicle("v2", " ",  "Brand", "M2", 2021, FuelType.DIESEL);

        Mockito.when(vehicleRepo.findByType(type)).thenReturn(Flux.just(withNull, withBlank));

        StepVerifier.create(service.searchByVehicleType(type))
                .verifyComplete(); // tous filtrés => vide

        Mockito.verify(vehicleRepo).findByType(type);
        Mockito.verifyNoInteractions(garageRepository);
    }

    // ========= searchByAccessory =========

    @Test
    void searchByAccessory_shouldReturnDistinctGarages_whenAccessoriesLeadToVehiclesAndGarages() {
        String name = "GPS";

        // Accessories (un avec vehicleId vide -> filtré)
        var a1 = new Accessory("a1", "v1", "GPS", "Nav 1", 1200.0, AccessoryType.GPS);
        var a2 = new Accessory("a2", "v2", "GPS", "Nav 2", 900.0,  AccessoryType.GPS);
        var a3 = new Accessory("a3", "",   "GPS", "Bad",   500.0,  AccessoryType.GPS); // filtré

        Mockito.when(accessoryRepo.findByName(name)).thenReturn(Flux.just(a1, a2, a3));

        // Vehicles (un avec garageId vide -> filtré)
        var v1 = new Vehicle("v1", "g1", "Renault", "Clio", 2025, FuelType.PETROL);
        var v2 = new Vehicle("v2", "g2", "Renault", "Megane", 2024, FuelType.DIESEL);
        var vBad = new Vehicle("vBad", "", "X", "Y", 2020, FuelType.DIESEL); // ne sera jamais demandé (car a3 filtré)

        Mockito.when(vehicleRepo.findById("v1")).thenReturn(Mono.just(v1));
        Mockito.when(vehicleRepo.findById("v2")).thenReturn(Mono.just(v2));

        var g1 = garage("g1", "Renault Casa Sud");
        var g2 = garage("g2", "Renault Agadir");

        Mockito.when(garageRepository.findById("g1")).thenReturn(Mono.just(g1));
        Mockito.when(garageRepository.findById("g2")).thenReturn(Mono.just(g2));

        var result = service.searchByAccessory(name)
                .map(Garage::getId)
                .collectList();

        StepVerifier.create(result)
                .expectNextMatches(list -> list.size() == 2 && list.containsAll(List.of("g1", "g2")))
                .verifyComplete();

        Mockito.verify(accessoryRepo).findByName(name);
        Mockito.verify(vehicleRepo).findById("v1");
        Mockito.verify(vehicleRepo).findById("v2");
        Mockito.verify(garageRepository).findById("g1");
        Mockito.verify(garageRepository).findById("g2");
    }

    @Test
    void searchByAccessory_shouldReturnEmpty_whenVehicleNotFoundOrGarageNotFound() {
        String name = "GPS";

        var a1 = new Accessory("a1", "v404", "GPS", "Nav", 1200.0, AccessoryType.GPS);
        Mockito.when(accessoryRepo.findByName(name)).thenReturn(Flux.just(a1));

        // Vehicle introuvable
        Mockito.when(vehicleRepo.findById("v404")).thenReturn(Mono.empty());

        StepVerifier.create(service.searchByAccessory(name))
                .verifyComplete(); // rien

        Mockito.verify(accessoryRepo).findByName(name);
        Mockito.verify(vehicleRepo).findById("v404");
        Mockito.verifyNoInteractions(garageRepository);
    }

    @Test
    void searchByAccessory_shouldFilterOutNullOrBlankVehicleIdsAndGarageIds() {
        String name = "GPS";

        var aNullVeh = new Accessory("a1", null, "GPS", "Bad", 100.0, AccessoryType.GPS);
        var aBlankVeh = new Accessory("a2", " ", "GPS", "Bad", 100.0, AccessoryType.GPS);

        Mockito.when(accessoryRepo.findByName(name)).thenReturn(Flux.just(aNullVeh, aBlankVeh));

        StepVerifier.create(service.searchByAccessory(name))
                .verifyComplete(); // tout filtré -> vide

        Mockito.verify(accessoryRepo).findByName(name);
        Mockito.verifyNoInteractions(vehicleRepo, garageRepository);
    }

    // ========= helpers =========

    private static Garage garage(String id, String name) {
        Garage g = new Garage();
        g.setId(id);
        g.setName(name);
        g.setAddress("Bd Anfa");
        g.setTelephone("+212600000000");
        g.setEmail("chouhab@gmail.com");
        return g;
    }
}
