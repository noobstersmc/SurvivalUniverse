package me.infinityz.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import me.infinityz.SurvivalUniverse;

/**
 * ChunkListener
 */
public class CityListener implements Listener {
    SurvivalUniverse instance;

    public CityListener(SurvivalUniverse instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e){
        if(e.getCause() == DamageCause.ENTITY_ATTACK)return;
    }

    @EventHandler
    public void onExplode(BlockExplodeEvent e){
        e.setCancelled(true);
        e.setYield(0.0f);
        
    }

    @EventHandler
    public void onCrystal(EntityExplodeEvent e){
        if(e.getEntityType() == EntityType.ENDER_CRYSTAL){
            e.setCancelled(true);
        }
        e.setCancelled(true);
        e.setYield(0.0f);
    }

}