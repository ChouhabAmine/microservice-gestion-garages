package com.renault.garage.domain.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class Accessory {
    private String id;

    private String vehicleId;

    @NotBlank(message = "Accessory name must not be blank")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be >= 0")
    private double price;

    @NotNull(message = "Accessory type is required")
    private AccessoryType type;

    public Accessory() { }

    public Accessory(String id, String vehicleId, String name, String description, double price, AccessoryType type) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.type = type;
    }

    // Getters / Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public AccessoryType getType() { return type; }
    public void setType(AccessoryType type) { this.type = type; }
}
