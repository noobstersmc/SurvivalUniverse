package me.infinityz.listeners;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import me.infinityz.SurvivalUniverse;
import me.infinityz.chunks.IChunk;
import me.infinityz.chunks.Tristate;
import me.infinityz.chunks.types.PlayerChunk;
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

    /*
     * TODO: Detect when player dies in PVP Chunk and process whether or not they
     * should drop their loot and spawn them in the nearest safe chunk.
     */

    public GlobalListeners(SurvivalUniverse instance) {
        this.instance = instance;
    }

    public static Player getPlayerDamagerEntityEvent(EntityDamageByEntityEvent e) {
        return e.getDamager() instanceof Player ? (Player) e.getDamager()
                : (e.getDamager() instanceof Projectile ? (((Projectile) e.getDamager()).getShooter() instanceof Player
                        ? (Player) ((Projectile) e.getDamager()).getShooter()
                        : null) : null);
    }

    @EventHandler
    public void cacheData(AsyncPlayerPreLoginEvent e) {
        try {
            instance.databaseManager.database.loadPlayer(e.getUniqueId());
        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }
    /* Global events, for both chunk and cities */

    @EventHandler
    public void onPlayerLectern(PlayerTakeLecternBookEvent e) {
        e.setCancelled(maybeInCityOrChunk(e.getLectern().getLocation(), e.getPlayer()));
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        switch (e.getEntityType()) {
            case ARMOR_STAND:
            case ITEM_FRAME:
            case PAINTING: {
                if (e.getCause() == DamageCause.ENTITY_EXPLOSION || e.getCause() == DamageCause.BLOCK_EXPLOSION
                        || e.getCause() == DamageCause.PROJECTILE) {
                    e.setCancelled(maybeInCityOrChunk(e.getEntity().getLocation()));
                }
            }
            default:
                // Rideable entity is unbreakable when they have a player as an entity
                // Update: Changed code from legacy for loop to lambda.
                e.setCancelled(e.getEntity().getPassengers().stream().filter(it -> (it instanceof Player)).findFirst()
                        .isPresent());
                break;
        }

    }

    @EventHandler
    public void onEntity(PlayerInteractAtEntityEvent e) {
        if (e.getRightClicked() == null)
            return;
        if (e.getRightClicked().getType() != EntityType.ARMOR_STAND)
            return;
        e.setCancelled(maybeInCityOrChunk(e.getRightClicked().getLocation(), e.getPlayer()));
    }

    @EventHandler
    public void onSpread(BlockSpreadEvent e) {
        if (e.getSource().getType() == Material.FIRE) {
            e.setCancelled(maybeInCityOrChunk(e.getBlock().getLocation()));
        }
    }

    /* Issue #3 - Problem 1, feature added. */
    @EventHandler
    public void onSpread(BlockIgniteEvent e) {
        if (e.getCause() == IgniteCause.ARROW) {
            if (e.getIgnitingEntity() instanceof Projectile) {
                Projectile proj = (Projectile) e.getIgnitingEntity();
                if (proj.getShooter() != null && proj.getShooter() instanceof Player) {
                    Player player = (Player) proj.getShooter();
                    boolean can = maybeInCityOrChunk(e.getBlock().getLocation(), player);
                    e.setCancelled(can);
                    if (can) {
                        Bukkit.getScheduler().runTaskLater(instance, () -> {
                            player.updateInventory();
                        }, 10);
                    }
                }
            }

        }
    }

    @EventHandler
    public void onSpread(BlockBurnEvent e) {
        e.setCancelled(maybeInCityOrChunk(e.getBlock().getLocation()));
    }

    @EventHandler
    public void onDamageByPlayer(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player player = (Player) e.getDamager();
            switch (e.getEntity().getType()) {
                case ARMOR_STAND:
                case ITEM_FRAME: {
                    e.setCancelled(maybeInCityOrChunk(e.getEntity().getLocation(), player));
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onHanging(HangingBreakEvent e) {
        if (e.getCause() == RemoveCause.PHYSICS || e.getCause() == RemoveCause.ENTITY)
            return;

        if (instance.chunkManager.findIChunkfromChunk(e.getEntity().getLocation().getChunk()) != null
                || instance.cityManager.isInCity(e.getEntity().getLocation()) != null) {
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onHangingEntity(HangingBreakByEntityEvent e) {
        if (!(e.getRemover() instanceof Player)) {
            e.setCancelled(maybeInCityOrChunk(e.getEntity().getLocation()));
            return;
        }
        final Player player = (Player) e.getRemover();
        e.setCancelled(maybeInCityOrChunk(e.getEntity().getLocation(), player));

    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBreakCity(BlockBreakEvent e) {
        e.setCancelled(e.getPlayer() != null ? maybeInCityOrChunk(e.getBlock().getLocation(), e.getPlayer())
                : maybeInCityOrChunk(e.getBlock().getLocation()));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlaceCity(BlockPlaceEvent e) {
        if (e.getBlock().getType() == Material.LECTERN)
            return;
        e.setCancelled(e.getPlayer() != null ? maybeInCityOrChunk(e.getBlock().getLocation(), e.getPlayer())
                : maybeInCityOrChunk(e.getBlock().getLocation()));
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        if (Bukkit.getOnlinePlayers().size() >= 50 && !e.getPlayer().hasPermission("reserved.slot"))
            /**
             * &fServer is full! \n &aGet your rank at survivalrip.buycraft.net \n &bAnd
             * don`t forget to follow us on Twitter @Survival_U
             **/
            /** &cMantenimiento\n &fDiscord discord.gg/6PzHNyt\n&6SOLO VIP **/
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.translateAlternateColorCodes('&',
                    "&fServer is full! \n &aGet your rank at survivalrip.buycraft.net \n &bAnd don`t forget to follow us on Twitter @Survival_U"));
    }

    @EventHandler
    public void onPing(ServerListPingEvent e) {
        e.setMaxPlayers(30);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void interactEvent(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null || e.getClickedBlock().getType() == Material.AIR)
            return;
        switch (e.getClickedBlock().getType()) {
            case ENDER_CHEST:
            case LECTERN:
            case BELL:
            case STONECUTTER:
            case LOOM:
            case FLETCHING_TABLE:
            case CARTOGRAPHY_TABLE:
            case SMITHING_TABLE:
            case GRINDSTONE:
            case NOTE_BLOCK:
            case TRIPWIRE_HOOK:
            case TRIPWIRE:
            case CRAFTING_TABLE:
                return;
            default:
                if (e.getClickedBlock().getType().toString().toLowerCase().contains("plate")
                        || e.getClickedBlock().getType().toString().toLowerCase().endsWith("bed"))
                    return;
                /* Fix Issue #6 - 2 */
                if (e.getItem() != null && e.getItem().getType() == Material.ENDER_PEARL) {
                    e.setUseInteractedBlock(Result.DENY);
                    e.setUseItemInHand(Result.ALLOW);
                    return;
                } // Maybe delete this?
                e.setUseItemInHand(Result.DEFAULT);
                final IChunk chunk = instance.chunkManager.findIChunkfromChunk(e.getClickedBlock().getChunk());
                if (chunk != null) {
                    e.setUseInteractedBlock(
                            chunk.shouldInteract(e.getPlayer().getUniqueId(), e.getClickedBlock().getLocation())
                                    ? Result.ALLOW
                                    : Result.DENY);
                } else {
                    final City city = instance.cityManager.isInCity(e.getClickedBlock().getLocation());
                    e.setUseInteractedBlock(
                            city.isHelper(e.getPlayer()) || city.isOwner(e.getPlayer()) ? Result.ALLOW : Result.DENY);
                }
                // TODO: Write a method to allow placing against block that's not yours.
                // if(e.getAction() == Action.RIGHT_CLICK_BLOCK);
                return;
        }
    }

    @EventHandler
    public void onChunkChange(PlayerChangeChunkEvent e) {
        instance.scoreboardManager.scoreboardHashMap.get(e.player.getUniqueId()).updateLine(2,
                ChatColor.GREEN + "Chunk: " + ChatColor.WHITE + getChunkCoord(e.to));
        instance.scoreboardManager.scoreboardHashMap.get(e.player.getUniqueId()).updateLine(3,
                ChatColor.GREEN + "Zone: " + ChatColor.WHITE + getZone(e.toLocation));

    }

    @EventHandler
    public void onCityChanged(CityChangeEvent e) {
        if (e.to == e.from)
            return;
        if (e.from != null) {
            e.player.sendMessage(ChatColor.RED + "You left " + e.from.cityName + ".");
        }
        if (e.to != null) {
            e.player.sendMessage(ChatColor.GREEN + "You entered " + e.to.cityName + ".");
        }

        instance.scoreboardManager.scoreboardHashMap.get(e.player.getUniqueId()).updateLine(3,
                ChatColor.GREEN + "Zone: " + ChatColor.WHITE + getZone(e.player.getLocation()));
    }

    /*
     * Loads the scoreboard and sets it to easy access. Possibly do some more
     * calculations here.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        final Player player = e.getPlayer();
        instance.playerManager.survivalPlayerMap.put(player.getUniqueId(), new SurvivalPlayer(player.getUniqueId()));
        FastBoard fb = new FastBoard(player);

        fb.updateTitle(ChatColor.translateAlternateColorCodes('&', "  &b&lSurvival Universe  "));

        fb.updateLines(ChatColor.GREEN + "World: " + ChatColor.WHITE + player.getWorld().getName(), " ",
                ChatColor.GREEN + "Chunk: " + ChatColor.WHITE + getChunkCoord(player.getLocation()),
                ChatColor.GREEN + "Zone: " + ChatColor.WHITE + getZone(player.getLocation()), "  ",
                ChatColor.GREEN + "Players: " + ChatColor.WHITE + Bukkit.getOnlinePlayers().size(), "   ",
                ChatColor.AQUA + "  survival.rip  ");

        instance.scoreboardManager.scoreboardHashMap.put(player.getUniqueId(), fb);

    }

    String capitalizeFirst(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    String getZone(Location loc) {
        final IChunk chunk = instance.chunkManager.getChunkNoType(loc.getChunk());
        if (chunk != null) {
            switch (chunk.getClass().getSimpleName().toLowerCase()) {
                case "playerchunk":
                    return "Private";
                case "claimablechunk":
                    return "Claimable";
                default:
                    break;
            }
            return capitalizeFirst(getClass().getSimpleName().replace("Chunk", ""));
        }
        final City ci = instance.cityManager.isInCity(loc);
        if (ci != null)
            return "City";
        return "Free";
    }

    String getChunkCoord(Location loc) {
        return loc.getChunk().getX() + ", " + loc.getChunk().getZ();
    }

    String getChunkCoord(Chunk loc) {
        return loc.getX() + ", " + loc.getZ();
    }

    /* Clears the cache from the player that has left */
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        final Player player = e.getPlayer();
        instance.playerManager.survivalPlayerMap.remove(player.getUniqueId());
        instance.scoreboardManager.scoreboardHashMap.remove(player.getUniqueId());
        instance.cityManager.lastKnownCityMap.remove(player.getUniqueId());

    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent e) {
        instance.scoreboardManager.scoreboardHashMap.get(e.getPlayer().getUniqueId()).updateLine(0,
                ChatColor.GREEN + "World: " + ChatColor.WHITE + e.getPlayer().getWorld().getName());

    }

    /* Handles the city change event */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (e.isCancelled())
            return;
        final Chunk to = e.getTo().getChunk();
        final Chunk from = e.getFrom().getChunk();
        if (to.getX() != from.getX() || to.getZ() != from.getZ()) {
            final Player player = e.getPlayer();
            Bukkit.getPluginManager().callEvent(new PlayerChangeChunkEvent(player, from, to, e.getTo()));
        }
        if (e.getTo().getBlockX() != e.getFrom().getBlockX() || e.getTo().getBlockZ() != e.getFrom().getBlockZ()) {

            Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                City player_city = instance.cityManager.lastKnownCityMap.get(e.getPlayer().getUniqueId());
                City found_city = null;
                for (City city : instance.cityManager.cities) {
                    if (instance.cityManager.isInRectangle(e.getTo(), city)) {
                        found_city = city;
                        break;
                    }
                }
                if (player_city == found_city)
                    return;
                Bukkit.getPluginManager().callEvent(new CityChangeEvent(e.getPlayer(), player_city, found_city));
                instance.cityManager.lastKnownCityMap.put(e.getPlayer().getUniqueId(), found_city);
            });

        }
    }

    /* Handles the city change event */
    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.isCancelled())
            return;
        final Chunk to = e.getTo().getChunk();
        final Chunk from = e.getFrom().getChunk();
        if (to.getX() != from.getX() || to.getZ() != from.getZ()) {
            final Player player = e.getPlayer();
            Bukkit.getPluginManager().callEvent(new PlayerChangeChunkEvent(player, from, to, e.getTo()));
        }
        if (e.getTo().getBlockX() != e.getFrom().getBlockX() || e.getTo().getBlockZ() != e.getFrom().getBlockZ()) {

            Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                City player_city = instance.cityManager.lastKnownCityMap.get(e.getPlayer().getUniqueId());
                City found_city = null;
                for (City city : instance.cityManager.cities) {
                    if (instance.cityManager.isInRectangle(e.getTo(), city)) {
                        found_city = city;
                        break;
                    }
                }
                if (player_city == found_city)
                    return;
                Bukkit.getPluginManager().callEvent(new CityChangeEvent(e.getPlayer(), player_city, found_city));
                instance.cityManager.lastKnownCityMap.put(e.getPlayer().getUniqueId(), found_city);
            });

        }

    }

    /* Handles the player's pvp booleans */
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity().getType() != EntityType.PLAYER)
            return;

        final Player dmgr = getPlayerDamagerEntityEvent(e);
        if (dmgr != null) {
            switch (instance.globalPvp) {
                case FALSE:
                    e.setCancelled(true);
                    break;
                case TRUE:
                    e.setCancelled(false);
                    break;
                case UNKNOWN:

                    final SurvivalPlayer survivalDamagerPlayer = instance.playerManager.survivalPlayerMap
                            .get(dmgr.getUniqueId()),
                            survivalPlayer = instance.playerManager.survivalPlayerMap.get(e.getEntity().getUniqueId());
                    /* Check if they are in pvp or safe chunks */
                    if (dmgr.getChunk() == e.getEntity().getChunk()) {
                        final IChunk c = instance.chunkManager.findIChunkfromChunk(dmgr.getChunk());
                        if (c.pvpMode == Tristate.TRUE) {
                            // Pvp chunk implied
                            e.setCancelled(false);
                            return;
                        } else if (c.pvpMode == Tristate.FALSE) {
                            // Safe chunk implied
                            e.setCancelled(true);
                            return;
                        }
                    } else {
                        final IChunk damagerChunk = instance.chunkManager.findIChunkfromChunk(dmgr.getChunk()),
                                entityChunk = instance.chunkManager.findIChunkfromChunk(e.getEntity().getChunk());
                        if (damagerChunk != null) {
                            if (entityChunk != null) {
                                if (damagerChunk.pvpMode == Tristate.FALSE || entityChunk.pvpMode == Tristate.FALSE) {
                                    e.setCancelled(true);
                                    return;
                                }

                            } else if ((survivalPlayer != null && !survivalPlayer.pvp)
                                    || damagerChunk.pvpMode == Tristate.FALSE) {
                                e.setCancelled(true);
                                return;
                            }
                        } else {
                            if (survivalDamagerPlayer != null && survivalDamagerPlayer.pvp) {
                                if (entityChunk != null) {
                                    if (entityChunk.pvpMode == Tristate.FALSE || !survivalDamagerPlayer.pvp) {
                                        e.setCancelled(true);
                                        return;
                                    }

                                } else if (entityChunk == null && (survivalPlayer != null && !survivalPlayer.pvp)) {
                                    e.setCancelled(true);
                                    return;
                                }

                            } else {
                                e.setCancelled(true);
                                return;
                            }

                        }
                    }
                    break;
            }
        }
    }

    // TODO: Allow players to use buckets in pvp chunks
    /* Issue #3 - Water and Lava buckets - Start */
    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        e.setCancelled(maybeInCityOrChunk(e.getBlockClicked().getLocation(), e.getPlayer(), true));

    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent e) {
        e.setCancelled(maybeInCityOrChunk(e.getBlockClicked().getLocation(), e.getPlayer(), true));
    }
    /* Issue #3 - Water and Lava buckets - End here */

    boolean maybeInCityOrChunk(Location location, Player player) {
        boolean bol = false;
        final City city = instance.cityManager.isInCity(location);
        if (city != null) {
            if (!(city.isOwner(player) || city.isHelper(player))) {
                bol = true;
            }
        }
        final PlayerChunk chunk = (PlayerChunk) instance.chunkManager.findIChunkfromChunk(location.getChunk());
        if (chunk != null) {
            if (chunk.owner.getMostSignificantBits() == player.getUniqueId().getMostSignificantBits()) {
                bol = false;
                return bol;
            }
            final SurvivalPlayer su = instance.playerManager.getPlayerFromId(chunk.owner);
            if (su != null) {
                if (su.isAlly(player.getUniqueId())) {
                    bol = false;
                }
            }

        }
        return bol;
    }

    /* Fix Issue #6 - 1 */
    boolean maybeInCityOrChunk(Location location, Player player, boolean Ignore) {
        boolean bol = false;
        boolean helper = false;
        final City city = instance.cityManager.isInCity(location);
        if (city != null) {
            helper = city.isHelper(player);
            if (!(city.isOwner(player) || helper)) {
                bol = true;
            }
        }
        final PlayerChunk chunk = (PlayerChunk) instance.chunkManager.findIChunkfromChunk(location.getChunk());
        if (chunk != null) {
            if (helper)
                bol = true;
            if (chunk.owner.getMostSignificantBits() == player.getUniqueId().getMostSignificantBits()) {
                bol = false;
                return bol;
            }
            final SurvivalPlayer su = instance.playerManager.getPlayerFromId(chunk.owner);
            if (su != null) {
                if (su.isAlly(player.getUniqueId())) {
                    bol = false;
                    // Allies should still be able to interact
                }
            }

        }
        return bol;
    }

    boolean maybeInCityOrChunk(Location location) {
        return instance.chunkManager.findIChunkfromChunk(location.getChunk()) != null
                || instance.cityManager.isInCity(location) != null;
    }

    /* Issue #4 - Feature request - Make charged creepers drop the player's head. */
    @SuppressWarnings("all")
    @EventHandler
    public void onCreeperDamage(EntityDamageByEntityEvent e) {
        if (e.isCancelled())
            return;
        if (e.getEntity().getType() != EntityType.PLAYER)
            return;
        if (e.getDamager() == null)
            return;
        if (e.getDamager().getType() != EntityType.CREEPER)
            return;
        final Player player = (Player) e.getEntity();
        if (player.getHealth() + player.getAbsorptionAmount() - e.getFinalDamage() <= 0) {
            final Creeper creeper = (Creeper) e.getDamager();
            if (creeper.isPowered()) {
                final ItemStack stack = new ItemStack(Material.PLAYER_HEAD, 1);
                final SkullMeta meta = (SkullMeta) stack.getItemMeta();
                final Location playerLocation = player.getLocation().clone();
                meta.setOwner(player.getName());
                stack.setItemMeta(meta);
                Bukkit.getScheduler().runTaskLater(instance, () -> {
                    playerLocation.getWorld().dropItemNaturally(playerLocation, stack);
                }, 4);
            }
        }

    }

    @EventHandler
    public void onPreCommand(ServerCommandEvent w) {

        if (w.getSender() instanceof BlockCommandSender) {
            BlockCommandSender sender = (BlockCommandSender) w.getSender();
            for (String arg : w.getCommand().split(" ")) {
                switch (arg.toLowerCase()) {
                    case "@a": {
                        Bukkit.getOnlinePlayers().forEach(all -> {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                    w.getCommand().replace("@a", all.getName()));
                        });
                        break;
                    }
                    case "@p": {
                        try {
                            final Collection<Entity> collection = sender.getBlock().getWorld().getNearbyEntities(
                                    sender.getBlock().getLocation(), 1.0, 255.0, 1.0,
                                    it -> it.getType() == EntityType.PLAYER);
                            final Entity first = collection.iterator().hasNext() ? collection.iterator().next() : null;
                            if (first == null)
                                return;
                            final Player player = (Player) first;
                            if (player != null)
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                        w.getCommand().replace("@p", player.getName()));

                        } catch (Exception e) {
                        }
                        break;
                    }
                    default:
                        break;
                }

            }
        }
    }
}
