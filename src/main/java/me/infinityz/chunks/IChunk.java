package me.infinityz.chunks;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import me.infinityz.SurvivalUniverse;
import me.infinityz.cities.City;

/**
 * IChunk
 */
public class IChunk {
    public int chunkX, chunkZ;
    public World chunkWorld;
    /* Deprecated */
    public boolean isPrivate, isVIPExclusive;
    /* Potentially deprecated */
    public HirarchyAllowance canBuild[] = { HirarchyAllowance.ADMIN, HirarchyAllowance.HELPER },
            canInteract[] = { HirarchyAllowance.ADMIN, HirarchyAllowance.HELPER };
    public Tristate pvpMode = Tristate.UNKNOWN;
    // All possible types: Owned, Claimable, VIP, Public, PVP, Safe, Shop

    public IChunk(int x, int z, World world) {
        this.chunkX = x;
        this.chunkZ = z;
        this.chunkWorld = world;
        this.isPrivate = false;
        this.isVIPExclusive = false;
    }

    public boolean shouldBuild(UUID uuid) {
        return isAdmin(uuid) || isHelper(uuid);
    }

    public boolean shouldInteract(UUID uuid) {
        return shouldBuild(uuid);
    }

    /* Added some null-safety to these methods. */
    public boolean isAdmin(UUID player) {
        final City city = SurvivalUniverse.instance.cityManager.isInCity(Bukkit.getPlayer(player).getLocation());
        return city != null && city.isOwner(player);
    }

    public boolean isHelper(UUID player) {
        final City city = SurvivalUniverse.instance.cityManager.isInCity(Bukkit.getPlayer(player).getLocation());
        return city != null && city.isHelper(player);
    }

    public boolean isVIP(UUID player) {
        final Player p = Bukkit.getPlayer(player);
        return p != null && p.hasPermission("vip.chunk");
    }
}