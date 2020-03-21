package me.infinityz.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;

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


}