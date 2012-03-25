package nl.lolmewn.sortal;

import java.io.File;
import java.io.IOException;
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
    File warpFile = new File("plugins" + File.separator
            + "Sortal" + File.separator + "warps.yml");
    File signFile = new File("plugins" + File.separator
            + "Sortal" + File.separator + "signs.yml");
    private HashMap<String, Warp> warps = new HashMap<String, Warp>();
    private HashMap<String, SignInfo> signs = new HashMap<String, SignInfo>();

    private Main getPlugin() {
        return this.plugin;
    }

    public WarpManager(Main m) {
        this.plugin = m;
        try {
            this.loadWarps();
            this.loadSigns();
        } catch (Exception e) {
            //Be ready to do stuff, even if loading fails
        }
    }

    private void loadWarps() {
        if (this.getPlugin().getSettings().useMySQL()) {
            ResultSet set = this.getPlugin().getMySQL().executeQuery("SELECT * FROM "
                    + this.getPlugin().getWarpTable());
            if (set == null) {
                this.getPlugin().getLogger().severe("Something is wrong with your MySQL database!");
                this.getPlugin().getLogger().severe("Plugin is disabling!");
                this.getPlugin().getServer().getPluginManager().disablePlugin(this.getPlugin());
                return;
            }
            try {
                while (set.next()) {
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
        if (!this.warpFile.exists()) {
            try {
                this.warpFile.createNewFile();
            } catch (IOException ex) {
                this.getPlugin().getLogger().log(Level.SEVERE, null, ex);
            } finally {
                return;
            }
        }
        YamlConfiguration c = YamlConfiguration.loadConfiguration(this.warpFile);
        for (String key : c.getConfigurationSection("").getKeys(false)) {
            this.addWarp(key, new Location(this.getPlugin().getServer().getWorld(c.getString(key + ".world")), c.getDouble(key + ".x"), c.getDouble(key + ".y"), c.getDouble(key + ".z"),
                    (float) c.getDouble(key + ".yaw"), (float) c.getDouble(key + ".pitch")));
        }
        this.getPlugin().getLogger().log(Level.INFO, String.format("Warps loaded: %s", this.warps.size()));
    }

    private void loadSigns() {
        if (this.getPlugin().getSettings().useMySQL()) {
            ResultSet set = this.getPlugin().getMySQL().executeQuery("SELECT * FROM "
                    + this.getPlugin().getSignTable());
            if (set == null) {
                this.getPlugin().getLogger().severe("Something is wrong with your MySQL database!");
                this.getPlugin().getLogger().severe("Plugin is disabling!");
                this.getPlugin().getServer().getPluginManager().disablePlugin(this.getPlugin());
                return;
            }
            try {
                while (set.next()) {
                    Location loc = new Location(
                            this.getPlugin().getServer().getWorld(set.getString("world")),
                            set.getInt("x"),
                            set.getInt("y"),
                            set.getInt("z"));
                    this.addSign(loc).setWarp(set.getString("warp"));
                    if (set.getInt("price") != 0) {
                        SignInfo added = this.getSign(loc);
                        if (added == null) {
                            //dafuq?
                            continue;
                        }
                        added.setPrice(set.getInt("price"));
                    }
                }
            } catch (SQLException ex) {
                this.getPlugin().getLogger().log(Level.SEVERE, null, ex);
            }
            return;
        }
        if(!this.signFile.exists()){
            try {
                this.signFile.createNewFile();
            } catch (IOException ex) {
                this.getPlugin().getLogger().log(Level.SEVERE, null, ex);
            } finally {
                return;
            }
        }
        YamlConfiguration c = YamlConfiguration.loadConfiguration(this.signFile);
        for (String key : c.getConfigurationSection("").getKeys(false)) {
            //Key is location
            if(!key.contains(",")){
                //dafuq?
                continue;
            }
            String[] splot = key.split(",");
            Location loc = new Location(this.getPlugin().getServer().getWorld(splot[0]),
                    Integer.parseInt(splot[1]),
                    Integer.parseInt(splot[2]),
                    Integer.parseInt(splot[3]));
            String extra = c.getString(key);
            if(extra.contains(",")){
                String warp = extra.split(",")[0];
                int price = Integer.parseInt(extra.split(",")[1]);
                this.addSign(loc);
                this.getSign(loc).setPrice(price);
                this.getSign(loc).setWarp(warp);
                continue;
            }
            this.addSign(loc).setWarp(extra); 
        }
        this.getPlugin().getLogger().log(Level.INFO, String.format("Signs loaded: %s", this.signs.size()));
    }

    public Warp addWarp(String name, Location loc) {
        return this.addWarp(name, loc, this.getPlugin().getSettings().getWarpUsePrice());
    }

    public Warp addWarp(String name, Location loc, int price) {
        this.warps.put(name, new Warp(name, loc, price));
        Warp w = this.getWarp(name);
        if (this.getPlugin().getSettings().useMySQL()) {
            w.save(this.getPlugin().getMySQL(), this.getPlugin().getWarpTable());
        } else {
            w.save(this.warpFile);
        }
        return w;
    }

    public SignInfo addSign(Location loc) {
        this.signs.put(loc.getWorld().getName() + "," + 
                loc.getBlockX() + "," + 
                loc.getBlockY() + "," + 
                loc.getBlockZ(), new SignInfo(
                loc.getWorld().getName(), 
                loc.getBlockX(), 
                loc.getBlockY(),
                loc.getBlockZ()));
        return this.getSign(loc);
    }
    
    public SignInfo getSign(Location loc) {
        return this.signs.get(loc.getWorld().getName() + "," + 
                loc.getBlockX() + "," + 
                loc.getBlockY() + "," + 
                loc.getBlockZ());
    }

    public Warp removeWarp(String name) {
        if (!this.hasWarp(name)) {
            return null;
        }
        if (this.getPlugin().getSettings().useMySQL()) {
            this.warps.get(name).delete(this.getPlugin().getMySQL(), this.getPlugin().getWarpTable());
        } else {
            this.warps.get(name).delete(this.warpFile);
        }
        return this.warps.remove(name);
    }

    public void removeSign(Location loc) {
        SignInfo s = this.getSign(loc);
        if (s == null) {
            return;
        }
        if (this.getPlugin().getSettings().useMySQL()) {
            s.delete(this.getPlugin().getMySQL(), this.getPlugin().getSignTable());
        } else {
            s.delete(this.signFile);
        }
    }

    public boolean isSortalSign(Location loc) {
        return this.getSign(loc) == null ? false : true;
    }

    public boolean hasWarp(String name) {
        return this.warps.containsKey(name);
    }

    public Warp getWarp(String name) {
        return this.warps.get(name);
    }

    public void saveData() {
        for (String name : this.warps.keySet()) {
            if (this.getPlugin().getSettings().isDebug()) {
                this.getPlugin().getLogger().log(Level.INFO, String.format("[Debug] Saving warp %s", name));
            }
            if (this.getPlugin().getSettings().useMySQL()) {
                this.warps.get(name).save(this.getPlugin().getMySQL(), this.getPlugin().getWarpTable());
            } else {
                this.warps.get(name).save(warpFile);
            }
        }
        for (String loc : this.signs.keySet()) {
            if (this.getPlugin().getSettings().isDebug()) {
                this.getPlugin().getLogger().log(Level.INFO, String.format("[Debug] Saving sign at %s", loc));
            }
            if (this.getPlugin().getSettings().useMySQL()) {
                this.signs.get(loc).save(this.getPlugin().getMySQL(), this.getPlugin().getSignTable());
            } else {
                this.signs.get(loc).save(this.signFile);
            }
        }
    }

    public Set<Warp> getWarps() {
        Set<Warp> warpSet = new HashSet<Warp>();
        for (String warp : this.warps.keySet()) {
            warpSet.add(this.warps.get(warp));
        }
        return warpSet;
    }
}
