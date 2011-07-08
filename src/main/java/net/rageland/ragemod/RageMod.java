package net.rageland.ragemod;

import java.io.File;
import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.config.Configuration;

import net.rageland.ragemod.data.PlayerData;
import net.rageland.ragemod.data.PlayerTowns;
import net.rageland.ragemod.data.Players;
import com.iConomy.*;
//import com.nijiko.permissions.PermissionHandler;
//import com.nijikokun.bukkit.Permissions.Permissions;
//import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

// TODO: Update all method names to lowercase to match Java convention - research first :(

import org.bukkit.plugin.Plugin;

/**
 * RageMod for Bukkit
 *
 * @author TheIcarusKid
 */
public class RageMod extends JavaPlugin {
    private final RMPlayerListener playerListener;
    private final RMBlockListener blockListener;
    private final RMServerListener serverListener;
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
    private Server server; 
    private PluginManager pluginManager;
    public int townCost;
    public iConomy iConomy;
 //   public WorldGuardPlugin worldGuard;
    public static String mainDirectory = "plugins/RageMod";
//    public static PermissionHandler permissionHandler;
    public File file = new File(mainDirectory + File.separator + "config.yml");
    private String missingPermissions;
    //public DatabaseHandler dbhandler = null;
    
    // Static utility classes
    public static RageConfig Config = null;
    public static RageDB Database = null;  
    public static RageZones Zones = null;
    
    public RageMod() {
    	serverListener = new RMServerListener(this);
    	playerListener = new RMPlayerListener(this);
    	blockListener = new RMBlockListener(this);  
    	iConomy = null; 
    	missingPermissions = "You don't have permissions to execute that command.";
    	

        
    }
    
    
    public void onEnable() 
    {    		           
    	server = this.getServer();
        pluginManager = server.getPluginManager();
        
        pluginManager.registerEvent(Event.Type.PLUGIN_ENABLE, this.serverListener, Event.Priority.Normal, this);
        pluginManager.registerEvent(Event.Type.PLUGIN_DISABLE, this.serverListener, Event.Priority.Normal, this);
        
        pluginManager.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Normal, this);
        pluginManager.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
        pluginManager.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        
//        setupPermissions();
        System.out.println( "RageMod is enabled!" );
        
        // Initialize the static classes - make sure to initialize Config first as ther other constuctors rely on it
        Config = new RageConfig();
        Database = new RageDB(this);
        Zones = new RageZones(this);
    	
        
        // Load the HashMaps for DB data
        PlayerTowns.GetInstance().LoadPlayerTowns();
        Players.GetInstance();
        
        // Run some tests because of stupid MC validation preventing me from testing in-game >:(
        runTests();
        
        
    }
    
    public void onDisable() {        
        System.out.println("Goodbye world!");
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {    	       	
    	if(command.getName().equalsIgnoreCase("claimtown") && sender instanceof Player) {
    		
//    		if(!this.permissionHandler.has((Player) sender, "ragemod.commands.claimtown") ){
//    			sender.sendMessage(missingPermissions);
//    			return true; // Nothing happens, the user don't have permissions
//    		}
 //   		townManager.addTown(args[0], (Player) sender);	
	    	return true;
    	}       	
    	return false;
    }
    
    public Configuration load(){
        try {
            Configuration config = new Configuration(file);
            config.load();
            return config;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean isDebugging(final Player player) {
        if (debugees.containsKey(player)) {
            return debugees.get(player);
        } else {
            return false;
        }
    }
    
    

    public void setDebugging(final Player player, final boolean value) {
        debugees.put(player, value);
    }
    
//    private void setupPermissions() {
//        Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
//
//        if (this.permissionHandler == null) {
//            if (permissionsPlugin != null) {
//                this.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
//            } else {
//                
//            }
//        }
//    }
    
    private void runTests()
    {
    	//Location testLoc1 = new Location(this.getServer().getWorld("world"), 100, 64, -100);
    	String playerName = "RedPlayer1";
    	
    	Players.PlayerLogin(playerName);
    	PlayerData playerData = Players.Get(playerName);
    	
    	System.out.println(playerName + "'s town is " + playerData.TownName);
    	
    	System.out.println("Players size: " + Players.size());
    	
    	
    	
    	
    }
}

