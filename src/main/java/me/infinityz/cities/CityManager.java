package me.infinityz.cities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import me.infinityz.SurvivalUniverse;

/**
 * CityManager
 */
public class CityManager {
    SurvivalUniverse instance;
    public List<City> cities;
    public Map<UUID, City> lastKnownCityMap;

    public CityManager(SurvivalUniverse instance) {
        this.instance = instance;
        cities = new ArrayList<>();
        lastKnownCityMap = new HashMap<>();
    }

    public City isInCity(Location loc) {
        return cities.stream().filter(it -> isInRectangle(loc, it)).findFirst().orElse(null);
    }

    public boolean isInCircle(final Location loc, final City city) {
        return new Location(Bukkit.getWorld(city.world), city.centerX, loc.getY(), city.centerZ)
                .distance(loc) <= city.radius;
    }

    public boolean isInRectangle(final Location loc, final City city) {
        final int x1 = city.centerX - city.radius;
        final int z1 = city.centerZ - city.radius;
        final int x2 = city.centerX + city.radius;
        final int z2 = city.centerZ + city.radius;
        final ZoneVector current = new ZoneVector(loc.getBlockX(), loc.getBlockZ());
        final ZoneVector min = new ZoneVector(Math.min(x1, x2), Math.min(z1, z2));
        final ZoneVector max = new ZoneVector(Math.max(x1, x2), Math.max(z1, z2));
        return loc.getWorld().getName().equalsIgnoreCase(city.world) && current.isInAABB(min, max);
    }
}