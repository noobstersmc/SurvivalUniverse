package me.infinityz.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
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
        final SurvivalPlayer player = instance.playerManager.survivalPlayerMap.get(chunk.owner);
        if (chunk.isOwner(e.getPlayer()) || (player != null && player.isAlly(e.getPlayer().getUniqueId())))
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.isCancelled())
            return;
        if (e.getEntity() instanceof Monster)
            return;
        e.setCancelled((PlayerChunk) instance.chunkManager.findIChunkfromChunk(e.getEntity().getChunk()) != null);
    }

    @EventHandler
    public void onPortal(PortalCreateEvent e) {
        e.setCancelled(
                (PlayerChunk) instance.chunkManager.findIChunkfromChunk(e.getBlocks().get(0).getChunk()) != null);
    }

    @EventHandler
    public void onPlaceTNT(BlockPlaceEvent e) {
        if(e.getBlock().getType() != Material.TNT)return;
        e.setCancelled((PlayerChunk) instance.chunkManager.findIChunkfromChunk(e.getBlockPlaced().getChunk()) != null);
    }

    @EventHandler
    public void onWither(CreatureSpawnEvent e) {
        if (e.getEntityType() != EntityType.WITHER)
            return;
        e.setCancelled((PlayerChunk) instance.chunkManager.findIChunkfromChunk(e.getEntity().getChunk()) != null);
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent e) {
        if (e.getEntityType() == EntityType.WITHER)
            return;
        if (e.getSpawnReason() == SpawnReason.EGG || e.getSpawnReason() == SpawnReason.BREEDING)
            return;
        e.setCancelled((PlayerChunk) instance.chunkManager.findIChunkfromChunk(e.getEntity().getChunk()) != null);
    }

}