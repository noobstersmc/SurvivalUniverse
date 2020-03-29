package me.infinityz.listeners;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;

import me.infinityz.SurvivalUniverse;
import me.infinityz.chunks.types.PlayerChunk;
import me.infinityz.cities.City;
import me.infinityz.players.SurvivalPlayer;

/**
 * ChunkListener
 */
public class CityListener implements Listener {
    SurvivalUniverse instance;

    public CityListener(SurvivalUniverse instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getCause() == DamageCause.ENTITY_ATTACK || e.getEntity().getType() == EntityType.PLAYER
                || e.getEntity().getType() == EntityType.ENDER_PEARL)
            return;
        if (e.getEntity().getCustomName() != null) {
            e.setCancelled(inCity(e.getEntity().getLocation()));
        }
    }

    @EventHandler
    public void onDamageEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity().getType() == EntityType.PLAYER)
            return;

        if (e.getEntity().getCustomName() != null) {
            e.setCancelled(
                    e.getDamager() instanceof Player ? inCity(e.getEntity().getLocation(), (Player) e.getDamager())
                            : inCity(e.getEntity().getLocation()));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBreak(BlockBreakEvent e) {
        if (e.getPlayer() == null)
            return;
        final Player player = e.getPlayer();
        final City city = instance.cityManager.isInCity(player.getLocation());
        if (city == null)
            return;
        if (city.isOwner(player))
            return;
        if (city.isHelper(player)) {
            final PlayerChunk chunk = (PlayerChunk) instance.chunkManager.findIChunkfromChunk(player.getChunk());
            if (chunk == null)
                return;
            if (chunk.isOwner(player))
                return;
            SurvivalPlayer su = instance.playerManager.getPlayerFromId(chunk.owner);
            if (su != null) {
                if (su.isAlly(player.getUniqueId())) {
                    e.setCancelled(false);
                    return;
                }
            }
            player.sendMessage("You're a helper and can't edit " + chunk.owner_last_known_name + "'s chunk.");
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlace(BlockPlaceEvent e) {
        if (e.getPlayer() == null)
            return;
        final Player player = e.getPlayer();
        final City city = instance.cityManager.isInCity(player.getLocation());
        if (city == null)
            return;
        if (city.isOwner(player))
            return;
        if (city.isHelper(player)) {
            final PlayerChunk chunk = (PlayerChunk) instance.chunkManager.findIChunkfromChunk(player.getChunk());
            if (chunk == null)
                return;
            if (chunk.isOwner(player))
                return;
            SurvivalPlayer su = instance.playerManager.getPlayerFromId(chunk.owner);
            if (su != null) {
                if (su.isAlly(player.getUniqueId())) {
                    e.setCancelled(false);
                    return;
                }
            }
            player.sendMessage("You're a helper and can't edit " + chunk.owner_last_known_name + "'s chunk.");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        if (e.getEntity().getType() == EntityType.PRIMED_TNT)
            return;
        e.blockList().removeIf(block -> inCity(block.getLocation()));
    }/* Issue #3 - TNT In city fixed */

    boolean inCity(Location location, Player player) {
        boolean bol = false;
        final City city = instance.cityManager.isInCity(location);
        if (city != null) {
            if (!(city.isOwner(player) || city.isHelper(player))) {
                bol = true;
            }
        }
        return bol;
    }

    boolean inCity(Location location) {
        return instance.cityManager.isInCity(location) != null;
    }

}
