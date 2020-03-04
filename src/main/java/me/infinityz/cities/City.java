package me.infinityz.cities;

import java.util.UUID;

/**
 * City
 */
public class City {
    public int centerChunkX, centerChunkZ, chunkRadius;
    public String cityName;
    public UUID[] owners, helpers;

    public City(String name) {
        this.cityName = name;
        // Get the owners and helpers
        // TODO: Calculate the chunks that will belong to the city and maybe store them
        // in RAM?
    }
}