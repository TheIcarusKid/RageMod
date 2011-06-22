package net.rageland.ragemod.database;

// TODO: Pull config data from properties

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import net.rageland.ragemod.PlayerData;
import net.rageland.ragemod.PlayerTown;
import net.rageland.ragemod.Players;
import net.rageland.ragemod.RageMod;

import org.bukkit.entity.Player;

public class RageDB {

    protected Connection conn;
    public String url = "jdbc:mysql://mirach.lunarpages.com:3306/";
    public String dba = "johnz2_ragemod?noAccessToProcedureBodies=true";
    public String driver = "com.mysql.jdbc.Driver";
    
    private PreparedStatement preparedStatement = null;
    private ResultSet rs = null;
    
    private RageMod plugin;



        public RageDB(RageMod instance)
        {
        	plugin = instance;
        	
            try
            {
              Class.forName(driver).newInstance();
               conn = DriverManager.getConnection(url+dba,"johnz2_ragemod","ragemod");
            }
            catch(Exception e)
            {
               System.out.println(e);
            }
        }

        public Connection getConnection()
        {
          return conn;
        }
        
    	// Write query
    	public boolean Write(String sql) 
    	{
    		/*
    		 * Double check connection to MySQL
    		 */
    		try 
    		{
    			if(!conn.isValid(5))
    			{
    			//reconnect();
    			}
    		} catch (SQLException e) 
    		{
    			e.printStackTrace();
    		}
    			
    		try 
    			{
    		  		PreparedStatement stmt = null;
    		  		stmt = this.conn.prepareStatement(sql);
    		  		stmt.executeUpdate();
    		  		return true;
    			} catch(SQLException ex) {
    			    System.out.println("SQLException: " + ex.getMessage());
    			    System.out.println("SQLState: " + ex.getSQLState());
    			    System.out.println("VendorError: " + ex.getErrorCode());
    				return false;
    			}
     	}
    	
    	// read query
    	public HashMap<Integer, ArrayList<String>> Read(String sql) {
    		/*
    		 * Double check connection to MySQL
    		 */
    		try 
    		{
    			if(!conn.isValid(5))
    			{
    			//reconnect();
    			}
    		} catch (SQLException e) 
    		{
    			e.printStackTrace();
    		}
    		
      		PreparedStatement stmt = null;
    		ResultSet rs = null;
    		HashMap<Integer, ArrayList<String>> Rows = new HashMap<Integer, ArrayList<String>>();
    		
    		try {
    			stmt = this.conn.prepareStatement(sql);
    		    if (stmt.executeQuery() != null) {
    		    	stmt.executeQuery();
    		        rs = stmt.getResultSet();
    				while (rs.next()) {
    					ArrayList<String> Col = new ArrayList<String>();
    					for(int i=1;i<=rs.getMetaData().getColumnCount();i++) {						
    						Col.add(rs.getString(i));
    					}
    					Rows.put(rs.getRow(),Col);
    				}
    			}	    
    		}
    		catch (SQLException ex) {
    		    System.out.println("SQLException: " + ex.getMessage());
    		    System.out.println("SQLState: " + ex.getSQLState());
    		    System.out.println("VendorError: " + ex.getErrorCode());
    		}
    		
    		// release dataset
    	    if (rs != null) {
    	        try {
    	            rs.close();
    	        } catch (SQLException sqlEx) { } // ignore
    	        rs = null;
    	    }
    	    if (stmt != null) {
    	        try {
    	            stmt.close();
    	        } catch (SQLException sqlEx) { } // ignore
    	        stmt = null;
    	    }

    		return Rows;
    	}
        
        // Tells the database when a player logs into the server
        public void player_Login(String name)
        {
        	try
        	{
        		// Apparently prepareCall() is expensive - separate this out and
        		// reset the parameters
        		//CallableStatement cStmt = conn.prepareCall("{CALL rage_p_Login(?)}");

        		//cStmt.setString(1, name);
        		
        		//boolean hadResults = cStmt.execute();
        		
        		preparedStatement = conn.prepareStatement(
	    			"CALL rage_p_Login(?)");
        		preparedStatement.setString(1, name);
        		preparedStatement.executeUpdate();
        		
        	} catch (Exception e) {
        		System.out.println("Error in RageDB.player_Login: " + e.getMessage());
    		} finally {
    			close();
    		}        	
        }
        
        public String testName()
        {
        	try
        	{
        		preparedStatement = conn.prepareStatement(
        			"SELECT * FROM Players LIMIT 1");
	        	rs = preparedStatement.executeQuery();
	        	
	        	rs.next();
	        	return rs.getString("Name");
        	} catch (Exception e) {
        		System.out.println("Error in RageDB.testName: " + e.getMessage());
    		} finally {
    			close();
    		}
    		
    		return "";
        }
        
        // You need to close the resultSet
    	private void close() {
    		try {
    			if (rs != null) {
    				rs.close();
    			}

    			if (preparedStatement != null) {
    				preparedStatement.close();
    			}

    			if (conn != null) {
    				//conn.close();
    			}
    		} catch (Exception e) {

    		}
    	}
    	
    	// Load all PlayerTown data
    	public HashMap<String, PlayerTown> LoadPlayerTowns()
        {
    		HashMap<String, PlayerTown> towns = new HashMap<String, PlayerTown>();
    		PlayerTown currentTown = null;
    		
        	try
        	{
	        	preparedStatement = conn.prepareStatement(
    				"SELECT pt.ID_PlayerTown, pt.TownName, pt.XCoord, pt.ZCoord, " +
    				"	tl.Name as TownLevel, tl.UpkeepCost, tl.Size, tl.MaxNPCs, " +
    				"	IFNULL(f.Name, 'Neutral') as FactionName, pt.TreasuryBalance, pt.BankruptDate " +
    				"FROM PlayerTowns pt " +
    				"INNER JOIN TownLevels tl ON pt.ID_TownLevel = tl.ID_TownLevel " +
    				"LEFT JOIN Factions f ON pt.ID_Faction = f.ID_Faction");
	        	
	        	rs = preparedStatement.executeQuery();
	        	
	        	while ( rs.next() ) 
	        	{
//	        		towns.put(rs.getString("TownName"), 
//	        			new PlayerTown(rs.getInt("ID_PlayerTown"), rs.getString("TownName"), rs.getInt("XCoord"), rs.getInt("ZCoord"),
//	        				rs.getString("FactionName"), rs.getFloat("TreasuryBalance"), rs.getDate("BankruptDate"), rs.getString("TownLevel"),
//	        				rs.getFloat("UpkeepCost"), rs.getInt("Size"), rs.getInt("MaxNPCs")));
	        		
	        		currentTown = new PlayerTown();
	        		currentTown.ID_PlayerTown = rs.getInt("ID_PlayerTown");
	        		currentTown.TownName = rs.getString("TownName");
	        		currentTown.XCoord = rs.getInt("XCoord");
	        		currentTown.ZCoord = rs.getInt("ZCoord");
	        		currentTown.Faction = rs.getString("FactionName");
	        		currentTown.TreasuryBalance = rs.getFloat("TreasuryBalance");
	        		currentTown.BankruptDate = rs.getDate("BankruptDate");
	        		currentTown.TownLevel = rs.getString("TownLevel");
	        		currentTown.UpkeepCost = rs.getFloat("UpkeepCost");
	        		currentTown.Size = rs.getInt("Size");
	        		currentTown.MaxNPCs = rs.getInt("MaxNPCs");
	        			        		
	        		towns.put(rs.getString("TownName"), currentTown);	        		
	        	}
	        		
	        	return towns;				
	        		        	
        	} catch (Exception e) {
        		System.out.println("Error in RageDB.LoadPlayerTowns: " + e.getMessage());
    		} finally {
    			close();
    		}
    		
    		return null;
        }
    	
    	// Load data from Players table on login if existing player - create new row if not 
    	public PlayerData PlayerLogin(String playerName)
        {
    		PlayerData playerData = null;
    		ResultSet rs = null;    		
    		
        	try
        	{
	        	String selectQuery = 
	        		"SELECT p.ID_Player, IFNULL(f.Name, 'Neutral') as FactionName, p.IsMember, p.MemberExpiration, p.Bounty, p.ExtraBounty, " +
	        		"   (p.Home_XCoord IS NOT NULL) AS Home_IsSet, p.Home_XCoord, p.Home_YCoord, p.Home_ZCoord, p.Home_LastUsed, " +
	        		"	(p.Spawn_XCoord IS NOT NULL) AS Spawn_IsSet, p.Spawn_XCoord, p.Spawn_YCoord, p.Spawn_ZCoord, p.Spawn_LastUsed, " +
	        		"	IFNULL(pt.TownName, '') as TownName, p.IsMayor " +
	        		"FROM Players p " +
	        		"LEFT JOIN Factions f ON p.ID_Faction = f.ID_Faction " +
	        		"LEFT JOIN PlayerTowns pt ON p.ID_PlayerTown = pt.ID_PlayerTown " +
	        		"WHERE p.Name = '" + playerName + "'";
        		
        		preparedStatement = conn.prepareStatement(selectQuery);	        		        	
	        	rs = preparedStatement.executeQuery();
	        	
	        	// Test to see if result set was empty
	        	if( rs.next() )
	        	{
	        		playerData = new PlayerData();
	        		
	        		playerData.ID_Player = rs.getInt("ID_Player");
	        		playerData.Faction = rs.getString("FactionName");
	        		playerData.IsMember = rs.getBoolean("IsMember");
	        		playerData.MemberExpiration = rs.getDate("MemberExpiration");
	        		playerData.Bounty = rs.getFloat("Bounty");
	        		playerData.ExtraBounty = rs.getFloat("ExtraBounty");
	        		playerData.TownName = rs.getString("TownName");
	        		playerData.IsMayor = rs.getBoolean("IsMayor");
	        		
	        		playerData.Home_IsSet = rs.getBoolean("Home_IsSet");
	        		playerData.Home_XCoord = rs.getInt("Home_XCoord");
	        		playerData.Home_YCoord = rs.getInt("Home_YCoord");
	        		playerData.Home_ZCoord = rs.getInt("Home_ZCoord");
	        		playerData.Home_LastUsed = rs.getDate("Home_LastUsed");
	        		
	        		playerData.Spawn_IsSet = rs.getBoolean("Spawn_IsSet");
	        		playerData.Spawn_XCoord = rs.getInt("Spawn_XCoord");
	        		playerData.Spawn_YCoord = rs.getInt("Spawn_YCoord");
	        		playerData.Spawn_ZCoord = rs.getInt("Spawn_ZCoord");
	        		playerData.Spawn_LastUsed = rs.getDate("Spawn_LastUsed");
	        		
	        		// Set LastLogin time
	        		preparedStatement = conn.prepareStatement("UPDATE Players SET LastLogin = NOW() WHERE Name = '" + playerName + "'");
	        		preparedStatement.executeUpdate();
	        	}
	        	else
	        	{
	        		// Insert new data into DB - default DB values will take care of the data
	        		preparedStatement = conn.prepareStatement("INSERT INTO Players (Name, LastLogin) VALUES ('" + playerName + "', NOW())");
	        		preparedStatement.executeUpdate();
	        		
	        		// Pull the DB-created defaults into memory
	        		preparedStatement = conn.prepareStatement(selectQuery);		
	        		rs = preparedStatement.executeQuery();
	        		
	        		playerData = new PlayerData();
	        		
	        		playerData.ID_Player = rs.getInt("ID_Player");
	        		playerData.Faction = rs.getString("FactionName");
	        		playerData.IsMember = rs.getBoolean("IsMember");
	        		playerData.MemberExpiration = rs.getDate("MemberExpiration");
	        		playerData.Bounty = rs.getFloat("Bounty");
	        		playerData.ExtraBounty = rs.getFloat("ExtraBounty");
	        		playerData.TownName = rs.getString("TownName");
	        		playerData.IsMayor = rs.getBoolean("IsMayor");
	        		
	        		playerData.Home_IsSet = rs.getBoolean("Home_IsSet");
	        		playerData.Home_XCoord = rs.getInt("Home_XCoord");
	        		playerData.Home_YCoord = rs.getInt("Home_YCoord");
	        		playerData.Home_ZCoord = rs.getInt("Home_ZCoord");
	        		playerData.Home_LastUsed = rs.getDate("Home_LastUsed");
	        		
	        		playerData.Spawn_IsSet = rs.getBoolean("Spawn_IsSet");
	        		playerData.Spawn_XCoord = rs.getInt("Spawn_XCoord");
	        		playerData.Spawn_YCoord = rs.getInt("Spawn_YCoord");
	        		playerData.Spawn_ZCoord = rs.getInt("Spawn_ZCoord");
	        		playerData.Spawn_LastUsed = rs.getDate("Spawn_LastUsed");

//	        		playerData = new PlayerData(rs.getInt("ID_Player"), playerName, rs.getString("FactionName"), rs.getBoolean("IsMember"), rs.getDate("MemberExpiration"),
//	        				rs.getFloat("Bounty"), rs.getFloat("ExtraBounty"), 
//	        				rs.getBoolean("Home_IsSet"), rs.getInt("Home_XCoord"), rs.getInt("Home_YCoord"), rs.getInt("Home_ZCoord"), rs.getDate("Home_LastUsed"), 
//	        				rs.getBoolean("Spawn_IsSet"), rs.getInt("Spawn_XCoord"), rs.getInt("Spawn_YCoord"), rs.getInt("Spawn_ZCoord"), rs.getDate("Spawn_LastUsed"),
//	        				rs.getString("TownName"), rs.getBoolean("IsMayor"));
	        	}	        	
	        	
	        	return playerData;				
	        		        	
        	} catch (SQLException e) {
        		System.out.println("Error in RageDB.PlayerLogin(): " + e.getMessage());
			    System.out.println("SQLState: " + e.getSQLState());
			    System.out.println("VendorError: " + e.getErrorCode());
    		} finally {
    			close();
    		}
    		
    		return null;
        }
    	
    	
    	// Load data from Players table for specified player; returns NULL if not found 
    	public PlayerData PlayerFetch(String playerName)
        {
    		PlayerData playerData = null;
    		ResultSet rs = null;    		
    		
        	try
        	{
	        	String selectQuery = 
	        		"SELECT p.ID_Player, IFNULL(f.Name, 'Neutral') as FactionName, p.IsMember, p.MemberExpiration, p.Bounty, p.ExtraBounty, " +
	        		"   (p.Home_XCoord IS NOT NULL) AS Home_IsSet, p.Home_XCoord, p.Home_YCoord, p.Home_ZCoord, p.Home_LastUsed, " +
	        		"	(p.Spawn_XCoord IS NOT NULL) AS Spawn_IsSet, p.Spawn_XCoord, p.Spawn_YCoord, p.Spawn_ZCoord, p.Spawn_LastUsed, " +
	        		"	IFNULL(pt.TownName, '') as TownName, p.IsMayor " +
	        		"FROM Players p " +
	        		"LEFT JOIN Factions f ON p.ID_Faction = f.ID_Faction " +
	        		"LEFT JOIN PlayerTowns pt ON p.ID_PlayerTown = pt.ID_PlayerTown " +
	        		"WHERE p.Name = '" + playerName + "'";
        		
        		preparedStatement = conn.prepareStatement(selectQuery);	        		        	
	        	rs = preparedStatement.executeQuery();
	        	
	        	// Test to see if result set was empty - return null if not 
	        	if( rs.next() )
	        	{
	        		playerData = new PlayerData();
	        		
	        		playerData.ID_Player = rs.getInt("ID_Player");
	        		playerData.Faction = rs.getString("FactionName");
	        		playerData.IsMember = rs.getBoolean("IsMember");
	        		playerData.MemberExpiration = rs.getDate("MemberExpiration");
	        		playerData.Bounty = rs.getFloat("Bounty");
	        		playerData.ExtraBounty = rs.getFloat("ExtraBounty");
	        		playerData.TownName = rs.getString("TownName");
	        		playerData.IsMayor = rs.getBoolean("IsMayor");
	        		
	        		playerData.Home_IsSet = rs.getBoolean("Home_IsSet");
	        		playerData.Home_XCoord = rs.getInt("Home_XCoord");
	        		playerData.Home_YCoord = rs.getInt("Home_YCoord");
	        		playerData.Home_ZCoord = rs.getInt("Home_ZCoord");
	        		playerData.Home_LastUsed = rs.getDate("Home_LastUsed");
	        		
	        		playerData.Spawn_IsSet = rs.getBoolean("Spawn_IsSet");
	        		playerData.Spawn_XCoord = rs.getInt("Spawn_XCoord");
	        		playerData.Spawn_YCoord = rs.getInt("Spawn_YCoord");
	        		playerData.Spawn_ZCoord = rs.getInt("Spawn_ZCoord");
	        		playerData.Spawn_LastUsed = rs.getDate("Spawn_LastUsed");
	        		
		        	return playerData;
	        	}
	        		        	
        	} catch (SQLException e) {
        		System.out.println("Error in RageDB.PlayerLogin(): " + e.getMessage());
			    System.out.println("SQLState: " + e.getSQLState());
			    System.out.println("VendorError: " + e.getErrorCode());
    		} finally {
    			close();
    		}
    		
    		return null;
        }
    	
    	
    	// Load data from Players table on login if existing player - create new row if not 
    	public void TownAdd(String targetPlayerName, String townName)
        {
    		ResultSet rs = null;   
    		PlayerData playerData = Players.Get(targetPlayerName);
    		
        	try
        	{
        		// Update the Players table to create the town association
        		preparedStatement = conn.prepareStatement(
        				"UPDATE Players SET ID_PlayerTown = " +
        				"	(SELECT ID_PlayerTown FROM PlayerTowns WHERE TownName = '" + townName + "') " +
        				", IsMayor = 0 WHERE ID_Player = " + playerData.ID_Player);
        		preparedStatement.executeUpdate();	
	        		        		        	
        	} catch (SQLException e) {
        		System.out.println("Error in RageDB.TownAdd(): " + e.getMessage());
			    System.out.println("SQLState: " + e.getSQLState());
			    System.out.println("VendorError: " + e.getErrorCode());
    		} finally {
    			close();
    		}
        }
    	
    	
       	// Load data from Players table on login if existing player - create new row if not 
    	public void TownCreate(Player player, String townName)
        {
    		ResultSet rs = null;   
    		PlayerData playerData = Players.Get(player.getName());
    		
        	try
        	{
        		// TODO: Set default treasury balance from config
        		// Insert the new town into the PlayerTowns table
        		preparedStatement = conn.prepareStatement(
        				"INSERT INTO PlayerTowns (TownName, XCoord, ZCoord, ID_Faction, TreasuryBalance) " +
        				"VALUES ('" + townName + "', " + (int)player.getLocation().getX() + ", " + (int)player.getLocation().getZ() + ", " +  
        				"(SELECT ID_Faction FROM Players WHERE ID_Player = " + playerData.ID_Player + "), 100)",
        				Statement.RETURN_GENERATED_KEYS);        		
        		preparedStatement.executeUpdate();
        		
        		// Retrieve the new auto-increment town ID 
        		rs = preparedStatement.getGeneratedKeys();
        		rs.next();
        		
        		// Update the Players table
        		preparedStatement = conn.prepareStatement(
        				"UPDATE Players SET ID_PlayerTown = " + rs.getInt(1) + ", IsMayor = 1 " +
        				"WHERE ID_Player = " + playerData.ID_Player);        		
        		preparedStatement.executeUpdate();
	        		        		        	
        	} catch (SQLException e) {
        		System.out.println("Error in RageDB.TownCreate(): " + e.getMessage());
			    System.out.println("SQLState: " + e.getSQLState());
			    System.out.println("VendorError: " + e.getErrorCode());
    		} finally {
    			close();
    		}
        }

    	public void TownLeave(Player player)
        {
    		ResultSet rs = null;   
    		PlayerData playerData = Players.Get(player.getName());
    		
        	try
        	{
        		// Update the Players table to remove the town association
        		preparedStatement = conn.prepareStatement(
        				"UPDATE Players SET ID_PlayerTown = NULL, IsMayor = 0 WHERE ID_Player = " + playerData.ID_Player);
        		preparedStatement.executeUpdate();	
        	} 
        	catch (SQLException e) {
        		System.out.println("Error in RageDB.TownLeave(): " + e.getMessage());
			    System.out.println("SQLState: " + e.getSQLState());
			    System.out.println("VendorError: " + e.getErrorCode());
    		} finally {
    			close();
    		}
        }

        
        
}
