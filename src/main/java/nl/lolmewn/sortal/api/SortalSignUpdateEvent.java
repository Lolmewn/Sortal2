/*
 *  Copyright 2013 Lolmewn <info@lolmewn.nl>.
 */

package nl.lolmewn.sortal.api;

import nl.lolmewn.sortal.SignInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */
public class SortalSignUpdateEvent extends Event implements Cancellable{
    
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    
    public enum SortalSignUpdateType{OWNER, COST, WARP_REGISTER, WARP_UNREGISTER, USES, PRIVATE, PRIVATE_USERS}

    private SortalSignUpdateType type;
    private Player player;
    private SignInfo info;
    private Object newValue;
    
    public SortalSignUpdateEvent(SortalSignUpdateType type, Player player, SignInfo info, Object newValue) {
        this.type = type;
        this.player = player;
        this.info = info;
        this.newValue = newValue;
    }

    public SignInfo getSignInfo() {
        return info;
    }

    public Object getNewValue() {
        return newValue;
    }

    public Player getPlayer() {
        return player;
    }

    public SortalSignUpdateType getType() {
        return type;
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
