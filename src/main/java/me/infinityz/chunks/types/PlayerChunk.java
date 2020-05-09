package me.infinityz.chunks.types;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import me.infinityz.SurvivalUniverse;
import me.infinityz.chunks.HirarchyAllowance;
import me.infinityz.chunks.IChunk;

/**
 * PlayerChunk
 */
public class PlayerChunk extends IChunk {
    public UUID owner;
    public String owner_last_known_name;

    /**
     * Player chunks are privately owned, though anyone can walk into them. The main
     * difference is that player chunks can only be interacted by allies, owner, or
     * the admin
     */

    public PlayerChunk(UUID owner, String world, int x, int z) {
        super(x, z, Bukkit.getWorld(world));
    }

    public PlayerChunk(UUID owner, World world, int x, int z) {
        super(x, z, world);
        this.isPrivate = true;
        final HirarchyAllowance h[] = new HirarchyAllowance[] { HirarchyAllowance.OWNER_AND_ALLY,
                HirarchyAllowance.ADMIN };
        this.canBuild = h;
        this.canInteract = h;
        this.owner = owner;
        this.owner_last_known_name = Bukkit.getOfflinePlayer(owner).getName();
    }

    @Override
    public boolean shouldBuild(UUID uuid) {
        return isOwner(uuid) || isAlly(uuid) || isAdmin(uuid);
    }

    @Override
    public boolean shouldInteract(UUID uuid) {
        return shouldBuild(uuid);
    }

    public boolean isAlly(UUID player) {
        return owner != null && SurvivalUniverse.instance.playerManager.getPlayerFromId(owner).isAlly(player);
    }

    public boolean isOwner(UUID player) {
        return owner.getMostSignificantBits() == player.getMostSignificantBits();
    }

    public boolean isOwner(Player player) {
        return isOwner(player.getUniqueId());
    }

}