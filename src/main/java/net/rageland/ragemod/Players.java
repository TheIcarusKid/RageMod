package net.rageland.ragemod;

import java.util.HashMap;

public class Players {
	
	// Set up PlayerTowns as a static instance
	private static volatile Players instance;
	
	public static HashMap<String, PlayerData> players = new HashMap<String, PlayerData>();
	
    public static Players GetInstance() 
    {
		if (instance == null) 
		{
			instance = new Players();
		}
		return instance;
	}
    
    // Retrieves the player's data from memory and updates last login time
    // Creates a new Player record if one does not exist
    public static void PlayerLogin(String playerName)
    {
    	players.put(playerName, RageMod.Database.PlayerLogin(playerName));
    	//players.put(playerName, new PlayerData());
    }
    
    // Gets the player from memory, or pulls from DB if not present.  Returns NULL for non-existent players
    public static PlayerData Get(String playerName)
    {    	
    	if( players.containsKey(playerName) )
    		return players.get(playerName);
    	else
    		return RageMod.Database.PlayerFetch(playerName);
    }
		

}
