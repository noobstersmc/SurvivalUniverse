package me.infinityz.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import me.infinityz.SurvivalUniverse;

public class DatabaseLoggerListener implements Listener {
    SurvivalUniverse instance;

    public DatabaseLoggerListener(SurvivalUniverse instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent e) {
        final Material type = e.getBlock().getType();
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            try {
                instance.databaseManager.database.logEvent(e, type);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlace(BlockPlaceEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            try {
                instance.databaseManager.database.logEvent(e);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace2(BlockPlaceEvent e) {
        switch (e.getBlock().getType()) {
            case BLAST_FURNACE:
            case SMOKER:
            case CARTOGRAPHY_TABLE:
            case BREWING_STAND:
            case COMPOSTER:
            case BARREL:
            case FLETCHING_TABLE:
            case CAULDRON:
            case LECTERN:
            case STONECUTTER:
            case LOOM:
            case SMITHING_TABLE:
            case GRINDSTONE: {
                e.getBlock().getLocation().getNearbyLivingEntities(10, 10).stream()
                        .filter(it -> it.getType() == EntityType.VILLAGER)
                        .filter(it -> (((Villager) it).getVillagerLevel() == 1
                                && ((Villager) it).getVillagerExperience() == 0))
                        .findFirst().ifPresent(it -> {
                            ((Villager)it).setMemory(MemoryKey.JOB_SITE, e.getBlock().getLocation());
                        });
                break;
            }
            default: {
                break;
            }
        }

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace2(BlockBreakEvent e) {
        switch (e.getBlock().getType()) {
            case BLAST_FURNACE:
            case SMOKER:
            case CARTOGRAPHY_TABLE:
            case BREWING_STAND:
            case COMPOSTER:
            case BARREL:
            case FLETCHING_TABLE:
            case CAULDRON:
            case LECTERN:
            case STONECUTTER:
            case LOOM:
            case SMITHING_TABLE:
            case GRINDSTONE: {
                e.getBlock().getLocation().getNearbyLivingEntities(10, 10).stream()
                        .filter(it -> it.getType() == EntityType.VILLAGER)
                        .filter(it -> (((Villager) it).getVillagerLevel() == 1
                                && ((Villager) it).getVillagerExperience() == 0)
                                && ((Villager)it).getMemory(MemoryKey.JOB_SITE).equals(e.getBlock().getLocation()))
                        .findFirst().ifPresent(it -> {
                            ((Villager)it).setMemory(MemoryKey.JOB_SITE, e.getBlock().getLocation());
                        });
                break;
            }
            default: {
                break;
            }
        }

    }

}