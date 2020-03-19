package me.infinityz.cities;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * CityLeftEvent
 */
public class CityChangeEvent extends Event{
    private static final HandlerList handlers = new HandlerList();
    public Player player;
    public City from, to;

    public CityChangeEvent (Player player, City fromCity, City toCity){
        super(true);
        this.player = player;
        this.from = fromCity;
        this.to = toCity;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    
}