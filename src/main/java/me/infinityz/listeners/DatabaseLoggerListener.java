package me.infinityz.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import me.infinityz.SurvivalUniverse;

public class DatabaseLoggerListener implements Listener {
    SurvivalUniverse instance;

    public DatabaseLoggerListener(SurvivalUniverse instance){
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            try {
                instance.databaseManager.database.logEvent(e);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlace(BlockPlaceEvent e){
        Bukkit.getScheduler().runTaskAsynchronously(instance, ()->{
            try {
                instance.databaseManager.database.logEvent(e);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
    }


}