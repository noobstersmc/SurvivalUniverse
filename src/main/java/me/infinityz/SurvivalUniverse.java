package me.infinityz;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import me.infinityz.chunks.ChunkManager;
import me.infinityz.chunks.types.PlayerChunk;
import me.infinityz.cities.City;
import me.infinityz.cities.CityManager;
import me.infinityz.commands.ChunkCommands;
import me.infinityz.commands.PvPCommand;
import me.infinityz.commands.TeleportCommands;
import me.infinityz.listeners.ChunkListener;
import me.infinityz.listeners.CityListener;
import me.infinityz.listeners.DatabaseLoggerListener;
import me.infinityz.listeners.GlobalListeners;
import me.infinityz.players.PlayerManager;
import me.infinityz.scoreboard.ScoreboardManager;
import me.infinityz.stats.DatabaseManager;
import me.infinityz.utils.FileConfig;

/**
 * SurvivalUniverse
 */
public class SurvivalUniverse extends JavaPlugin {
    public static SurvivalUniverse instance;

    public ScoreboardManager scoreboardManager;
    public PlayerManager playerManager;
    public ChunkManager chunkManager;
    public CityManager cityManager;
    public FileConfig chunksFile;
    public FileConfig cityFile;
    public DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;
        this.scoreboardManager = new ScoreboardManager(this);
        this.playerManager = new PlayerManager(this);
        this.chunkManager = new ChunkManager(this);
        this.cityManager = new CityManager(this);
        this.chunksFile = new FileConfig(this, "chunks.yml", "chunks.yml");
        this.cityFile = new FileConfig(this, "city.yml", "city.yml");
        getCommand("pvp").setExecutor(new PvPCommand(this));
        final ChunkCommands ch = new ChunkCommands(this);
        getCommand("chunk").setExecutor(ch);
        getCommand("city").setExecutor(ch);
        getCommand("admin").setExecutor(ch);
        getCommand("helper").setExecutor(ch);
        getCommand("ally").setExecutor(ch);
        
        final TeleportCommands teleport = new TeleportCommands(this);
        getCommand("home").setExecutor(teleport);
        getCommand("t").setExecutor(teleport);
        getCommand("randomtp").setExecutor(teleport);
        getCommand("spawn").setExecutor(teleport);
        getCommand("regen").setExecutor(teleport);
        Bukkit.getPluginManager().registerEvents(new GlobalListeners(this), this);
        Bukkit.getPluginManager().registerEvents(teleport, this);
        Bukkit.getPluginManager().registerEvents(new CityListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ChunkListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DatabaseLoggerListener(this), this);
        loadChunks();
        loadCities();
        this.databaseManager = new DatabaseManager(this);
    }

    @Override
    public void onDisable() {

    }

    void loadChunks() {
        chunksFile.getConfigurationSection("chunks").getKeys(false).forEach(it -> {
            chunksFile.getConfigurationSection("chunks." + it).getKeys(false).forEach(cks -> {
                String keypath = "chunks." + it + "." + cks;
                UUID uuid = UUID.fromString(it);
                int x = chunksFile.getInt(keypath + ".x-coordinate");
                int z = chunksFile.getInt(keypath + ".z-coordinate");
                String world = chunksFile.getString(keypath + ".world");
                PlayerChunk chunk = new PlayerChunk(uuid, world, x, z);
                chunkManager.ownedChunksMap.put(chunk, uuid);
            });
        });
    }

    void loadCities() {
        cityFile.getConfigurationSection("Cities").getKeys(false).forEach(it -> {
            final String keypath = "Cities." + it;
            final int x = cityFile.getInt(keypath + ".x-center");
            final int z = cityFile.getInt(keypath + ".z-center");
            final int radius = cityFile.getInt(keypath + ".radius");
            final String world = cityFile.getString(keypath + ".world");
            final String cityName = cityFile.getString(keypath + ".city-name");

            List<UUID> helpers = new ArrayList<>();
            List<UUID> owners = new ArrayList<>();
            cityFile.getStringList(keypath + ".helpers").forEach(id -> helpers.add(UUID.fromString(id)));
            cityFile.getStringList(keypath + ".owners").forEach(id -> owners.add(UUID.fromString(id)));

            final City city = new City(cityName, world, x, z, radius);
            city.helpers = helpers.stream().toArray(UUID[]::new);
            city.owners = owners.stream().toArray(UUID[]::new);
            cityManager.cities.add(city);
        });
    }

}