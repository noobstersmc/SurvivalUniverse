package me.infinityz.chunks.types;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;

import me.infinityz.chunks.HirarchyAllowance;
import me.infinityz.chunks.IChunk;

public class PublicChunk extends IChunk {

    public PublicChunk(int x, int z, World world) {
        super(x, z, world);
        this.canInteract = new HirarchyAllowance[] { HirarchyAllowance.ADMIN, HirarchyAllowance.HELPER,
                HirarchyAllowance.DONOR, HirarchyAllowance.DEFAULT };
    }

    @Override
    public boolean shouldInteract(UUID uuid) {
        return true;
    }

    @Override
    public boolean shouldInteract(UUID uuid, Location loc) {
        return true;
    }

}