package com.renault.garage.domain.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

/**
 * POJO Garage avec contraintes Jakarta.
 */
public class Garage {
    private String id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 255)
    private String address;

    // Regex Java: bien doubler les antislashs
    @Pattern(regexp = "^[+\\d][\\d\\s\\-().]{6,20}$", message = "Invalid telephone format")
    private String telephone;

    @Email
    private String email;

    private Map<DayOfWeek, List<OpeningTime>> horairesOuverture;

    // liste des véhicules disponibles dans ce garage
    private List<Vehicle> vehicles;

    public Garage() {}

    public Garage(String id, String name, String address, String telephone, String email,
                  Map<DayOfWeek, List<OpeningTime>> horairesOuverture, List<Vehicle> vehicles) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.telephone = telephone;
        this.email = email;
        this.horairesOuverture = horairesOuverture;
        this.vehicles = vehicles;
    }

    public Garage(String id, String name, String address, String telephone, String email) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.telephone = telephone;
        this.email = email;
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Map<DayOfWeek, List<OpeningTime>> getHorairesOuverture() { return horairesOuverture; }
    public void setHorairesOuverture(Map<DayOfWeek, List<OpeningTime>> horairesOuverture) {
        this.horairesOuverture = horairesOuverture;
    }

    public List<Vehicle> getVehicles() { return vehicles; }
    public void setVehicles(List<Vehicle> vehicles) { this.vehicles = vehicles; }
}