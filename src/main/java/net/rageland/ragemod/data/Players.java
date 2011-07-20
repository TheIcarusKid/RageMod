package net.rageland.ragemod.data;

import java.util.HashMap;

import net.rageland.ragemod.RageMod;

public class Players {
	
	// Set up PlayerTowns as a static instance
	private static volatile Players instance;
	
	private static HashMap<String, PlayerData> players = new HashMap<String, PlayerData>();
	
    public static Players getInstance() 
    {
		if (instance == null) 
		{
			instance = new Players();
		}
		return instance;
	}
    
    // Retrieves the player's data from memory and updates last login time
    // Creates a new Player record if one does not exist
    public static PlayerData playerLogin(String playerName)
    {
    	PlayerData playerData = RageMod.database.playerLogin(playerName);
    	players.put(playerName.toLowerCase(), playerData);
    	
    	return playerData;
    }
    
    // Gets the player from memory, or pulls from DB if not present.  Returns NULL for non-existent players
    public static PlayerData get(String playerName)
    {       	
    	if( players.containsKey(playerName.toLowerCase()) )
    		return players.get(playerName.toLowerCase());
    	else
    	{
    		System.out.println("DB fetch called for player: " + playerName);
    		PlayerData playerData = RageMod.database.playerFetch(playerName);
    		if( playerData == null )
    			return null;
    		else
    		{
    			players.put(playerName.toLowerCase(), playerData);
    			return playerData;
    		}
    	}
    }
    
    // Updates the player's info in memory
    public static void update(PlayerData playerData)
    {
    	if( players.containsKey(playerData.name.toLowerCase()) )
    	{
    		players.put(playerData.name.toLowerCase(), playerData);
    	}
    	else
    	{
    		System.out.println("Players.update called on invalid value: " + playerData.name);
    	}
    }
    
    // For debugging - returns the number of players loaded into memory
    public static int size()
    {
    	return players.size();
    }
		

}
