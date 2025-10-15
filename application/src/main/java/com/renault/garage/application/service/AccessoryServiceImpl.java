package com.renault.garage.application.service;

import com.renault.garage.application.IAccessoryService;
import com.renault.garage.domain.model.Accessory;
import com.renault.garage.domain.model.Vehicle;
import com.renault.garage.domain.ports.AccessoryRepository;
import com.renault.garage.domain.ports.VehicleRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AccessoryServiceImpl implements IAccessoryService {

    private final VehicleRepository vehicleRepo;
    private final AccessoryRepository accessoryRepo;

    public AccessoryServiceImpl(VehicleRepository vehicleRepo, AccessoryRepository accessoryRepo) {
        this.vehicleRepo = vehicleRepo;
        this.accessoryRepo = accessoryRepo;
    }

    @Override
    public Mono<Accessory> addToVehicle(String vehicleId, Accessory accessory) {
        return ensureVehicleExists(vehicleId)
                .then(accessoryRepo.save(vehicleId, accessory));
    }

    @Override
    public Mono<Accessory> updateOnVehicle(String vehicleId, String accessoryId, Accessory accessory) {
        accessory.setId(accessoryId);
        return ensureVehicleExists(vehicleId)
                .then(accessoryRepo.save(vehicleId, accessory));
    }

    @Override
    public Mono<Void> removeFromVehicle(String vehicleId, String accessoryId) {
        return ensureVehicleExists(vehicleId)
                .then(accessoryRepo.deleteById(accessoryId));
    }

    @Override
    public Flux<Accessory> listByVehicle(String vehicleId) {
        return ensureVehicleExists(vehicleId).thenMany(accessoryRepo.findByVehicleId(vehicleId));
    }

    private Mono<Vehicle> ensureVehicleExists(String vehicleId) {
        return vehicleRepo.findById(vehicleId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Vehicle not found: " + vehicleId)));
    }
}
