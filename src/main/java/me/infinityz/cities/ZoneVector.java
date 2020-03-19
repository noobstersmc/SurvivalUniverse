package me.infinityz.cities;

/**
 * ZoneVector
 */
public class ZoneVector {
    public int x, z;

    public ZoneVector(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public boolean isInAABB(ZoneVector min, ZoneVector max) {
        return ((this.x <= max.x) && (this.x >= min.x) && (this.z <= max.z) && (this.z >= min.z));
    }
}