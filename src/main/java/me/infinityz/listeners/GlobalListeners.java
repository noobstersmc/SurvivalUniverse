package me.infinityz.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import me.infinityz.SurvivalUniverse;
import me.infinityz.cities.City;
import me.infinityz.cities.CityChangeEvent;
import me.infinityz.players.SurvivalPlayer;
import me.infinityz.scoreboard.FastBoard;
import net.md_5.bungee.api.ChatColor;

/**
 * GlobalListeners
 */
public class GlobalListeners implements Listener {
    SurvivalUniverse instance;

    public GlobalListeners(SurvivalUniverse instance){
        this.instance = instance;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        final Player player = e.getPlayer();
        instance.playerManager.survivalPlayerMap.put(player.getUniqueId(), new SurvivalPlayer(player.getUniqueId()));
        //TODO: Handle scoreboards
        FastBoard fb = new FastBoard(player);
        fb.updateTitle("Survival Universe");
        fb.updateLines("Chunk: " + player.getChunk().toString());
        instance.scoreboardManager.scoreboardHashMap.put(player.getUniqueId(), fb);

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        final Player player = e.getPlayer();
        instance.playerManager.survivalPlayerMap.remove(player.getUniqueId());
        instance.scoreboardManager.scoreboardHashMap.remove(player.getUniqueId());

    }

    @EventHandler
    public void onChunkChange(PlayerChangeChunkEvent e){
        instance.scoreboardManager.scoreboardHashMap.get(e.player.getUniqueId()).updateLine(0, "Chunk: " + e.to.toString());

    }

    @EventHandler
    public void onCityChanged(CityChangeEvent e){
        if(e.from != null){
            e.player.sendMessage("Haz salido de " + e.from.cityName);
            instance.scoreboardManager.scoreboardHashMap.get(e.player.getUniqueId()).updateLines(instance.scoreboardManager.scoreboardHashMap.get(e.player.getUniqueId()).getLine(0));
        }
        if(e.to != null){
            e.player.sendMessage("Bievenido a " + e.to.cityName);
            instance.scoreboardManager.scoreboardHashMap.get(e.player.getUniqueId()).updateLine(3, "City: " + e.to.cityName);
    
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (e.isCancelled())
            return;
        final Chunk to = e.getTo().getChunk();
        final Chunk from = e.getFrom().getChunk();
        if (to.getX() != from.getX() || to.getZ() != from.getZ()) {
            final Player player = e.getPlayer();
            Bukkit.getPluginManager().callEvent(new PlayerChangeChunkEvent(player, from, to));
        }
        if(e.getTo().getBlockX() != e.getFrom().getBlockX() || e.getTo().getBlockZ() != e.getFrom().getBlockZ()){
            
            Bukkit.getScheduler().runTaskAsynchronously(instance, ()->{
                City player_city = instance.cityManager.lastKnownCityMap.get(e.getPlayer().getUniqueId());
                City found_city = null;
                for(City city: instance.cityManager.cities){
                    if(instance.cityManager.isInRectangle(e.getTo(), city)){
                        found_city = city;
                        break;
                    }
                }
                if(player_city == found_city)return;
                Bukkit.getPluginManager().callEvent(new CityChangeEvent(e.getPlayer(), player_city, found_city));
                instance.cityManager.lastKnownCityMap.put(e.getPlayer().getUniqueId(), found_city);
            });

        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e){
        if (e.isCancelled())
            return;
        final Chunk to = e.getTo().getChunk();
        final Chunk from = e.getFrom().getChunk();
        if (to.getX() != from.getX() || to.getZ() != from.getZ()) {
            final Player player = e.getPlayer();
            Bukkit.getPluginManager().callEvent(new PlayerChangeChunkEvent(player, from, to));
        }
        if(e.getTo().getBlockX() != e.getFrom().getBlockX() || e.getTo().getBlockZ() != e.getFrom().getBlockZ()){
            
            Bukkit.getScheduler().runTaskAsynchronously(instance, ()->{
                City player_city = instance.cityManager.lastKnownCityMap.get(e.getPlayer().getUniqueId());
                City found_city = null;
                for(City city: instance.cityManager.cities){
                    if(instance.cityManager.isInRectangle(e.getTo(), city)){
                        found_city = city;
                        break;
                    }
                }
                if(player_city == found_city)return;
                Bukkit.getPluginManager().callEvent(new CityChangeEvent(e.getPlayer(), player_city, found_city));
                instance.cityManager.lastKnownCityMap.put(e.getPlayer().getUniqueId(), found_city);
            });

        }
        
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity().getType() != EntityType.PLAYER)
            return;
        Player damager;
        /*
         * Perform the checks to know if the damager was a projectile and if the shooter
         * of that projectile was a player
         */
        if (e.getDamager().getType() != EntityType.PLAYER) {
            if (!(e.getDamager() instanceof Projectile))
                return;
            final Projectile projectile = (Projectile) e.getDamager();
            if (projectile.getShooter() != null && !(projectile.getShooter() instanceof Player))
                return;
            damager = (Player) projectile.getShooter();
        } else {
            damager = (Player) e.getDamager();
        }
        final SurvivalPlayer survivalDamagerPlayer = instance.playerManager.survivalPlayerMap.get(damager.getUniqueId());
        if(!survivalDamagerPlayer.pvp){
            damager.sendMessage(ChatColor.RED + "Your PVP is disabled. Use /pvp on to enable it!");
            e.setCancelled(true);
            return;
        }
        final Player damaged = (Player) e.getEntity();
        final SurvivalPlayer survivalDamagedPlayer = instance.playerManager.survivalPlayerMap.get(damaged.getUniqueId());
        if(!survivalDamagedPlayer.pvp){
            e.setCancelled(true);
            damager.sendMessage(ChatColor.RED + damaged.getName() + " has not enabled their pvp!");
            return;
        }
    }

}