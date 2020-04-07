package me.infinityz.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import me.infinityz.SurvivalUniverse;
import me.infinityz.chunks.IChunk;
import me.infinityz.chunks.types.ClaimableChunk;
import me.infinityz.chunks.types.PlayerChunk;
import me.infinityz.cities.City;
import me.infinityz.players.SurvivalPlayer;
import net.md_5.bungee.api.ChatColor;

/**
 * ChunkCommands
 */
public class ChunkCommands implements CommandExecutor, TabCompleter {

    SurvivalUniverse instance;
    String[] helpArray = { "check", "delete", "own", "list", "claimlist", "claimadd", "claimremove" };
    String[] helperHelpArray = { "add", "remove" };
    String[] allyHelpArray = { "add", "remove", "list" };

    public ChunkCommands(SurvivalUniverse instance) {
        this.instance = instance;
    }

    @SuppressWarnings("all")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("chunk")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Console can't use chunk.");
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage("Command usage: /chunk <check:delete:own> [Player]");
                return true;
            }
            Player player = (Player) sender;
            switch (args[0].toLowerCase()) {
                case "own": {
                    if (args.length > 1) {
                        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                        final City city = instance.cityManager.isInCity(player.getLocation());
                        if (city == null || (!city.isOwner(player))) {
                            sender.sendMessage(ChatColor.RED + "No permissions");
                            return true;
                        }
                        final Chunk c = player.getLocation().getChunk();
                        IChunk iChunk = instance.chunkManager.getChunkNoType(c);
                        if (iChunk == null) {
                            PlayerChunk newIChunk = new PlayerChunk(target.getUniqueId(), c.getWorld().getName(),
                                    c.getX(), c.getZ());
                            instance.chunkManager.ownedChunksMap.put(newIChunk, target.getUniqueId());
                            String keyString = "chunks." + target.getUniqueId() + "." + newIChunk.toString();
                            instance.chunksFile.set(keyString + ".world", newIChunk.chunkWorld.getName());
                            instance.chunksFile.set(keyString + ".x-coordinate", newIChunk.chunkX);
                            instance.chunksFile.set(keyString + ".z-coordinate", newIChunk.chunkZ);
                            instance.chunksFile.save();
                            instance.chunksFile.reload();
                            sender.sendMessage("This chunk has been given to " + target.getName());

                        } else {
                            sender.sendMessage("This chunk is already owned.");
                        }

                    } else {
                        sender.sendMessage("Command usage: /chunk <check:delete:own> [Player]");
                        return true;
                    }
                    break;
                }
                case "delete": {

                    final City city = instance.cityManager.isInCity(player.getLocation());
                    if (city == null || (!city.isOwner(player))) {
                        sender.sendMessage(ChatColor.RED + "No permissions");
                        return true;
                    }
                    final Chunk c = player.getLocation().getChunk();
                    final PlayerChunk playerChunk = (PlayerChunk) instance.chunkManager.findIChunkfromChunk(c);
                    if (playerChunk == null) {
                        // Chunk null, send message if needed
                        return true;
                    }
                    if (instance.chunkManager.ownedChunksMap.remove(playerChunk) != null) {
                        for (String str : instance.chunksFile.getConfigurationSection("chunks." + playerChunk.owner)
                                .getKeys(false)) {
                            final String path = "chunks." + playerChunk.owner + "." + str;
                            final int x = instance.chunksFile.getInt(path + ".x-coordinate");
                            final int z = instance.chunksFile.getInt(path + ".z-coordinate");
                            final String world = instance.chunksFile.getString(path + ".world");
                            if (playerChunk.chunkX == x && playerChunk.chunkZ == z
                                    && playerChunk.chunkWorld.getName().equalsIgnoreCase(world)) {
                                instance.chunksFile.set(path, null);
                                instance.chunksFile.save();
                                instance.chunksFile.reload();
                                sender.sendMessage(
                                        ChatColor.translateAlternateColorCodes('&', "&aChunk deleted succesfully."));
                                break;
                            }
                        }
                    } else {
                        sender.sendMessage("Couldn't delete such chunk");
                    }

                    break;
                }
                case "check": {
                    final Chunk c = player.getLocation().getChunk();
                    IChunk iChunk = instance.chunkManager.findIChunkfromChunk(c);
                    if (iChunk == null) {
                        sender.sendMessage(ChatColor.RED + "This chunk is not owned!");
                        return true;
                    } else {
                        if (iChunk instanceof PlayerChunk) {
                            PlayerChunk chunk = (PlayerChunk) iChunk;
                            sender.sendMessage("The owner is " + chunk.owner_last_known_name);

                        } else {
                            sender.sendMessage("Unsupported, city chunks are not yet working!");
                        }
                        // Check for the chunk type and then weather city or owned and in such case
                        // return the owner.
                    }
                    break;
                }
                case "list": {
                    if (args.length > 1) {
                        OfflinePlayer of = Bukkit.getOfflinePlayer(args[0]);

                        return true;
                    }
                    final SurvivalPlayer su = instance.playerManager.getPlayerFromId(player.getUniqueId());
                    if (su != null) {
                        player.sendMessage("Your chunks are: ");
                        instance.chunkManager.ownedChunksMap.forEach((c, id) -> {
                            if (id.getMostSignificantBits() == player.getUniqueId().getMostSignificantBits()) {
                                player.sendMessage(
                                        " - " + c.chunkWorld.getName() + " (" + c.chunkX + ", " + c.chunkZ + ")");
                            }

                        });

                    } else {
                        sender.sendMessage("Error");
                    }

                    break;
                }
                case "claim": {
                    if (player.getWorld().getEnvironment() != Environment.NORMAL) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                "&cYou MUST be in the overworld to use this command."));
                        return true;
                    }
                    /* Only allow claim if has played 1h+ */
                    final int played_time_seconds = (player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20);
                    if ((played_time_seconds / 3600) < 1) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                String.format("&cYou have to have played at least one hour to claim a chunk! (%.2f)h",
                                        played_time_seconds / 3600.00D)));
                        return true;
                    }
                    /* Ensure player isn't null */
                    final SurvivalPlayer su = instance.playerManager.getPlayerFromId(player.getUniqueId());
                    if (su == null) {
                        sender.sendMessage(
                                ChatColor.translateAlternateColorCodes('&', "&cAn error has ocurred. please relog"));
                        return true;
                    }
                    /* Limit the claim to only one chunk per player */
                    if (instance.chunkManager.ownedChunksMap.values().stream()
                            .filter(it -> it.getMostSignificantBits() == su.playerUUID.getMostSignificantBits())
                            .findAny().isPresent()) {
                        sender.sendMessage(
                                ChatColor.translateAlternateColorCodes('&', "&cYou can only claim one chunk!"));
                        return true;
                    }
                    /* Ensure the chunk player is on is claimable */
                    ClaimableChunk claimableChunk = instance.chunkManager
                            .findClaimableChunkFromChunk(player.getLocation().getChunk());
                    if (claimableChunk != null) {
                        final PlayerChunk pu = instance.chunkManager.claimChunk(
                                instance.playerManager.getPlayerFromId(player.getUniqueId()), claimableChunk);
                        if (pu != null) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    "&aYou've succesfully claimed this chunk!"));
                        }
                        return true;
                    }
                    /** If the chunk is null, try giving the player the nearest chunk. */
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&cThis chunk is not claimable, attempting to find a chunk near you..."));

                    claimableChunk = instance.chunkManager.getNearestClaimableChunk(player.getLocation());
                    if (claimableChunk != null) {
                        final Location loc = instance.chunkManager.getCenterLocationChunk(claimableChunk);
                        loc.add(0.0, 1.0, 0.0);
                        if (loc ==  null){
                            sender.sendMessage("An error has ocurred.");
                            return true;
                        }
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String
                                .format("&aChunk found at %d, %d", claimableChunk.chunkX, claimableChunk.chunkZ)));
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format("t %s %d %d %d %s",
                                player.getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), claimableChunk.chunkWorld.getName()));

                        final PlayerChunk pu = instance.chunkManager.claimChunk(
                                instance.playerManager.getPlayerFromId(player.getUniqueId()), claimableChunk);
                        if (pu != null) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    "&aYou've succesfully claimed this chunk!"));
                        }
                        return true;

                    } else {

                    }
                    break;
                }
                case "claimlist": {
                    sender.sendMessage("Available chunks: ");
                    instance.chunkManager.claimableChunks.keySet().parallelStream().filter(it -> it != null && it.chunkWorld != null).forEach(it -> sender
                            .sendMessage("" + it.chunkX + ", " + it.chunkZ + ", " + it.chunkWorld.getName()));

                    break;
                }
                case "claimadd": {
                    /* Ensure player isn't null */
                    final SurvivalPlayer su = instance.playerManager.getPlayerFromId(player.getUniqueId());
                    if (su == null) {
                        sender.sendMessage(
                                ChatColor.translateAlternateColorCodes('&', "&cAn error has ocurred. please relog"));
                        return true;
                    }
                    final City city = instance.cityManager.isInCity(player.getLocation());
                    if (city == null || (!city.isOwner(player))) {
                        sender.sendMessage(ChatColor.RED + "No permissions");
                        return true;
                    }
                    final IChunk c = instance.chunkManager.getChunkNoType(player.getLocation().getChunk());
                    if (c == null) {
                        final ClaimableChunk claimableChunk = new ClaimableChunk(player.getLocation().getChunk().getX(),
                                player.getLocation().getChunk().getZ(),
                                player.getLocation().getChunk().getWorld().getName());

                        sender.sendMessage(
                                instance.chunkManager.addClaimableChunk(claimableChunk, true) ? "Succesfully added"
                                        : "Can't add this chunk.");
                    } else {
                        sender.sendMessage("Can't add this chunk.");
                    }
                    break;
                }
                case "claimremove": {
                    /* Ensure player isn't null */
                    final SurvivalPlayer su = instance.playerManager.getPlayerFromId(player.getUniqueId());
                    if (su == null) {
                        sender.sendMessage(
                                ChatColor.translateAlternateColorCodes('&', "&cAn error has ocurred. please relog"));
                        return true;
                    }
                    final City city = instance.cityManager.isInCity(player.getLocation());
                    if (city == null || (!city.isOwner(player))) {
                        sender.sendMessage(ChatColor.RED + "No permissions");
                        return true;
                    }
                    final ClaimableChunk c = instance.chunkManager
                            .findClaimableChunkFromChunk(player.getLocation().getChunk());
                    if (c != null) {
                        sender.sendMessage(instance.chunkManager.removeClaimableChunk(c, true) ? "Succesfully removed"
                                : "Can't remove this chunk.");

                    } else {
                        sender.sendMessage("Can't remove this chunk.");
                    }
                    break;
                }
                default: {
                    sender.sendMessage("Command usage: /chunk <check:delete:own> [Player]");
                    break;
                }
            }

        } else if (cmd.getName().equalsIgnoreCase("city")) {
            final Player player = (Player) sender;
            final City city = instance.cityManager.isInCity(player.getLocation());
            if (city == null) {
                return true;
            }
            String owners = "";
            for (UUID uuid : city.owners)
                owners = owners + Bukkit.getOfflinePlayer(uuid).getName() + ", ";
            owners = owners.substring(0, owners.length() - 2);

            String helpers = "";
            for (UUID uuid : city.helpers) {
                if (uuid == null)
                    continue;
                helpers = helpers + Bukkit.getOfflinePlayer(uuid).getName() + ", ";
            }
            helpers = helpers.substring(0, helpers.length() - 2);

            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "\n&b" + city.cityName + "\n&aAdmin(s):&f " + owners + "\n&aHelper(s): &f" + helpers + "  \n  "));

            return true;
        } else if (cmd.getName().equalsIgnoreCase("helper")) {
            if (args.length < 2) {
                sender.sendMessage("Command usage: /helper <add:remove> [Player]");
                return true;
            }
            final Player player = (Player) sender;
            final OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if (target.getName() == null) {
                sender.sendMessage(args[1] + " doesn't exist!");
                return true;
            }
            final City city = instance.cityManager.isInCity(player.getLocation());
            if (city == null) {
                sender.sendMessage(ChatColor.RED + "City is null");
                return true;
            }
            if (!(city.isOwner(player) || player.isOp())) {
                sender.sendMessage(ChatColor.RED + "No permissions!");
                return true;
            }
            switch (args[0].toLowerCase()) {
                case "add": {
                    if (city.isHelper(target.getUniqueId())) {
                        sender.sendMessage(ChatColor.RED + target.getName() + " is already a helper!");
                        return true;
                    }
                    final List<UUID> helpers = new ArrayList<>(Arrays.asList(city.helpers));
                    helpers.add(target.getUniqueId());
                    final UUID[] newArray = new UUID[helpers.size()];
                    for (int i = 0; i < helpers.size(); i++)
                        newArray[i] = helpers.get(i);
                    city.helpers = newArray;
                    final List<String> stlist = new ArrayList<>();
                    helpers.forEach(a -> stlist.add(a.toString()));
                    for (String str : instance.cityFile.getConfigurationSection("Cities").getKeys(false)) {
                        String cityname = instance.cityFile.getString("Cities." + str + ".city-name");
                        if (cityname.equalsIgnoreCase(city.cityName)) {
                            instance.cityFile.set("Cities." + str + ".helpers", stlist);
                            instance.cityFile.save();
                            instance.cityFile.reload();
                            break;
                        }
                    }
                    sender.sendMessage(target.getName() + " has been added as a city helper!");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            "lp user " + target.getName() + " parent set helper");
                    break;
                }
                case "remove": {
                    if (!city.isHelper(target.getUniqueId())) {
                        sender.sendMessage(ChatColor.RED + target.getName() + " is not a helper!");
                        return true;
                    }
                    final List<UUID> helpers = new ArrayList<>(Arrays.asList(city.helpers));
                    helpers.removeIf(
                            it -> it.getMostSignificantBits() == target.getUniqueId().getMostSignificantBits());
                    final UUID[] newArray = new UUID[helpers.size()];
                    for (int i = 0; i < helpers.size(); i++)
                        newArray[i] = helpers.get(i);
                    city.helpers = newArray;
                    final List<String> stlist = new ArrayList<>();
                    helpers.forEach(a -> stlist.add(a.toString()));
                    for (String str : instance.cityFile.getConfigurationSection("Cities").getKeys(false)) {
                        String cityname = instance.cityFile.getString("Cities." + str + ".city-name");
                        if (cityname.equalsIgnoreCase(city.cityName)) {
                            instance.cityFile.set("Cities." + str + ".helpers", stlist);
                            instance.cityFile.save();
                            instance.cityFile.reload();
                            break;
                        }
                    }
                    sender.sendMessage(target.getName() + " has been removed as a city helper!");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            "lp user " + target.getName() + " parent set default");
                    break;
                }
                case "default": {
                    sender.sendMessage("Command usage: /helper <add:remove> [Player]");
                    return true;
                }
            }

        } else if (cmd.getName().equalsIgnoreCase("admin")) {
            if (args.length < 2) {
                sender.sendMessage("Command usage: /admin <add:remove> [Player]");
                return true;
            }
            final Player player = (Player) sender;
            final OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if (target.getName() == null) {
                sender.sendMessage(args[1] + " doesn't exist!");
                return true;
            }
            final City city = instance.cityManager.isInCity(player.getLocation());
            if (city == null) {
                sender.sendMessage(ChatColor.RED + "City is null");
                return true;
            }
            if (!player.isOp()) {
                sender.sendMessage(ChatColor.RED + "No permissions!");
                return true;
            }
            switch (args[0].toLowerCase()) {
                case "add": {
                    if (city.isOwner(target.getUniqueId())) {
                        sender.sendMessage(ChatColor.RED + target.getName() + " is already an owner!");
                        return true;
                    }
                    final List<UUID> owners = new ArrayList<>(Arrays.asList(city.owners));
                    owners.add(target.getUniqueId());
                    final UUID[] newArray = new UUID[owners.size()];
                    for (int i = 0; i < owners.size(); i++)
                        newArray[i] = owners.get(i);
                    city.owners = newArray;
                    final List<String> stlist = new ArrayList<>();
                    owners.forEach(a -> stlist.add(a.toString()));
                    for (String str : instance.cityFile.getConfigurationSection("Cities").getKeys(false)) {
                        String cityname = instance.cityFile.getString("Cities." + str + ".city-name");
                        if (cityname.equalsIgnoreCase(city.cityName)) {
                            instance.cityFile.set("Cities." + str + ".owners", stlist);
                            instance.cityFile.save();
                            instance.cityFile.reload();
                            break;
                        }
                    }
                    sender.sendMessage(target.getName() + " has been added as a city owner!");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            "lp user " + target.getName() + " parent set admin");
                    break;
                }
                case "remove": {
                    if (!city.isOwner(target.getUniqueId())) {
                        sender.sendMessage(ChatColor.RED + target.getName() + " is not an owner!");
                        return true;
                    }
                    final List<UUID> owners = new ArrayList<>(Arrays.asList(city.owners));
                    owners.removeIf(a -> a.getMostSignificantBits() == target.getUniqueId().getMostSignificantBits());
                    final UUID[] newArray = new UUID[owners.size()];
                    for (int i = 0; i < owners.size(); i++)
                        newArray[i] = owners.get(i);
                    city.owners = newArray;
                    final List<String> stlist = new ArrayList<>();
                    owners.forEach(a -> stlist.add(a.toString()));
                    for (String str : instance.cityFile.getConfigurationSection("Cities").getKeys(false)) {
                        String cityname = instance.cityFile.getString("Cities." + str + ".city-name");
                        if (cityname.equalsIgnoreCase(city.cityName)) {
                            instance.cityFile.set("Cities." + str + ".owners", stlist);
                            instance.cityFile.save();
                            instance.cityFile.reload();
                            break;
                        }
                    }
                    sender.sendMessage(target.getName() + " has been removed from city owner!");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            "lp user " + target.getName() + " parent set default");
                    break;
                }
                case "default": {
                    sender.sendMessage("Command usage: /helper <add:remove> [Player]");
                    return true;
                }
            }

        }else if (cmd.getName().equalsIgnoreCase("claim")) {
            Player player = (Player)sender;
            player.performCommand("chunk claim");
         } 
         else if (cmd.getName().equalsIgnoreCase("ally")) {
            final Player player = (Player) sender;
            final SurvivalPlayer su = instance.playerManager.getPlayerFromId(player.getUniqueId());
            if (su == null) {
                player.kickPlayer("An error has ocurred, please relog!");
                return false;
            }
            if (args.length == 0) {
                return false;
            }
            switch (args[0].toLowerCase()) {
                case "add": {
                    if (args.length < 2)
                        return false;
                    final OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                    if (su.isAlly(target.getUniqueId())) {
                        sender.sendMessage(ChatColor.RED + target.getName() + " is already an ally!");
                        return true;
                    }
                    final List<UUID> allies = new ArrayList<>(Arrays.asList(su.allies));
                    allies.add(target.getUniqueId());
                    final UUID[] newArray = new UUID[allies.size()];
                    for (int i = 0; i < allies.size(); i++)
                        newArray[i] = allies.get(i);
                    su.allies = newArray;
                    Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                        try {
                            instance.databaseManager.database.savePlayer(player.getUniqueId());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    sender.sendMessage(
                            ChatColor.GREEN + "You've succesfully added " + target.getName() + " to your allies.");
                    break;
                }
                case "remove": {
                    if (args.length < 2)
                        return false;
                    final OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

                    if (!su.isAlly(target.getUniqueId())) {
                        sender.sendMessage(ChatColor.RED + target.getName() + " is not an ally!");
                        return true;
                    }

                    final List<UUID> allies = new ArrayList<>(Arrays.asList(su.allies));
                    allies.removeIf(a -> a.getMostSignificantBits() == target.getUniqueId().getMostSignificantBits());
                    final UUID[] newArray = new UUID[allies.size()];
                    for (int i = 0; i < allies.size(); i++)
                        newArray[i] = allies.get(i);
                    su.allies = newArray;
                    Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                        try {
                            instance.databaseManager.database.savePlayer(player.getUniqueId());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    sender.sendMessage(
                            ChatColor.GREEN + "You've succesfully removed " + target.getName() + " from your allies.");
                    break;
                }
                case "list": {
                    String str = "";
                    if (su != null) {
                        for (UUID uuid : su.allies) {
                            if (uuid != null && uuid.toString().length() > 6) {
                                str = str + " - " + Bukkit.getOfflinePlayer(uuid).getName() + "\n";
                            }
                        }
                    }
                    sender.sendMessage(ChatColor.GREEN + "Your current allies are: \n" + ChatColor.WHITE + str);

                    break;
                }
                default: {
                    sender.sendMessage(ChatColor.RED + "Command usage: /ally <add:remove> [Player]");
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        switch (cmd.getName().toLowerCase()) {
            case "chunk": {
                if (args.length == 1) {
                    if (args[0].isEmpty()) {
                        return Arrays.asList(helpArray);
                    }
                    final List<String> list = new ArrayList<>();
                    for (String string : helpArray) {
                        // Check if it matches any of the arguments available then autocomplete
                        if (string.toLowerCase().startsWith(args[0].toLowerCase()))
                            list.add(string);
                    }
                    return list;
                }
                break;
            }
            case "city": {
                return Collections.emptyList();
            }
            case "admin":
            case "helper": {
                if (args.length == 1) {
                    if (args[0].isEmpty()) {
                        return Arrays.asList(helperHelpArray);
                    }
                    final List<String> list = new ArrayList<>();
                    for (String string : helperHelpArray) {
                        // Check if it matches any of the arguments available then autocomplete
                        if (string.toLowerCase().startsWith(args[0].toLowerCase()))
                            list.add(string);
                    }
                    return list;
                }
                break;

            }
            case "ally": {
                if (args.length == 1) {
                    if (args[0].isEmpty()) {
                        return Arrays.asList(allyHelpArray);
                    }
                    final List<String> list = new ArrayList<>();
                    for (String string : allyHelpArray) {
                        // Check if it matches any of the arguments available then autocomplete
                        if (string.toLowerCase().startsWith(args[0].toLowerCase()))
                            list.add(string);
                    }
                    return list;
                }
                if (args.length > 1) {
                    switch (args[0].toLowerCase()) {
                        case "remove":
                        case "add":
                            return null;
                        default:
                            return Collections.emptyList();
                    }
                }
                break;
            }
        }

        return null;
    }

}
