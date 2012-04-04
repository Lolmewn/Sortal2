package nl.lolmewn.sortal;

import java.io.*;
import java.util.HashMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Lolmewn
 */
public class Localisation {
    
    private File localisation = new File("plugins" + File.separator + "Sortal"
            + File.separator + "localisation.yml");
    
    private String noPlayer;
    private String noPerms;
    private String createNameForgot;
    private String deleteNameForgot;
    private String nameInUse;
    private String warpCreated;
    private String warpDeleted;
    private String warpNotFound;
    private String paymentComplete;
    private String noMoney;
    private String noWarpsFound;
    private String errorInSign;
    private String playerTeleported;
    private String costSet;
    private String maxUsesReached;

    public Localisation() {
        this.checkFile();
        this.loadFile();
    }

    private void checkFile() {
        if(!this.localisation.exists()){
            Bukkit.getLogger().info("[Sortal] Trying to create default language file...");
            try {
                this.localisation.getParentFile().mkdirs();
                InputStream in = this.getClass().
                        getClassLoader().getResourceAsStream("localisation.yml");
                OutputStream out = new BufferedOutputStream(
                        new FileOutputStream(this.localisation));
                int c;
                while ((c = in.read()) != -1) {
                    out.write(c);
                }
                out.flush();
                out.close();
                in.close();
                Bukkit.getLogger().info("[Sortal] Default language file created succesfully!");
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "[Sortal] Error creating language file! Using default settings!", e);
            }
        }
    }

    private void loadFile() {
        YamlConfiguration c = YamlConfiguration.loadConfiguration(this.localisation);
        this.createNameForgot = c.getString("commands.createNameForgotten" , "Please specify a name for this warp!");
        this.deleteNameForgot = c.getString("commands.deleteNameForgotten" , "Please specify the name of the warp!");
        this.nameInUse = c.getString("commands.nameInUse", "Warp $WARP already exists!");
        this.noMoney = c.getString("noMoney", "You need $MONEY to do this!");
        this.noPerms = c.getString("noPermissions", "You don't have permissions to do that!");
        this.noPlayer = c.getString("noPlayer", "You have to be a player to do this!");
        this.paymentComplete = c.getString("paymentComplete", "You've payed $MONEY!");
        this.warpCreated = c.getString("commands.warpCreated", "Warp $WARP created!");
        this.warpDeleted = c.getString("commands.warpDeleted", "Warp $WARP deleted!");
        this.warpNotFound = c.getString("commands.warpNotFound", "Warp $WARP not found!");
        this.noWarpsFound = c.getString("commands.noWarpsFound", "There are no warps yet!");
        this.errorInSign = c.getString("signError", "There's something wrong with this sign!");
        this.playerTeleported = c.getString("playerTeleported", "You've teleported to $DEST!");
        this.costSet = c.getString("commands.costSet", "Cost set to $COST!");
        this.maxUsesReached = c.getString("maxUsesReached", "You can't use this sign anymore!");
        Bukkit.getLogger().info("[Sortal] Localisation loaded!");
    }

    public String getMaxUsesReached() {
        return maxUsesReached;
    }

    public String getCreateNameForgot() {
        return createNameForgot;
    }

    public String getDeleteNameForgot() {
        return deleteNameForgot;
    }

    public String getNameInUse(String warp) {
        if(this.nameInUse.contains("$WARP") && warp != null && !warp.equals("")){
            return nameInUse.replace("$WARP", warp);
        }
        return nameInUse;
    }

    public String getNoMoney(String money) {
        if(this.noMoney.contains("$MONEY") && money != null && !money.equals("")){
            return noMoney.replace("$MONEY", money);
        }
        return noMoney;
    }

    public String getNoPerms() {
        return noPerms;
    }

    public String getNoPlayer() {
        return noPlayer;
    }

    public String getPaymentComplete(String money) {
        if(this.paymentComplete.contains("$MONEY") && money != null && !money.equals("")){
            return paymentComplete.replace("$MONEY", money);
        }
        return paymentComplete;
    }

    public String getWarpCreated(String warp) {
        if(this.warpCreated.contains("$WARP") && warp != null && !warp.equals("")){
            return warpCreated.replace("$WARP", warp);
        }
        return warpCreated;
    }

    public String getWarpDeleted(String warp) {
        if(this.warpDeleted.contains("$WARP") && warp != null && !warp.equals("")){
            return warpDeleted.replace("$WARP", warp);
        }
        return warpDeleted;
    }

    public String getWarpNotFound(String warp) {
        if(this.warpNotFound.contains("$WARP") && warp != null && !warp.equals("")){
            return warpNotFound.replace("$WARP", warp);
        }
        return warpNotFound;
    }

    public String getNoWarpsFound() {
        return noWarpsFound;
    }

    public String getErrorInSign() {
        return errorInSign;
    }

    public String getPlayerTeleported(String dest) {
        if(this.playerTeleported.contains("$DEST") && dest != null && !dest.equals("")){
            return playerTeleported.replace("$DEST", dest);
        }
        return playerTeleported;
    }

    public String getCostSet(String cost) {
        if(this.costSet.contains("$COST") && cost != null && !cost.equals("")){
            return costSet.replace("$COST", cost);
        }
        return costSet;
    }

    void addOld(HashMap<String, String> local) {
        try {
            YamlConfiguration c = YamlConfiguration.loadConfiguration(this.localisation);
            for(String path : local.keySet()){
                c.set(path, local.get(path));
            }
            c.save(localisation);
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, null, ex);
        }
        this.loadFile();
    }
    
}
