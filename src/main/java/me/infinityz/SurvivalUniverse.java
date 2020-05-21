package me.infinityz;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import me.infinityz.chunks.ChunkManager;
import me.infinityz.chunks.Tristate;
import me.infinityz.chunks.types.ClaimableChunk;
import me.infinityz.chunks.types.PlayerChunk;
import me.infinityz.chunks.types.PublicChunk;
import me.infinityz.chunks.types.PvPChunk;
import me.infinityz.chunks.types.SafeChunk;
import me.infinityz.chunks.types.VipChunk;
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
import net.md_5.bungee.api.ChatColor;

/**
 * SurvivalUniverse
 */
public class SurvivalUniverse extends JavaPlugin implements PluginMessageListener {
    public static SurvivalUniverse instance;

    public ScoreboardManager scoreboardManager;
    public PlayerManager playerManager;
    public ChunkManager chunkManager;
    public CityManager cityManager;
    public FileConfig chunksFile, claimableChunkFile, cityFile, config;
    public DatabaseManager databaseManager;
    public Tristate globalPvp;
    public Boolean pvp_drops = false;
    int bungee_players = 0;

    @Override
    public void onEnable() {
        instance = this;
        this.scoreboardManager = new ScoreboardManager(this);
        this.playerManager = new PlayerManager(this);
        this.chunkManager = new ChunkManager(this);
        this.cityManager = new CityManager(this);
        this.claimableChunkFile = new FileConfig(this, "claimable_chunks.yml", "claimable_chunks.yml");
        this.chunksFile = new FileConfig(this, "chunks.yml", "chunks.yml");
        this.cityFile = new FileConfig(this, "city.yml", "city.yml");
        this.config = new FileConfig(this, "config.yml", "config.yml");
        PvPCommand pvpcmd = new PvPCommand(this);
        getCommand("pvp").setExecutor(pvpcmd);
        Bukkit.getPluginManager().registerEvents(pvpcmd, this);
        getCommand("globalpvp").setExecutor(pvpcmd);
        getCommand("pvpdrops").setExecutor(pvpcmd);
        final ChunkCommands ch = new ChunkCommands(this);
        getCommand("chunk").setExecutor(ch);
        getCommand("claim").setExecutor(ch);
        getCommand("city").setExecutor(ch);
        getCommand("admin").setExecutor(ch);
        getCommand("helper").setExecutor(ch);
        getCommand("ally").setExecutor(ch);
        this.globalPvp = getTristeState(config.get("global-pvp").toString());

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
        loadClaimable();
        this.databaseManager = new DatabaseManager(this);
        boolean c = config.getBoolean("bungee-online-players");
        if (c) {
            this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
        }
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (c)
                getOnline();
            scoreboardManager.scoreboardHashMap.values().stream().forEach(it -> {
                it.updateLine(5, ChatColor.GREEN + "Players: " + ChatColor.WHITE
                        + (c ? bungee_players : Bukkit.getOnlinePlayers().size()));
            });
        }, 20, 20);
    }

    Tristate getTristeState(String str) {
        switch (str.toLowerCase()) {
            case "true":
                return Tristate.TRUE;
            case "false":
                return Tristate.FALSE;
            default:
                return Tristate.UNKNOWN;
        }
    }

    @Override
    public void onDisable() {
        this.databaseManager.database.disconnect();

    }

    void loadChunks() {
        chunksFile.getConfigurationSection("chunks.player-chunks").getKeys(false).forEach(it -> {
            chunksFile.getConfigurationSection("chunks.player-chunks." + it).getKeys(false).forEach(cks -> {
                String keypath = "chunks.player-chunks." + it + "." + cks;
                UUID uuid = UUID.fromString(it);
                int x = chunksFile.getInt(keypath + ".x-coordinate");
                int z = chunksFile.getInt(keypath + ".z-coordinate");
                String world = chunksFile.getString(keypath + ".world");
                PlayerChunk chunk = new PlayerChunk(uuid, world, x, z);
                if (!chunkManager.verifyDups(chunk)) {
                    chunkManager.ownedChunksMap.put(chunk, uuid);
                    System.out.println("Loaded: Owned " + x + ", " + z);
                    chunkManager.chunkMap.put(chunk, 1);
                }
            });
        });
        chunksFile.getConfigurationSection("chunks.pvp-chunks").getKeys(false).forEach(it -> {
            final String keypath = "chunks.pvp-chunks." + it + ".";
            final int x = chunksFile.getInt(keypath + "x-coordinate");
            final int z = chunksFile.getInt(keypath + "z-coordinate");
            final World w = Bukkit.getWorld(chunksFile.getString(keypath + "world"));
            if (w == null)
                return;
            final PvPChunk chunk = new PvPChunk(x, z, w);
            if (!chunkManager.verifyDups(chunk)) {
                chunkManager.chunkMap.put(chunk, 1);
                System.out.println("Loaded: PVP " + x + ", " + z);
            }
        });
        chunksFile.getConfigurationSection("chunks.safe-chunks").getKeys(false).forEach(it -> {
            final String keypath = "chunks.safe-chunks." + it + ".";
            final int x = chunksFile.getInt(keypath + "x-coordinate");
            final int z = chunksFile.getInt(keypath + "z-coordinate");
            final World w = Bukkit.getWorld(chunksFile.getString(keypath + "world"));
            if (w == null)
                return;
            final SafeChunk chunk = new SafeChunk(x, z, w);
            if (!chunkManager.verifyDups(chunk)) {
                chunkManager.chunkMap.put(chunk, 1);
                System.out.println("Loaded: SafeChunk " + x + ", " + z);
            }
        });
        chunksFile.getConfigurationSection("chunks.vip-chunks").getKeys(false).forEach(it -> {
            final String keypath = "chunks.vip-chunks." + it + ".";
            final int x = chunksFile.getInt(keypath + "x-coordinate");
            final int z = chunksFile.getInt(keypath + "z-coordinate");
            final World w = Bukkit.getWorld(chunksFile.getString(keypath + "world"));
            if (w == null)
                return;
            final VipChunk chunk = new VipChunk(x, z, w);
            if (!chunkManager.verifyDups(chunk)) {
                chunkManager.chunkMap.put(chunk, 1);
                System.out.println("Loaded: VipChunk " + x + ", " + z);
            }
        });
        chunksFile.getConfigurationSection("chunks.public-chunks").getKeys(false).forEach(it -> {
            final String keypath = "chunks.public-chunks." + it + ".";
            final int x = chunksFile.getInt(keypath + "x-coordinate");
            final int z = chunksFile.getInt(keypath + "z-coordinate");
            final World w = Bukkit.getWorld(chunksFile.getString(keypath + "world"));
            if (w == null)
                return;
            final PublicChunk chunk = new PublicChunk(x, z, w);
            if (!chunkManager.verifyDups(chunk)) {
                chunkManager.chunkMap.put(chunk, 1);
                System.out.println("Loaded: PublicChunk " + x + ", " + z);
            }
        });
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (subchannel.equalsIgnoreCase("PlayerCount")) {
            in.readUTF();
            bungee_players = in.readInt();
        }

    }

    void getOnline() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PlayerCount");
        out.writeUTF("ALL");
        Bukkit.getOnlinePlayers().stream().findFirst().ifPresent(it -> {
            it.sendPluginMessage(this, "BungeeCord", out.toByteArray());
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

    void loadClaimable() {
        claimableChunkFile.getStringList("claimable-chunk-list").forEach(it -> {
            final String str[] = it.split(" ");
            final int x = Integer.parseInt(str[0]);
            final int z = Integer.parseInt(str[1]);
            final String world = str[2];
            ClaimableChunk c = new ClaimableChunk(x, z, world);
            if (instance.chunkManager.addClaimableChunk(c)) {
                chunkManager.chunkMap.put(c, 1);
            }
        });
    }

}