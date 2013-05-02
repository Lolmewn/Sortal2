/*
 *  Copyright 2013 Lolmewn <info@lolmewn.nl>.
 */

package nl.lolmewn.sortal.api;

import nl.lolmewn.sortal.Warp;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */
public class SortalWarpCreateEvent extends Event implements Cancellable{
    
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    
    private Player sender;
    private String name;
    private Location location;
    
    public SortalWarpCreateEvent(Player sender, String name, Location location) {
        this.sender = sender;
        this.name = name;
        this.location = location;
    }
           
    public Player getCommandSender(){
        return sender;
    }
    
    public String getName(){
        return name;
    }
    
    public Location getLocation(){
        return location;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList(){
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean bln) {
        cancelled = bln;
    }

}
