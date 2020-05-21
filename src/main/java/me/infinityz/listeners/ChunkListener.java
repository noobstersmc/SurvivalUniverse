package me.infinityz.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
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
import org.spigotmc.event.entity.EntityMountEvent;

import me.infinityz.SurvivalUniverse;
import me.infinityz.chunks.IChunk;
import me.infinityz.chunks.types.PlayerChunk;
import me.infinityz.chunks.types.SafeChunk;

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
        final IChunk c = instance.chunkManager.getChunkFromLoc(e.getEntity().getLocation());
        e.setCancelled(c != null ? c.shouldInteract(e.getPlayer().getUniqueId()) : false);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        // TODO: Investigate, weather or not zombies can damage villagers to turn them
        // into zombie vigs.
        if (e.isCancelled())
            return;
        if (e.getEntity() instanceof Monster || e.getEntity() instanceof Player) /* Issue #3 - Player Damage */
            return;
        final IChunk chunk = instance.chunkManager.getChunkFromLoc(e.getEntity().getLocation());
        if (chunk != null) {
            final Player p = GlobalListeners.getPlayerDamagerEntityEvent(e);
            if (p != null) {
                e.setCancelled(!chunk.shouldDamageEntity(p.getUniqueId()));
            }
        }
    }

    @EventHandler
    public void mount(EntityMountEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            PlayerChunk pc = instance.chunkManager.getChunkFromLocationByType(PlayerChunk.class,
                    e.getMount().getLocation());
            if (pc != null) {
                e.setCancelled(!pc.shouldBuild(p.getUniqueId(), e.getMount().getLocation()));
            }

        }
    }

    @EventHandler
    public void onPortal(PortalCreateEvent e) {
        final IChunk c = instance.chunkManager.getChunkFromLoc(e.getBlocks().get(0).getLocation());
        e.setCancelled(c != null && c.getClass().isAssignableFrom(PlayerChunk.class));
    }

    @EventHandler
    public void onPlaceTNT(BlockPlaceEvent e) {
        final IChunk c = instance.chunkManager.getChunkFromLoc(e.getBlockPlaced().getLocation());
        e.setCancelled(c != null && (c.getClass().isAssignableFrom(PlayerChunk.class)));
    }

    @EventHandler
    public void onExplode(BlockExplodeEvent e) {
        final IChunk c = instance.chunkManager.getChunkFromLoc(e.getBlock().getLocation());
        e.setCancelled(c != null && (c.getClass().isAssignableFrom(PlayerChunk.class)
                || c.getClass().isAssignableFrom(SafeChunk.class)));
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        if (e.getEntity().getType() != EntityType.PRIMED_TNT)
            return;
        e.blockList().removeIf(block -> instance.chunkManager.getChunkFromLoc(block.getLocation()) != null);
    }/* Issue #3 - TNT In city fixed */

    @EventHandler
    public void onWither(CreatureSpawnEvent e) {
        if (e.getEntityType() == EntityType.WITHER || e.getEntityType() == EntityType.ENDER_CRYSTAL) {
            final IChunk c = instance.chunkManager.getChunkFromLoc(e.getEntity().getLocation());
            e.setCancelled(c != null && c.getClass().isAssignableFrom(PlayerChunk.class));
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
                || e.getSpawnReason() == SpawnReason.DISPENSE_EGG || e.getSpawnReason() == SpawnReason.SHEARED
                || e.getSpawnReason() == SpawnReason.LIGHTNING || e.getSpawnReason() == SpawnReason.BEEHIVE)
            return;

        final IChunk c = instance.chunkManager.getChunkFromLoc(e.getEntity().getLocation());
        e.setCancelled(c != null && c.getClass().isAssignableFrom(PlayerChunk.class));
    }

}
