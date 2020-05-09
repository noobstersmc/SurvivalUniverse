package me.infinityz.chunks.types;

import java.util.UUID;

import org.bukkit.World;

import me.infinityz.chunks.HirarchyAllowance;
import me.infinityz.chunks.IChunk;

public class VipChunk extends IChunk {

    public VipChunk(int x, int z, World world) {
        super(x, z, world);
        this.canInteract = new HirarchyAllowance[] { HirarchyAllowance.ADMIN, HirarchyAllowance.HELPER,
                HirarchyAllowance.DONOR };
    }

    @Override
    public boolean shouldInteract(UUID uuid) {
        return shouldBuild(uuid) || isVIP(uuid);
    }

}