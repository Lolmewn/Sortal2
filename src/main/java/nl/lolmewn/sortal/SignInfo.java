/*
 * SignInfo.java
 * 
 * Copyright (c) 2012 Lolmewn <info@lolmewn.nl>. 
 * 
 * Sortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Sortal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Sortal.  If not, see <http ://www.gnu.org/licenses/>.
 */

package nl.lolmewn.sortal;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */

public class SignInfo {

    private String warp;
    private String world;
    private int x, y, z;
    private int price = -1;
    private boolean hasPrice = false;
    private int uses = -1;
    private int used = 0;
    private boolean usedTotalBased = false;
    private String owner = null;
    private boolean isPrivate = false;
    private HashSet<String> privateUsers = new HashSet<String>();

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
    
    public void addPrivateUser(String name){
        this.privateUsers.add(name);
    }
    
    public boolean isPrivateUser(String name){
        return this.privateUsers.contains(name);
    }
    
    private String getPrivateUsers(){
        StringBuilder b = new StringBuilder();
        if(this.privateUsers.isEmpty()){
            return "";
        }
        for(String s : this.privateUsers){
            b.append(s).append(",");
        }
        String re = b.toString();
        re.substring(0, re.lastIndexOf(","));
        return re;
    }
    
    public void removePrivateUser(String name){
        if(this.privateUsers.contains(name)){
            this.privateUsers.remove(name);
        }
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
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
        return this.warp == null ? null : this.warp;
    }

    public boolean hasWarp() {
        return this.warp == null || this.warp.equals("null") ? false : true;
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
                        + "usedTotalBased=" + this.isUsedTotalBased() + ", "
                        + "isPrivate=" + this.isPrivate() + ", "
                        + "privateUsers='" + this.getPrivateUsers() + "'"
                        + " WHERE x=" + x + " AND y=" + y + " AND z=" + z + " AND world='" + this.world + "'");
                return;
            }
            //It's not in the table at all
            m.executeQuery("INSERT INTO " + table + "("
                    + "world, "
                    + "x, "
                    + "y, "
                    + "z, "
                    + "warp, "
                    + "price, "
                    + "uses, "
                    + "used, "
                    + "usedTotalBased, "
                    + "isPrivate, "
                    + "privateUsers) VALUES ('" 
                    + this.world + "', "
                    + this.x + ", " 
                    + this.y + ", " 
                    + this.z + ", '" 
                    + this.getWarp() + "', " 
                    + this.getPrice() + ", "
                    + this.uses + ", " 
                    + this.used + ", " 
                    + this.usedTotalBased + ", " 
                    + this.isPrivate() + ", '"
                    + this.getPrivateUsers() + "')");
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
        if (this.hasPrice()) {
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
        if(this.isPrivate()){
            c.set(this.getLocationToString() + ".private", true);
            c.set(this.getLocationToString() + ".privateUsers", Arrays.asList(this.privateUsers.toArray()));
        }else{
            c.set(this.getLocationToString() + ".private", false);
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
