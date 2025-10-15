package com.renault.garage.application.service;

import com.renault.garage.application.IGarageService;
import com.renault.garage.domain.model.Garage;
import com.renault.garage.domain.ports.GarageRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@Service
public class GarageServiceImpl implements IGarageService {
    private final GarageRepository garageRepository;


    public GarageServiceImpl(GarageRepository garageRepository) { this.garageRepository = garageRepository; }

    public Mono<Garage> create(Garage g){ return garageRepository.save(g); }
    public Mono<Garage> update(String id, Garage g){
        return garageRepository.findById(id).flatMap(existing -> { g.setId(id); return garageRepository.save(g); });
    }
    public Mono<Void> delete(String id){ return garageRepository.deleteById(id); }
    public Mono<Garage> get(String id){ return garageRepository.findById(id); }
    public Flux<Garage> list(){ return garageRepository.findAll(); }

}
