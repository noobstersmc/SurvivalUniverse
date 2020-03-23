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
    }

    public boolean isOwner(UUID player) {
        if (owners == null || owners.length == 0)
            return false;
        for (UUID uuid : owners) {
            if (player.getMostSignificantBits() == uuid.getMostSignificantBits())
                return true;
        }
        return false;
    }

    public boolean isOwner(Player player) {
        return isOwner(player.getUniqueId());
    }

    public boolean isHelper(Player player) {
        return isHelper(player.getUniqueId());
    }

    public boolean isHelper(UUID player) {
        if (helpers == null || helpers.length == 0)
            return false;
        for (UUID uuid : helpers)
            if (player.getMostSignificantBits() == uuid.getMostSignificantBits())
                return true;
        return false;
    }
}