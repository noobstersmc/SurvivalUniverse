package me.infinityz.cities;

import java.util.UUID;

import org.bukkit.entity.Player;

/**
 * City
 */
public class City {
    public int centerX, centerZ, radius;
    public String cityName, world;
    public UUID[] owners, helpers;

    public City(String name, String world, int centerX, int centerZ, int radius) {
        this.cityName = name;
        this.world = world;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.radius = radius;
        // Get the owners and helpers
        // TODO: Calculate the chunks that will belong to the city and maybe store them
        // in RAM?
    }

    public boolean isOwner(Player player) {
        if (owners == null || owners.length == 0)
            return false;
        for (UUID uuid : owners) {
            if (player.getUniqueId().getMostSignificantBits() == uuid.getMostSignificantBits())
                return true;
        }
        return false;
    }

    public boolean isHelper(Player player) {
        if (helpers == null || helpers.length == 0)
            return false;
        for (UUID uuid : helpers)
            if (player.getUniqueId().getMostSignificantBits() == uuid.getMostSignificantBits())
                return true;
        return false;
    }
}