package me.infinityz.stats.types;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import me.infinityz.SurvivalUniverse;
import me.infinityz.players.SurvivalPlayer;
import me.infinityz.stats.DatabaseManager;
import me.infinityz.stats.IDatabase;
import me.infinityz.stats.core.MySQL;
import net.md_5.bungee.api.ChatColor;

/**
 * vMySQL
 */
public class vMySQL implements IDatabase {
    MySQL mysql;
    DatabaseManager databaseManager;

    public vMySQL(MySQL mysql, DatabaseManager database) {
        this.mysql = mysql;
        this.databaseManager = database;
        try {
            mysql.connect();
            System.out.println("Succesfully connected to database");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect(){
        mysql.disconnect();
    }

    @Override
    public void loadPlayer(UUID uuid) throws Exception {
        try {
            if (!isInDatabase(uuid)) {
                mysql.update(
                        "INSERT INTO SurvivalUniversePlayers (UUID, allies) VALUES ('" + uuid.toString() + "', ' ');");
                databaseManager.alliesCachedData.put(uuid, new UUID[1]);
                return;
            }
            ResultSet player = mysql
                    .query("SELECT * FROM SurvivalUniversePlayers WHERE UUID='" + uuid.toString() + "'");
            if (player != null && player.next()) {
                String allies = player.getString("allies");
                if (allies.length() > 1) {
                    String[] st = allies.split(" ");
                    HashSet<UUID> set = new HashSet<>();
                    for (String str : st) {
                        if (str.length() < 2)
                            continue;
                        final UUID ally_uuid = UUID.fromString(str);
                        if (ally_uuid != null) {
                            set.add(ally_uuid);
                        }
                    }
                    databaseManager.alliesCachedData.put(uuid, set.toArray(new UUID[st.length]));

                } else {
                    databaseManager.alliesCachedData.put(uuid, new UUID[0]);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.broadcastMessage(ChatColor.RED + "An error has ocurred...");
        }

    }

    @Override
    public void savePlayer(UUID uuid) throws Exception {
        try {
            String str = "";
            SurvivalPlayer su = SurvivalUniverse.instance.playerManager.getPlayerFromId(uuid);
            if (su != null) {
                for (UUID ud : su.allies) {
                    if (ud == null)
                        continue;
                    str = str + ud.toString() + " ";
                }
            }
            if (isInDatabase(uuid)) {
                mysql.update(
                        "UPDATE SurvivalUniversePlayers SET allies='" + str + "' WHERE UUID='" + uuid.toString() + "'");

            } else {
                mysql.update("INSERT INTO SurvivalUniversePlayers (UUID, allies) VALUES ('" + uuid.toString() + "', '"
                        + str + "');");
            }

        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.broadcastMessage(ChatColor.RED + "An error has ocurred...");
        }

    }

    @Override
    public void updateStats(UUID uuid) throws Exception {
        final SurvivalPlayer su = SurvivalUniverse.instance.playerManager.getPlayerFromId(uuid);
        if (su == null)
            return;
        updateStats(uuid, su);
    }

    @Override
    public void updateStats(UUID uuid, SurvivalPlayer survivalPlayer) throws Exception {

    }

    boolean isInDatabase(UUID uuid) {
        try {
            final ResultSet rs = mysql
                    .query("SELECT * FROM SurvivalUniversePlayers WHERE UUID='" + uuid.toString() + "'");
            return rs.next() && rs.getString("UUID") != null;
        } catch (Exception ignore) {
        }
        return false;
    }

    @Override
    public void logEvent(BlockBreakEvent brk, Material type) throws Exception {
        Location loc = brk.getBlock().getLocation();
        double x_abs = Math.abs(loc.getX());
        double z_abs = Math.abs(loc.getZ());
        if (x_abs > 3000 || z_abs > 3000)
            return;
        String location = loc.getWorld().getName() + " (" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";

        mysql.update(String.format(
            "INSERT INTO SU_BLOCK_LOG (id, UUID, blockType, eventType, location, cancelled, date) VALUES(NULL, '%s', '%s', '%s', '%s', %s, '%s');",
            (brk.getPlayer() != null ? brk.getPlayer().getName() : "Not a player"),
            type.toString(), 
            "BREAK",
            location,
            brk.isCancelled() + "",
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now())
            ));
            

    }

    @Override
    public void logEvent(BlockPlaceEvent place) throws Exception {
        Location loc = place.getBlock().getLocation();
        double x_abs = Math.abs(loc.getX());
        double z_abs = Math.abs(loc.getZ());
        if (x_abs > 3000 || z_abs > 3000)
            return;
        String location = loc.getWorld().getName() + " (" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
        

        mysql.update(String.format(
            "INSERT INTO SU_BLOCK_LOG (id, UUID, blockType, eventType, location, cancelled, date) VALUES(NULL, '%s', '%s', '%s', '%s', %s, '%s');",
            (place.getPlayer() != null ? place.getPlayer().getName() : "Not a player"),
            place.getBlock().getType().toString(), 
            "PLACE",
            location,
            place.isCancelled() + "",
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now())
            ));
            

    }

}