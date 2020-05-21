package me.infinityz.chunks.types;

import org.bukkit.Bukkit;
import org.bukkit.World;

import me.infinityz.chunks.IChunk;
import me.infinityz.chunks.Tristate;

public class PvPChunk extends IChunk {

    public PvPChunk(int x, int z, World world) {
        super(x, z, world);
        this.pvpMode = Tristate.TRUE;
    }

    public PvPChunk(int x, int z, String world) {
        this(x, z, Bukkit.getWorld(world));
    }
}