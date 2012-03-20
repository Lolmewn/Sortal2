package nl.lolmewn.sortal;

import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

/**
 *
 * @author Sybren
 */
public class EventListener implements Listener {

    private Main plugin;

    private Main getPlugin() {
        return this.plugin;
    }
    
    private Localisation getLocalisation(){
        return this.getPlugin().getSettings().getLocalisation();
    }

    public EventListener(Main main) {
        this.plugin = main;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        for (int i = 0; i < event.getLines().length; i++) {
            if (this.getPlugin().getSettings().isDebug()) {
                this.getPlugin().getLogger().log(Level.INFO, String.format("[Debug] Checking line "
                        + " %s of the sign", i));
            }
            if (event.getLine(i).toLowerCase().contains("[sortal]")
                    || event.getLine(i).toLowerCase().contains(this.getPlugin().getSettings().getSignContains())) {
                if (!event.getPlayer().hasPermission("sortal.placesign")) {
                    event.getPlayer().sendMessage(this.getLocalisation().getNoPerms());
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerHitSign(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            Block b = event.getClickedBlock();
            if (b.getType().equals(Material.SIGN) || b.getType().equals(Material.SIGN_POST)
                    || b.getType().equals(Material.WALL_SIGN)) {
                //It's a sign
                Sign s = (Sign) b.getState();
                if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                    if (sortalSign(s, event.getPlayer())) {
                        event.setCancelled(true);
                        return;
                    }
                }
                int found = -1;
                for (int i = 0; i < s.getLines().length; i++) {
                    if (s.getLine(i).toLowerCase().contains("[sortal]")
                            || s.getLine(i).toLowerCase().contains(this.getPlugin().getSettings().getSignContains())) {
                        //It's a sortal sign
                        found = i;
                        break;
                    }
                }
                if (found == -1) {
                    if (!event.getPlayer().hasPermission("sortal.warp")) {
                        event.getPlayer().sendMessage(this.getLocalisation().getNoPerms());
                        event.setCancelled(true);
                        return;
                    }
                    //no [Sortal] found, may be registered
                    //TODO do registered signs check
                    event.setCancelled(true); //Cancel, don't place block.
                    return;
                }
                if (!event.getPlayer().hasPermission("sortal.warp")) {
                    event.getPlayer().sendMessage(this.getLocalisation().getNoPerms());
                    event.setCancelled(true);
                    return;
                }
                String nextLine = s.getLine(found + 1);
                if (nextLine == null || nextLine.equals("")) {
                    //Well, that didn't really work out well..
                    event.getPlayer().sendMessage(this.getLocalisation().getErrorInSign());
                    event.setCancelled(true);
                    return;
                }
                if(nextLine.contains("w:")){
                    //It's a warp
                    String warp = nextLine.split(":")[1];
                    if(!this.getPlugin().getWarpManager().hasWarp(warp)){
                        event.getPlayer().sendMessage(this.getLocalisation().getWarpNotFound(warp));
                        event.setCancelled(true);
                        return;
                    }
                    Warp w = this.getPlugin().getWarpManager().getWarp(warp);
                    if(!this.getPlugin().pay(event.getPlayer(), w.getPrice())){
                        event.setCancelled(true);
                        return;
                    }
                    event.getPlayer().teleport(w.getLocation(), TeleportCause.PLUGIN);
                    event.getPlayer().sendMessage(this.getLocalisation().getPlayerTeleported(warp));
                    event.setCancelled(true);
                    return;
                }
                if(nextLine.contains(",")){
                    String[] split = nextLine.split(",");
                    World w;
                    int add = 0;
                    if(split.length == 3){
                        w = event.getPlayer().getWorld();
                    }else{
                        w = this.getPlugin().getServer().getWorld(split[0]);
                        if(w == null){
                            event.getPlayer().sendMessage(this.getLocalisation().getErrorInSign());
                            event.setCancelled(true);
                            return;
                        }
                        add = 1;
                    }
                    int x = Integer.parseInt(split[0 + add]), y = Integer.parseInt(split[1 + add]),
                            z = Integer.parseInt(split[2 + add]);
                    Location dest = new Location (w, x, y, z, event.getPlayer().getLocation().getYaw(), event.getPlayer().getLocation().getPitch());
                    event.getPlayer().teleport(dest, TeleportCause.PLUGIN);
                    event.getPlayer().sendMessage(this.getLocalisation().getPlayerTeleported(
                            dest.getBlockX() + ", " + dest.getBlockY() + ", " + dest.getBlockZ()));
                    event.setCancelled(true);
                    return;
                }
                event.getPlayer().sendMessage(this.getLocalisation().getErrorInSign());
                event.setCancelled(true);
            }
        }
    }

    private boolean sortalSign(Sign s, Player player) {
        return false;
    }
}
