package nl.lolmewn.sortal;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Lolmewn
 */
public class WarpManager {
    
    private Main plugin;
    
     File warpFile = new File("plugins" + File.separator + 
                "Sortal" + File.separator + "warps.yml");
    
    private HashMap<String, Warp> warps = new HashMap<String, Warp>();
    
    private Main getPlugin(){
        return this.plugin;
    }
    
    public WarpManager(Main m){
        this.plugin = m;
        this.loadWarps();
        this.loadSigns();
    }

    private void loadWarps() {
        if(this.getPlugin().getSettings().useMySQL()){
            ResultSet set = this.getPlugin().getMySQL().executeQuery("SELECT * FROM " 
                    + this.getPlugin().getWarpTable());
            if(set == null){
                this.getPlugin().getLogger().severe("Something is wrong with your MySQL database!");
                this.getPlugin().getLogger().severe("Plugin is disabling!");
                this.getPlugin().getServer().getPluginManager().disablePlugin(this.getPlugin());
                return;
            }
            try {
                while(set.next()){
                    this.addWarp(set.getString("name"), new Location(
                            this.getPlugin().getServer().getWorld(set.getString("world")),
                            set.getInt("x"), set.getInt("y"), set.getInt("z"),
                            set.getFloat("yaw"), set.getFloat("pitch")));
                }
            } catch (SQLException ex) {
                this.getPlugin().getLogger().log(Level.SEVERE, null, ex);
            }
            return;
        }
       
        YamlConfiguration c = YamlConfiguration.loadConfiguration(warpFile);
        for(String key : c.getConfigurationSection("").getKeys(false)){
            this.addWarp(key, new Location(this.getPlugin().getServer().getWorld(c.getString(key + ".world"))
                    , c.getDouble("x"), c.getDouble("y"), c.getDouble("z"), 
                    (float)c.getDouble("yaw"), (float)c.getDouble("pitch")));
        }
        this.getPlugin().getLogger().log(Level.INFO, String.format("Warps loaded: %s", this.warps.size()));
    }

    private void loadSigns() {
        
    }
    
    public Warp addWarp(String name, Location loc){
        Warp w = this.warps.put(name, new Warp(name, loc));
        if(this.getPlugin().getSettings().useMySQL()){
            w.save(this.getPlugin().getMySQL(), this.getPlugin().getWarpTable());
        }else{
            w.save(this.warpFile);
        }
        return w;
    }
    
    public Warp removeWarp(String name){
        if(!this.hasWarp(name)){
            return null;
        }
        if(this.getPlugin().getSettings().useMySQL()){
            this.warps.get(name).delete(this.getPlugin().getMySQL(), this.getPlugin().getWarpTable());
        }else{
            this.warps.get(name).delete(this.warpFile);
        }
        return this.warps.remove(name);
    }
    
    public boolean hasWarp(String name){
        return this.warps.containsKey(name);
    }
    
    public Warp getWarp(String name){
        return this.warps.get(name);
    }
    
    public void saveData(){
        for(String name : this.warps.keySet()){
            if(this.getPlugin().getSettings().useMySQL()){
                this.warps.get(name).save(this.getPlugin().getMySQL()
                        , this.getPlugin().getWarpTable());
            }else{
                this.warps.get(name).save(warpFile);
            }
        }
    }
    
    public Set<Warp> getWarps(){
        Set<Warp> warpSet = new HashSet<Warp>();
        for(String warp : this.warps.keySet()){
            warpSet.add(this.warps.get(warp));
        }
        return warpSet;
    }
    
}
