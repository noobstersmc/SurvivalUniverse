package me.infinityz.listeners;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.world.PortalCreateEvent;

import me.infinityz.SurvivalUniverse;
import me.infinityz.chunks.types.PlayerChunk;
import me.infinityz.players.SurvivalPlayer;

/**
 * ChunkListener
 */
public class ChunkListener implements Listener {

    SurvivalUniverse instance;

    public ChunkListener(SurvivalUniverse instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onShears(PlayerShearEntityEvent e) {
        // Cancel shears if not ally
        final PlayerChunk chunk = (PlayerChunk) instance.chunkManager.findIChunkfromChunk(e.getEntity().getChunk());
        if (chunk == null)
            return;
        final SurvivalPlayer player = instance.playerManager.getPlayerFromId(e.getPlayer().getUniqueId());
        if (chunk.isOwner(e.getPlayer()) || (player != null && player.isAlly(e.getPlayer().getUniqueId())))
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.isCancelled())
            return;
        if (e.getEntity() instanceof Monster || e.getEntity() instanceof Player) /* Issue #3 - Player Damage */
            return;
        PlayerChunk c = (PlayerChunk) instance.chunkManager.findIChunkfromChunk(e.getEntity().getChunk());
        if (c != null) {
            if (e.getDamager() instanceof Player) {
                Player pl = (Player) e.getDamager();
                SurvivalPlayer su = instance.playerManager.getPlayerFromId(c.owner);
                if (c.isOwner(pl) || (su != null && su.isAlly(pl.getUniqueId()))) {
                    e.setCancelled(false);
                    return;
                }
                e.setCancelled(true);
                return;
            }
            if (e.getDamager() instanceof Projectile) {
                Projectile prj = (Projectile) e.getDamager();
                if (prj.getShooter() instanceof Player) {
                    Player pl = (Player) prj.getShooter();
                    SurvivalPlayer su = instance.playerManager.getPlayerFromId(c.owner);
                    if (c.isOwner(pl) || (su != null && su.isAlly(pl.getUniqueId()))) {
                        e.setCancelled(false);
                        return;
                    }
                    e.setCancelled(true);
                    return;
                }
                e.setCancelled(true);
                return;

            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPortal(PortalCreateEvent e) {
        e.setCancelled(
                (PlayerChunk) instance.chunkManager.findIChunkfromChunk(e.getBlocks().get(0).getChunk()) != null);
    }

    @EventHandler
    public void onPlaceTNT(BlockPlaceEvent e) {
        if (e.getBlock().getType() == Material.TNT)
            e.setCancelled(
                    (PlayerChunk) instance.chunkManager.findIChunkfromChunk(e.getBlockPlaced().getChunk()) != null);
    }

    @EventHandler
    public void onExplode(BlockExplodeEvent e) {
        e.setCancelled((PlayerChunk) instance.chunkManager.findIChunkfromChunk(e.getBlock().getChunk()) != null);
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        if (e.getEntity().getType() != EntityType.PRIMED_TNT)
            return;
        e.blockList()
                .removeIf(block -> (PlayerChunk) instance.chunkManager.findIChunkfromChunk(block.getChunk()) != null);
    }/* Issue #3 - TNT In city fixed */

    @EventHandler
    public void onWither(CreatureSpawnEvent e) {
        if (e.getEntityType() == EntityType.WITHER || e.getEntityType() == EntityType.ENDER_CRYSTAL) {
            e.setCancelled((PlayerChunk) instance.chunkManager.findIChunkfromChunk(e.getEntity().getChunk()) != null);
        }
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent e) {
        if (e.getEntityType() == EntityType.WITHER)
            return;
        if (e.getEntityType() == EntityType.ARMOR_STAND) {
            return;
        }
        if (e.getSpawnReason() == SpawnReason.EGG || e.getSpawnReason() == SpawnReason.BREEDING
                || e.getSpawnReason() == SpawnReason.SPAWNER || e.getSpawnReason() == SpawnReason.SPAWNER_EGG
                || e.getSpawnReason() == SpawnReason.CURED || e.getSpawnReason() == SpawnReason.INFECTION
                || e.getSpawnReason() == SpawnReason.DISPENSED_EGG || e.getSpawnReason() == SpawnReason.SHEARED || e.getSpawnReason() == SpawnReason.LIGHTNING || e.getSpawnReason() == SpawnReason.BEEHIVE)
            return;
        e.setCancelled((PlayerChunk) instance.chunkManager.findIChunkfromChunk(e.getEntity().getChunk()) != null);
    }

}
