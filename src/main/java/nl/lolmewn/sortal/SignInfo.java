package nl.lolmewn.sortal;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Sybren
 */
public class SignInfo {

    private String warp;
    private String world;
    private int x, y, z;
    private int price = -1;
    private boolean hasPrice = false;
    private int uses = -1;
    private int used;
    private boolean usedTotalBased;
    private String owner;

    public SignInfo(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SignInfo(String world, int x, int y, int z, String warp) {
        this(world, x, y, z);
        this.warp = warp;
    }

    public boolean isUsedTotalBased() {
        return usedTotalBased;
    }

    public void setUsedTotalBased(boolean usedTotalBased) {
        this.usedTotalBased = usedTotalBased;
    }

    public boolean hasPrice() {
        return this.hasPrice;
    }

    public void setWarp(String warp) {
        this.warp = warp;
    }

    public String getWarp() {
        return this.warp;
    }

    public boolean hasWarp() {
        return this.warp == null ? false : true;
    }

    public String getOwner() {
        return owner;
    }

    public boolean hasOwner() {
        return this.owner == null ? false : true;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getUses() {
        return uses;
    }

    public void setUses(int uses) {
        this.uses = uses;
    }

    public int getUsed() {
        return used;
    }

    public void setUsed(int used) {
        this.used = used;
    }

    public boolean isThisSign(String world, int x, int y, int z) {
        if (this.x == x && this.y == y && this.z == z && world.equals(this.world)) {
            return true;
        }
        return false;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
        this.hasPrice = true;
    }

    public String getLocationToString() {
        return this.world + "," + this.x + "," + this.y + "," + this.z;
    }

    public Location getLocation() {
        return new Location(Bukkit.getServer().getWorld(world), x, y, z);
    }

    public void save(MySQL m, String table) {
        ResultSet set = m.executeQuery("SELECT * FROM " + table
                + " WHERE x=" + x + " AND y=" + y + " AND z=" + z + " AND world='" + this.world + "'");
        if (set == null) {
            //dafuq? 
            System.out.println("[Sortal] ERR: ResultSet returned null");
            return;
        }
        try {
            while (set.next()) {
                m.executeStatement("UPDATE " + table + " SET "
                        + "warp='" + this.warp + "', "
                        + "price=" + this.getPrice() + ", "
                        + "uses=" + this.getUses() + ", "
                        + "used=" + this.getUsed() + ", "
                        + "usedTotalBased=" + this.isUsedTotalBased()
                        + " WHERE x=" + x + " AND y=" + y + " AND z=" + z + " AND world='" + this.world + "'");
                return;
            }
            //It's not in the table at all
            m.executeQuery("INSERT INTO " + table + "(world, x, y, z, warp, price, uses, used, usedTotalBased) VALUES ("
                    + "'" + this.world + "', "
                    + this.x + ", " + this.y + ", " + this.z
                    + ", '" + this.warp + "', " + this.getPrice() + ", "
                    + this.uses + ", " + this.used + ", " + this.usedTotalBased + ")");
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public void save(File f) {
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException ex) {
                Bukkit.getLogger().log(Level.SEVERE, null, ex);
            }
        }
        YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
        if (this.hasWarp()) {
            c.set(this.getLocationToString() + ".warp", this.warp);
        } else {
            c.set(this.getLocationToString(), null);
        }
        if (this.hasPrice) {
            c.set(this.getLocationToString() + ".price", this.getPrice());
        } else {
            c.set(this.getLocationToString() + ".price", null);
        }
        if (this.getUses() != -1) {
            c.set(this.getLocationToString() + ".uses", this.uses);
            c.set(this.getLocationToString() + ".used", this.used);
            c.set(this.getLocationToString() + ".usedTotalBased", this.usedTotalBased);
        } else {
            c.set(this.getLocationToString() + ".uses", null);
            c.set(this.getLocationToString() + ".used", null);
            c.set(this.getLocationToString() + ".usedTotalBased", null);
        }
        if (this.hasOwner()) {
            c.set(this.getLocationToString() + ".owner", this.getOwner());
        } else {
            c.set(this.getLocationToString() + ".owner", null);
        }
        try {
            c.save(f);
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public void delete(MySQL m, String warpTable) {
        m.executeStatement("DELETE FROM " + warpTable + " WHERE x=" + x + " AND y=" + y + " AND z=" + z + " AND world='" + this.world + "'");
    }

    public void delete(File warpFile) {
        YamlConfiguration c = YamlConfiguration.loadConfiguration(warpFile);
        c.set(this.getLocationToString(), null);
        try {
            c.save(warpFile);
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, null, ex);
        }
    }
}
