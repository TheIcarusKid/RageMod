package net.rageland.ragemod;

import java.util.HashMap;

import org.bukkit.ChatColor;

import net.rageland.ragemod.data.TownLevel;

// Stores and loads configuration values
public class RageConfig {
	
	/* TODO: 
	 *  - Faction info?
	 * 
	 */
	
	// General settings
	public static String ServerName = "Rageland";
	
	// Database settings
	public static String DB_URL = "jdbc:mysql://localhost:3306/";
    public static String DB_DatabaseName = "johnz2_ragemod";
    public static String DB_Driver = "com.mysql.jdbc.Driver";
    public static String DB_User = "johnz2_ragemod";
    public static String DB_Password = "ragemod";
    
    // Town settings
    public static int Town_MinDistanceBetween = 400;
    public static int Town_MinDistanceEnemyCapitol = 2000;
    public static int Town_MinDistanceSpawn = 1000;  // This leaves the issue of towns hanging into the neutral zone
    public static int Town_MaxLevel_Neutral = 4;
    public static int Town_MaxLevel_Faction = 5;
    public static int Town_DistanceBetweenBeds = 6;
    public static HashMap<Integer, TownLevel> TownLevels;
    
    // Zone settings
    public static String ZoneA_Name = "the Neutral Zone";
    public static int ZoneA_Border = 1000;  // Distance from spawn
    public static String ZoneB_Name = "the War Zone";
    public static int ZoneB_Border = 2000;  // Distance from spawn
    public static String ZoneC_Name = "The Wilds";
    public static int ZoneC_Border = 2500;  // Distance from spawn

    // Lot settings
    public static int Lot_XOffset = -384;			// How to convert the web X coordinates to in-game coords
    public static int Lot_ZOffset = 144;
    public static int Lot_Multiplier = 16;			// The lot grid is based on 16x16 chunks
    public static int Lot_CoalPrice = 10;			// Price for Coal-level member lot in USD
    public static int Lot_IronPrice = 20;			// Price for Iron-level member lot in USD
    public static int Lot_GoldPrice = 30;			// Price for Gold-level member lot in USD
    public static int Lot_DiamondPrice = 40;		// Price for Diamond-level member lot in USD
    
    // Capitol settings
    public static int Capitol_X1a = -384;			// The NW corner of region A for capitol
    public static int Capitol_Z1a = 144;
    public static int Capitol_X2a = 100;			// The SE corner of region A for capitol
    public static int Capitol_Z2a = -244;
    public static String Capitol_Name = ChatColor.DARK_GREEN + "Rage City";
    
    // Cooldowns (in seconds)
    public static int Cooldown_Spawn = 30;
    public static int Cooldown_Home = 30;
    
    // Faction settings
    public static int Faction_BaseJoinCost = 100;		// Initial cost in coins to join a faction
    public static int Faction_JoinCostIncrease = 10; 	// Amount the join cost will go up due to population imbalance

    

    private static volatile RageConfig instance;
	
    public static RageConfig GetInstance() 
    {
		if (instance == null) 
		{
			instance = new RageConfig();
		}
		return instance;
	}
    
    public RageConfig ()
    {
    	loadDefaultTownLevels();
    }
    
    private void loadDefaultTownLevels()
    {
    	TownLevels = new HashMap<Integer, TownLevel>();
    	
    	TownLevel townLevel = new TownLevel();
    	townLevel.Level = 1;
    	townLevel.Name = "Settlement";
    	townLevel.Size = 80;
    	townLevel.InitialCost = 1000;
    	townLevel.UpkeepCost = 5;
    	townLevel.MinimumBalance = 100;
    	townLevel.MaxResidents = 5;
    	townLevel.MaxNPCs = 1;
    	TownLevels.put(1, townLevel);
    	
    	townLevel = new TownLevel();
    	townLevel.Level = 2;
    	townLevel.Name = "Village";
    	townLevel.Size = 120;
    	townLevel.InitialCost = 2000;
    	townLevel.UpkeepCost = 10;
    	townLevel.MinimumBalance = 200;
    	townLevel.MaxResidents = 10;
    	townLevel.MaxNPCs = 2;
    	TownLevels.put(2, townLevel);
    	
    	townLevel = new TownLevel();
    	townLevel.Level = 3;
    	townLevel.Name = "Town";
    	townLevel.Size = 180;
    	townLevel.InitialCost = 3000;
    	townLevel.UpkeepCost = 15;
    	townLevel.MinimumBalance = 300;
    	townLevel.MaxResidents = 15;
    	townLevel.MaxNPCs = 3;
    	TownLevels.put(3, townLevel);
    	
    	townLevel = new TownLevel();
    	townLevel.Level = 4;
    	townLevel.Name = "City";
    	townLevel.Size = 270;
    	townLevel.InitialCost = 5000;
    	townLevel.UpkeepCost = 25;
    	townLevel.MinimumBalance = 500;
    	townLevel.MaxResidents = 25;
    	townLevel.MaxNPCs = 4;
    	TownLevels.put(4, townLevel);
    	
    	townLevel = new TownLevel();
    	townLevel.Level = 5;
    	townLevel.Name = "Capitol";
    	townLevel.Size = 400;
    	townLevel.InitialCost = 10000;
    	townLevel.UpkeepCost = 50;
    	townLevel.MinimumBalance = 1000;
    	townLevel.MaxResidents = 50;
    	townLevel.MaxNPCs = 6;
    	townLevel.IsCapitol = true;
    	TownLevels.put(5, townLevel);
    	
    	
    }

    
}