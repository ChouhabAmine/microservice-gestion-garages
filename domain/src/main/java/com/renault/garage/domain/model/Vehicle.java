package com.renault.garage.domain.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class Vehicle {
    private String id;

    private String garageId;

    @NotBlank
    @Size(max = 100)
    private String brand;

    @NotBlank
    @Size(max = 100)
    private String model;

    @Min(1886) @Max(2100)
    private int year;

    @NotNull
    private FuelType fuelType;

    public Vehicle() { }

    public Vehicle(String id, String garageId, String brand, String model, int year, FuelType fuelType) {
        this.id = id;
        this.garageId = garageId;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.fuelType = fuelType;
    }

    // Getters / Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getGarageId() { return garageId; }
    public void setGarageId(String garageId) { this.garageId = garageId; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public FuelType getFuelType() { return fuelType; }
    public void setFuelType(FuelType fuelType) { this.fuelType = fuelType; }
}
