package me.infinityz.chunks.types;

import org.bukkit.World;

import me.infinityz.chunks.IChunk;
import me.infinityz.chunks.Tristate;

public class SafeChunk extends IChunk {

    public SafeChunk(int x, int z, World world) {
        super(x, z, world);
        this.pvpMode = Tristate.FALSE;
    }

}