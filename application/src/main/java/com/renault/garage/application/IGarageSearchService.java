package com.renault.garage.application;

import com.renault.garage.domain.model.Garage;
import reactor.core.publisher.Flux;

public interface IGarageSearchService {
    Flux<Garage> searchByVehicleType(String type);
    Flux<Garage> searchByAccessory(String accessoryName);
}
