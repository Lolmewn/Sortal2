/*
 *  Copyright 2013 Lolmewn <info@lolmewn.nl>.
 */

package nl.lolmewn.sortal.api;

import nl.lolmewn.sortal.Warp;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */
public class SortalWarpDeleteEvent extends Event implements Cancellable{
    
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    
    private CommandSender sender;
    private Warp warp;
    
    public SortalWarpDeleteEvent(CommandSender sender, Warp warp) {
        this.sender = sender;
        this.warp = warp;
    }
    
    public CommandSender getCommandSender(){
        return sender;
    }
    
    public Warp getWarp(){
        return warp;
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
