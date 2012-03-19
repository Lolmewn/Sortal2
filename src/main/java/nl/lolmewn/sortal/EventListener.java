package nl.lolmewn.sortal;

import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

/**
 *
 * @author Sybren
 */
public class EventListener implements Listener{
    
    private Main plugin;
    
    private Main getPlugin(){
        return this.plugin;
    }
    
    public EventListener(Main main){
        this.plugin = main;
    }
    
    public void onSignChange(SignChangeEvent event){
        for(int i = 0; i < event.getLines().length; i++){
            if(event.getLine(i).toLowerCase().contains("[sortal]") || 
                    event.getLine(i).toLowerCase().contains(this.getPlugin().getSettings().getSignContains())){
                if(!event.getPlayer().hasPermission("sortal.placesign")){
                    event.getPlayer().sendMessage(this.getPlugin().getSettings().getLocalisation().getNoPerms());
                    event.setCancelled(true);
                    return;
                }
                
            }
        }
    }
    
}
