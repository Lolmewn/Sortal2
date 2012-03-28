package nl.lolmewn.sortal;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import net.milkbowl.vault.economy.Economy;
import nl.lolmewn.sortal.Metrics.Graph;
import nl.lolmewn.sortal.Metrics.Plotter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Lolmewn
 */
public class Main extends JavaPlugin{
    
    private WarpManager warpManager;
    private Settings settings;
    private MySQL mysql;
    private Economy eco; //Vault
    private Metrics metrics;
    
    protected HashMap<String, Integer> setcost = new HashMap<String, Integer>();
    protected HashMap<String, String> register = new HashMap<String, String>();
    protected HashSet<String> unregister = new HashSet<String>();
    protected HashMap<String, Integer> setuses = new HashMap<String, Integer>();
    
    private boolean willUpdate;
    private double newVersion;
    
    private File settingsFile = new File("plugins" + File.separator + "Sortal"
            + File.separator + "settings.yml");
    
    @Override
    public void onDisable(){
        this.saveData();
        this.getServer().getScheduler().cancelTasks(this);
        if(this.willUpdate){
            this.getLogger().log(Level.INFO, String.format("Updating Sortal to version %s, please wait..", newVersion));
            this.update();
        }
        this.getLogger().info("Disabled!");
    }
    
    private void update(){
        try {
            BufferedInputStream in = new BufferedInputStream(new URL("http://dl.dropbox.com/u/7365249/Sortal.jar").openStream());
            FileOutputStream fout = new FileOutputStream(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            byte data[] = new byte[1024]; //Download 1 KB at a time
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
            }
            in.close();
            fout.close();
            YamlConfiguration c = new YamlConfiguration();
            try {
                c.load(this.settingsFile);
                c.set("version", this.newVersion);
                c.save(this.settingsFile);
            } catch (Exception e) {
                this.getLogger().log(Level.WARNING, null, e);
            }
        } catch (MalformedURLException e) {
            this.getLogger().log(Level.WARNING, null, e);
        } catch (IOException e) {
            this.getLogger().log(Level.WARNING, null, e);
        } catch (URISyntaxException e) {
            this.getLogger().log(Level.WARNING, null, e);
        }
    }
    
    @Override
    public void onEnable() {
        this.settings = new Settings(this); //Also loads Localisation
        if (this.getSettings().useMySQL()) {
            if (!this.initMySQL()) {
                this.getLogger().severe("Something is wrong with the MySQL database, switching to flatfile!");
                this.getSettings().setUseMySQL(false);
            }
        }
        this.warpManager = new WarpManager(this);
        this.getCommand("sortal").setExecutor(new SortalExecutor(this));
        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
        if(!this.initVault()){
            this.getLogger().info("Vault error or not found, setting costs to 0!");
            this.getSettings().setWarpCreatePrice(0);
            this.getSettings().setWarpUsePrice(0);
        }else{
            this.getLogger().info("Hooked into Vault and Economy plugin succesfully!");
        }
        this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable(){
            public void run() {
                new Thread(new Runnable(){public void run() {saveData();getLogger().info("Data saved!");}}).start();
                }
        }, 36000L, 36000L);
        this.startMetrics();
        this.checkUpdate();
        this.getLogger().log(Level.INFO, String.format("Version %s build %s loaded!", this.getSettings().getVersion(), this.getDescription().getVersion()));
    }
    
    protected void startMetrics(){
        try {
            this.metrics = new Metrics(this);
            Graph g = this.metrics.createGraph("Custom Data for Sortal");
            g.addPlotter(new Plotter("Warps") {
                @Override
                public int getValue() {
                    return getWarpManager().getWarps().size();
                }
            });
            g.addPlotter(new Plotter("Signs") {

                @Override
                public int getValue() {
                    return getWarpManager().getSigns().size();
                }
            });
            this.metrics.start();
        } catch (IOException ex) {
            this.getLogger().log(Level.WARNING, null, ex);
        }
    }
    
    protected boolean initMySQL(){
        this.mysql = new MySQL(
                    this.getSettings().getDbHost(),
                    this.getSettings().getDbPort(),
                    this.getSettings().getDbUser(),
                    this.getSettings().getDbPass(),
                    this.getSettings().getDbDatabase(),
                    this.getSettings().getDbPrefix());
        return this.mysql.isFault();
    }

    public Settings getSettings() {
        return settings;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }
    
    protected MySQL getMySQL(){
        return this.mysql;
    }
    
    protected String getWarpTable(){
        return this.getSettings().getDbPrefix() + "warps";
    }
    
    protected String getSignTable(){
        return this.getSettings().getDbPrefix() + "signs";
    }
    
    public void saveData(){
        this.getWarpManager().saveData();
    }
    
    public boolean pay(Player p, int amount){
        if(amount == 0){
            return true;
        }
        if(initVault()){
            if(!this.eco.has(p.getName(), amount)){
                //Doesn't have enough money
                p.sendMessage(this.getSettings().getLocalisation().getNoMoney(Integer.toString(amount)));
                return false;
            }
            this.eco.withdrawPlayer(p.getName(), amount);
            p.sendMessage(this.getSettings().getLocalisation().getPaymentComplete(Integer.toString(amount)));
            return true;
        }
        //Either vault isn't found or Economy isn't found.
        return true;
    }

    private boolean initVault() {
        if(this.eco != null){
            return true;
        }
        if(this.getServer().getPluginManager().getPlugin("Vault") == null){
            //Vault not found
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp == null && (this.getSettings().getWarpCreatePrice() != 0 || this.getSettings().getWarpUsePrice() != 0)){
            //Vault not found
            return false;
        }
        this.eco = rsp.getProvider();
        if(this.eco == null && (this.getSettings().getWarpCreatePrice() != 0 || this.getSettings().getWarpUsePrice() != 0)){
            return false;
        }
        return true;
    }

    private void checkUpdate() {
        if(!this.getSettings().isUpdate()){
            return;
        }
        try {
            URL url = new URL("http://dl.dropbox.com/u/7365249/sortal.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            while ((str = in.readLine()) != null) {
                if (this.getSettings().getVersion() < Double.parseDouble(str)) {
                    this.newVersion = Double.parseDouble(str);
                    this.willUpdate = true;
                    this.getLogger().info(String.format("An update is available! Will be downloaded on Disable! New version: %s", str));
                }
            }
            in.close();
        } catch (MalformedURLException e) {
            this.getLogger().log(Level.WARNING, null, e);
        } catch (IOException e) {
            this.getLogger().log(Level.WARNING, null, e);
        } catch (Exception e) {
            this.getLogger().log(Level.WARNING, null, e);
        }
    }
}
