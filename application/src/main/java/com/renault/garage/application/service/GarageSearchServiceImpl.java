package com.renault.garage.application.service;

import com.renault.garage.application.IGarageSearchService;
import com.renault.garage.domain.model.Garage;
import com.renault.garage.domain.ports.AccessoryRepository;
import com.renault.garage.domain.ports.GarageRepository;
import com.renault.garage.domain.ports.VehicleRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class GarageSearchServiceImpl implements IGarageSearchService {
    private final GarageRepository garageRepository;
    private final VehicleRepository vehicleRepo;
    private final AccessoryRepository accessoryRepo;

    public GarageSearchServiceImpl(GarageRepository garageRepository, VehicleRepository vehicleRepo, AccessoryRepository accessoryRepo) {
        this.garageRepository = garageRepository;
        this.vehicleRepo = vehicleRepo;
        this.accessoryRepo = accessoryRepo;
    }

    @Override
    public Flux<Garage> searchByVehicleType(String type) {

        return vehicleRepo.findByType(type)
                .filter(v -> v.getGarageId() != null && !v.getGarageId().isBlank())
                .flatMap(v -> garageRepository.findById(v.getGarageId()))
                .distinct(Garage::getId);
    }

    @Override
    public Flux<Garage> searchByAccessory(String accessoryName) {

        return accessoryRepo.findByName(accessoryName)
                .filter(a -> a.getVehicleId() != null && !a.getVehicleId().isBlank())
                .flatMap(a -> vehicleRepo.findById(a.getVehicleId()))
                .filter(v -> v.getGarageId() != null && !v.getGarageId().isBlank())
                .flatMap(v -> garageRepository.findById(v.getGarageId()))
                .distinct(Garage::getId);
    }

}
