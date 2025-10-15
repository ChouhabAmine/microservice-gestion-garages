package com.renault.garage.application.service;

import com.renault.garage.application.IVehicleService;
import com.renault.garage.domain.events.VehicleCreatedEvent;
import com.renault.garage.domain.model.FuelType;
import com.renault.garage.domain.model.Vehicle;
import com.renault.garage.domain.ports.VehicleEventPublisher;
import com.renault.garage.domain.ports.VehicleRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class VehicleServiceImpl implements IVehicleService {
    private static final long MAX_VEHICLES_PER_GARAGE = 50L;

    private final VehicleRepository vehicleRepository;
    private final VehicleEventPublisher eventPublisher;

    public VehicleServiceImpl(VehicleRepository vehicleRepository, VehicleEventPublisher eventPublisher) {
        this.vehicleRepository = vehicleRepository;
        this.eventPublisher = eventPublisher;
    }

    public Mono<Vehicle> addToGarage(String garageId, Vehicle v){
        v.setGarageId(garageId);
        return vehicleRepository.countByGarageId(garageId)
                .flatMap(count -> {
                    if (count >= MAX_VEHICLES_PER_GARAGE) {
                        return Mono.error(new IllegalStateException("Garage capacity exceeded (50)."));
                    }
                    return vehicleRepository.save(v);
                })
                .flatMap(saved -> eventPublisher.publish(new VehicleCreatedEvent(
                        saved.getId(), saved.getGarageId(), saved.getModel(), saved.getBrand())).thenReturn(saved));
    }

    public Mono<Vehicle> update(String id, Vehicle v){
        return vehicleRepository.findById(id).flatMap(existing -> {
            v.setId(id); v.setGarageId(existing.getGarageId());
            return vehicleRepository.save(v);
        });
    }

    public Mono<Void> delete(String id){ return vehicleRepository.deleteById(id); }
    public Flux<Vehicle> listByGarage(String garageId){ return vehicleRepository.findAllByGarageId(garageId); }
    public Flux<Vehicle> listByTypeAcrossGarages(String model){ return vehicleRepository.findAllByModelAcrossGarages(model); }
    public Mono<Vehicle> get(String id){ return vehicleRepository.findById(id); }
}
