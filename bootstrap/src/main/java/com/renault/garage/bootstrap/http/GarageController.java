package com.renault.garage.bootstrap.http;

import com.renault.garage.application.IGarageService;
import com.renault.garage.domain.model.Garage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Validated
@RestController
@RequestMapping("/garages")
public class GarageController {

    private final IGarageService service;

    public GarageController(IGarageService service) { this.service = service; }

    @PostMapping
    public Mono<ResponseEntity<Garage>> create(@RequestBody @Valid Garage g) {
        return service.create(g)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Garage>> get(@PathVariable
                                            @NotBlank(message = "id est obligatoire")
                                            String id) {
        return service.get(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @PatchMapping(value="/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Garage> update(@PathVariable
                                @NotBlank(message = "id est obligatoire")
                               String id,
                               @RequestBody @Valid Garage g){
        return service.update(id, g);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Object>> delete(@PathVariable
                                             @NotBlank(message = "id est obligatoire")
                                             String id) {
        return service.delete(id)
                .then(Mono.just(ResponseEntity.noContent().build()))
                .onErrorResume(IllegalArgumentException.class,
                        e -> Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Garage> list(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "20") int size,
        @RequestParam(name = "sort", defaultValue = "name,asc") String sortParam) {
    String[] parts = sortParam.split(",", 2);
    String sort = parts[0] == null || parts[0].isBlank() ? "name" : parts[0].trim();
    String dir  = parts.length > 1 && parts[1] != null && !parts[1].isBlank() ? parts[1].trim() : "asc";

    Comparator<Garage> cmp = switch (sort.toLowerCase()) {
        case "id" -> Comparator.comparing(Garage::getId);
        case "name" -> Comparator.comparing(Garage::getName, String.CASE_INSENSITIVE_ORDER);
        default -> Comparator.comparing(Garage::getName, String.CASE_INSENSITIVE_ORDER);
    };
    if ("desc".equalsIgnoreCase(dir)) cmp = cmp.reversed();

    return service.list()
            .sort(cmp)
            .skip((long) page * size)
            .take(size);
}
}