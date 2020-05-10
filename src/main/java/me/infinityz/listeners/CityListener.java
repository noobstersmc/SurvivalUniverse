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
import me.infinityz.chunks.IChunk;
import me.infinityz.cities.City;

/**
 * ChunkListener
 */
public class CityListener implements Listener {
    SurvivalUniverse instance;

    public CityListener(SurvivalUniverse instance) {
        this.instance = instance;
    }

    /* Update: Now only Smith entites are invincible */
    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getCause() == DamageCause.ENTITY_ATTACK || e.getEntity().getType() == EntityType.PLAYER
                || e.getEntity().getType() == EntityType.ENDER_PEARL)
            return;
        if (e.getEntity().getCustomName() != null && e.getEntity().getName().equals("Smith")) {
            e.setCancelled(inCity(e.getEntity().getLocation()));
        }
    }

    @EventHandler
    public void onDamageEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity().getType() == EntityType.PLAYER)
            return;

        if (e.getEntity().getCustomName() != null && e.getEntity().getName().equals("Smith")) {
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
        final IChunk ch = instance.chunkManager.findIChunkfromChunk(player.getChunk());
        if (ch != null) {
            e.setCancelled(!ch.shouldBuild(player.getUniqueId(), e.getBlock().getLocation()));
        } else {
            final City city = instance.cityManager.isInCity(e.getBlock().getLocation());
            if (city != null)
                e.setCancelled(!(city.isHelper(player) || city.isOwner(player)));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlace(BlockPlaceEvent e) {
        if (e.getPlayer() == null)
            return;
        final Player player = e.getPlayer();
        final IChunk ch = instance.chunkManager.findIChunkfromChunk(player.getChunk());
        if (ch != null) {
            e.setCancelled(!ch.shouldBuild(player.getUniqueId(), e.getBlock().getLocation()));
        } else {
            final City city = instance.cityManager.isInCity(e.getBlock().getLocation());
            if (city != null)
                e.setCancelled(!(city.isHelper(player) || city.isOwner(player)));
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
