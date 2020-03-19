package me.infinityz.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import me.infinityz.SurvivalUniverse;
import me.infinityz.chunks.IChunk;
import me.infinityz.chunks.types.PlayerChunk;
import net.md_5.bungee.api.ChatColor;

/**
 * ChunkCommands
 */
public class ChunkCommands implements CommandExecutor, TabCompleter {

    SurvivalUniverse instance;
    String[] helpArray = {"check", "delete", "own"};

    public ChunkCommands(SurvivalUniverse instance) {
        this.instance = instance;
    }

    @SuppressWarnings("all")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equals("chunk")) {
            if(!(sender instanceof Player)){
                sender.sendMessage("Console can't use chunk.");
                return true;
            }
            Player player = (Player)sender;
            switch(args[0].toLowerCase()){
                case "own":{
                    if(args.length > 1){
                        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                        final Chunk c = player.getLocation().getChunk();
                        IChunk iChunk = instance.chunkManager.findIChunkfromChunk(c);
                        if(iChunk == null){
                            PlayerChunk newIChunk = new PlayerChunk(target.getUniqueId(), c.getWorld().getName(), c.getX(), c.getZ());
                            instance.chunkManager.ownedChunksMap.put(newIChunk, target.getUniqueId());
                            String keyString = "chunks." + target.getUniqueId() + "." + newIChunk.toString();
                            instance.chunksFile.set(keyString + ".world", newIChunk.chunkWorld.getName());     
                            instance.chunksFile.set(keyString + ".x-coordinate", newIChunk.chunkX);     
                            instance.chunksFile.set(keyString + ".z-coordinate", newIChunk.chunkZ);
                            instance.chunksFile.save();
                            instance.chunksFile.reload();
                            sender.sendMessage("This chunk has been given to " + target.getName());

                        }else{
                            sender.sendMessage("This chunk is already owned.");
                        }

                        
                    }else{
                        sender.sendMessage("Command usage: /chunk <check:delete:own> [Player]");
                        return true;
                    }
                    break;
                }
                case "delete":{
                    break;
                }
                case "check":{
                    final Chunk c = player.getLocation().getChunk();
                    IChunk iChunk = instance.chunkManager.findIChunkfromChunk(c);
                    if(iChunk == null){
                        sender.sendMessage(ChatColor.RED + "This chunk is not owned!");
                        return true;
                    }else{
                        if(iChunk instanceof PlayerChunk){
                            PlayerChunk chunk = (PlayerChunk) iChunk;
                            sender.sendMessage("The owner is " + chunk.owner_last_known_name);
                            
                        }else{
                            sender.sendMessage("Unsupported, city chunks are not yet working!");
                        }
                        //Check for the chunk type and then weather city or owned and in such case return the owner.
                    }
                    break;
                }
                default:{
                    sender.sendMessage("Command usage: /chunk <check:delete:own> [Player]");
                    break;
                }
            }

        }
        else if (cmd.getName().equals("city")) {
            Player player = (Player) sender;
            instance.cityManager.cities.parallelStream().forEach(city->{
                Boolean bol =  instance.cityManager.isInRectangle(player.getLocation(), city);
                Bukkit.broadcastMessage(city.cityName+": " + bol.toString());
            });
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        switch(cmd.getName().toLowerCase()){
            case "chunk":{
                if(args.length == 1){            
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
            case "city":{
                return Collections.emptyList();
            }
        }

        return null;
    }

}