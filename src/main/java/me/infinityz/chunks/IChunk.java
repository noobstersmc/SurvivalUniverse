package me.infinityz.chunks;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;

import me.infinityz.SurvivalUniverse;

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

    public boolean isAdmin(UUID player) {
        return SurvivalUniverse.instance.cityManager.isInCity(Bukkit.getPlayer(player).getLocation()).isOwner(player);
    }

    public boolean isHelper(UUID player) {
        return SurvivalUniverse.instance.cityManager.isInCity(Bukkit.getPlayer(player).getLocation()).isHelper(player);
    }

    public boolean isVIP(UUID player) {
        return Bukkit.getPlayer(player).hasPermission("vip.chunk");
    }
}