package com.vendingmachine.repository;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("MINERAL_WATER")
public class MineralWater extends Beverage {

    private Double volume;

    protected MineralWater() {}

    public MineralWater(String id, String name, double price, double volume) {
        super(id, name, price);
        this.volume = volume;
    }

    public Double getVolume() { return volume; }

    @Override
    public String getDescription() {
        return getName() + " (" + (volume != null ? volume.intValue() : 0) + "ml)";
    }
}
