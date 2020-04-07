package me.infinityz.cities;

import java.util.Arrays;
import java.util.Objects;
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

    public boolean isOwner(Player player) {
        return isOwner(player.getUniqueId());
    }

    public boolean isHelper(Player player) {
        return isHelper(player.getUniqueId());
    }

    public boolean isHelper(UUID uuid) {
        return Objects.nonNull(uuid) ? Arrays.asList(helpers).stream().filter(Objects::nonNull)
                .filter(it -> uuid.getMostSignificantBits() == it.getMostSignificantBits()).findFirst().isPresent()
                : false;
    }
    public boolean isOwner(UUID uuid) {
        return Objects.nonNull(uuid) ? Arrays.asList(owners).stream().filter(Objects::nonNull)
                .filter(it -> uuid.getMostSignificantBits() == it.getMostSignificantBits()).findFirst().isPresent()
                : false;
    }
    
    public boolean isHelper(UUID player, boolean use_legacy) {
        if (helpers == null || helpers.length == 0)
            return false;
        for (UUID uuid : helpers)
            if (player.getMostSignificantBits() == uuid.getMostSignificantBits())
                return true;
        return false;
    }
    public boolean isOwner(UUID player, boolean use_legacy) {
        if (owners == null || owners.length == 0)
            return false;
        for (UUID uuid : owners) {
            if (player.getMostSignificantBits() == uuid.getMostSignificantBits())
                return true;
        }
        return false;
    }
}