/*
 * Copyright (C) 2019 MCME
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mcmiddleearth.connect.bungee.warp;

import com.mcmiddleearth.connect.bungee.ConnectBungeePlugin;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eriol_Eandur
 */
public class MyWarpDBConnector {

    private final String dbUser;
    private final String dbPassword;
    private final String dbName;
    private final String dbIp;
    private final int port;
    
    private Connection dbConnection;

    private PreparedStatement getWarp;
    private PreparedStatement getWarpList;
    private PreparedStatement getPlayerList;
    private PreparedStatement getInvitations;

    private File worldFile = new File(ConnectBungeePlugin.getInstance().getDataFolder(),"world.uuid");
        
    private Map<String, String> worldUUID = new HashMap<>();
    
    private boolean connected = false;
    
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    
    private ScheduledTask keepAliveTask;
    
    public MyWarpDBConnector(Map config) {
        if(config==null) {
            config = new HashMap();
        }
        dbUser = (String) config.get("user");
        dbPassword = (String) config.get("password");//,"mywarp");
        dbName = (String) config.get("dbName");//,"mywarp");
        dbIp = (String) config.get("ip");//, "localhost");
        port = (Integer) config.get("port");//,3306);

        loadWorldUUIDs();
        connect();
        keepAliveTask = ProxyServer.getInstance().getScheduler()
                .schedule(ConnectBungeePlugin.getInstance(), () -> {
            checkConnection();
            WarpHandler.updateCache();
        },10,60,TimeUnit.SECONDS);
    }
    
    public void disconnect() {
        connected = false;
        if(keepAliveTask!=null) {
            keepAliveTask.cancel();
        }
        try {
            dbConnection.close();
        } catch (SQLException ex) {
            Logger.getLogger(MyWarpDBConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private boolean checkConnection() {
        try {
            if(connected && dbConnection.isValid(5)) {
                ConnectBungeePlugin.getInstance().getLogger().log(Level.INFO, 
                        "Successfully checked connection to myWarp database.");
                connected = true;
            } else {
                //throw new SQLException();
                if(dbConnection!=null) {
                    dbConnection.close();
                }
                ConnectBungeePlugin.getInstance().getLogger().log(Level.INFO, 
                        "Reconnecting to myWarp database.");
                connect();
            }
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(MyWarpDBConnector.class.getName()).log(Level.SEVERE, "No DB connection!!", ex);
            connected = false;
            return false;
        }
    }
    
    private void connect() {
        try {
            dbConnection = DriverManager.getConnection(
                    "jdbc:mysql://"+dbIp+":"+port+"/"+dbName,
                    dbUser, dbPassword);

            getWarp = dbConnection.prepareStatement("SELECT warp.name, warp.x, warp.y, warp.z, "
                    + "warp.pitch, warp.yaw, warp.welcome_message, warp.visits, "
                    + "warp.type, world.uuid, owner.uuid, invited.uuid "
                    + "FROM warp JOIN player AS owner ON warp.player_id = owner.player_id "
                    +           "JOIN world ON warp.world_id = world.world_id "
                    +           "LEFT JOIN warp_player_map ON warp.warp_id = warp_player_map.warp_id "
                    +           "LEFT JOIN player AS invited ON warp_player_map.player_id = invited.player_id "
                    + "WHERE warp.name REGEXP ? "
                    + "AND (warp.type = 1 OR owner.uuid = ? OR invited.uuid = ?) "
                    + "ORDER BY warp.visits DESC");
            getWarp.setQueryTimeout(1);
            getWarp.setFetchSize(1);
            getWarpList = dbConnection.prepareStatement("SELECT warp.warp_id, warp.name, warp.player_id, warp.type "
                    + "FROM warp");
            getWarpList.setQueryTimeout(1);
            getPlayerList = dbConnection.prepareStatement("SELECT player.player_id, player.uuid FROM player");
            getPlayerList.setQueryTimeout(1);
            getInvitations = dbConnection.prepareStatement("SELECT player_id, warp_id FROM warp_player_map");
            getPlayerList.setQueryTimeout(1);
            connected = true;
        } catch (SQLException ex) {
            Logger.getLogger(MyWarpDBConnector.class.getName()).log(Level.SEVERE, null, ex);
            connected = false;
        }
    }

    public Set<Warp> getWarps() {
        Set<Warp> result = new HashSet<>();
        try {
            ResultSet warpData = getWarpList.executeQuery();
            ResultSet playerData = getPlayerList.executeQuery();
            Map<Integer, UUID> players = new HashMap<>(); //player_id -> player UUID
            if(playerData.next()) {
                do {
                    players.put(playerData.getInt("player.player_id"),
                            UUID.fromString(playerData.getString("player.uuid")));
                } while(playerData.next());
            }
            playerData.close();
            ResultSet invitationData = getInvitations.executeQuery();
            Map<Integer, Set<UUID>> invitations = new HashMap<>(); // warp_id -> List of player UUID
            if(invitationData.next()) {
                do {
                    int warpId = invitationData.getInt("warp_id");
                    Set<UUID> invitedPlayers = invitations.get(warpId);
                    if(invitedPlayers==null) {
                        invitedPlayers = new HashSet<>();
                        invitations.put(warpId,invitedPlayers);
                    }
                    invitedPlayers.add(players.get(invitationData.getInt("player_id")));
                } while(invitationData.next());
            }
            if(warpData.next()) {
                do {
                    Warp warp = new Warp();
                    warp.setName(warpData.getString("warp.name"));
                    warp.setPublic(warpData.getInt("warp.type")==1);
                    warp.setOwner(players.get(warpData.getInt("warp.player_id")));
                    warp.setInvited(invitations.get(warpData.getInt("warp.warp_id")));
                    result.add(warp);
                } while(warpData.next());
            }
            warpData.close();
        } catch (SQLException throwables) {
            Logger.getLogger(MyWarpDBConnector.class.getName()).log(Level.SEVERE, null, throwables);
            connected = false;
        }
        return result;
    }
    
    public Warp getWarp(ProxiedPlayer player, String name) {
        if(connected) {
            try {
                getWarp.setString(1, addWildcards(name));
                getWarp.setString(2, player.getUniqueId().toString());
                getWarp.setString(3, player.getUniqueId().toString());
                ResultSet result = getWarp.executeQuery();
                if(result.next()) {
                    String world = worldUUID.get(result.getString("world.uuid"));
                    if(world==null) {
                        //world unknown
                        world = "_unknown";
                    }
                    Warp warp = new Warp();
                    warp.setName(result.getString("warp.name"));
                    warp.setWorld(world);
                    warp.setServer(world);
                    warp.setWelcomeMessage(result.getString("warp.welcome_message"));
                    warp.setLocation(result.getDouble("warp.x")+";"
                                   + result.getDouble("warp.y")+";"
                                   + result.getDouble("warp.z")+";"
                                   + result.getFloat("warp.yaw")+";"
                                   + result.getFloat("warp.pitch"));

                    return warp;
                }
                result.close();
            } catch (SQLException ex) {
                Logger.getLogger(MyWarpDBConnector.class.getName()).log(Level.SEVERE, null, ex);
                connected = false;
            }
        }
        return null;
    }
    
    private String addWildcards(String name) {
        String result = "";
        for(int i = 0; i<name.length();i++) {
            String sub = name.substring(i,i+1);
            if(sub.matches("[a-z]|[A-Z]")) {
                result = result + "["+sub.toLowerCase()+sub.toUpperCase()+"]";
            } else if(sub.matches(" |-")) {
                result = result + "[ |-]";
            }
        }
        return result;
    }
    
    public void addWorldUUID(String uuid, String worldName) {
        worldUUID.put(uuid, worldName);
        saveWorldUUIDs();
    }
    
    private void loadWorldUUIDs() {
        if(!worldFile.exists()) {
            return;
        }
        try(Scanner scanner = new Scanner(worldFile)) {
            worldUUID.clear();
            while(scanner.hasNext()) {
                String[] line = scanner.nextLine().split(";");
                worldUUID.put(line[0], line[1]);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MyWarpDBConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void saveWorldUUIDs() {
        try(PrintWriter fw = new PrintWriter(new FileWriter(worldFile))) {
            worldUUID.entrySet().forEach((entry) -> {
                fw.println(entry.getKey()+";"+entry.getValue());
            });
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MyWarpDBConnector.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MyWarpDBConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isConnected() {
        return connected;
    }
}


/*

SELECT warp.name, warp.x, warp.y, warp.z, warp.pitch, warp.yaw, warp.welcome_message, 
       warp.type, world.uuid, owner.uuid, invited.uuid 
  FROM warp JOIN player AS owner ON warp.player_id = owner.player_id 
            JOIN world ON warp.world_id = world.world_id 
            LEFT JOIN warp_player_map ON warp.warp_id = warp_player_map.warp_id 
            LEFT JOIN player AS invited ON warp_player_map.player_id = invited.player_id 
  WHERE warp.name LIKE "%Ere%" AND (owner.uuid = "fedf6ee0-8573-4588-89cf-5951e2596795" OR invited.uuid = "fedf6ee0-8573-4588-89cf-5951e2596795")

SELECT warp.name, warp.x, warp.y, warp.z, warp.pitch, warp.yaw, warp.welcome_message, 
       warp.type, world.uuid, owner.uuid, invited.uuid 
FROM warp JOIN player AS owner ON warp.player_id = owner.player_id 
          JOIN world ON warp.world_id = world.world_id 
          LEFT JOIN warp_player_map ON warp.warp_id = warp_player_map.warp_id 
          LEFT JOIN player AS invited ON warp_player_map.player_id = invited.player_id 
WHERE warp.name REGEXP '[kK][hH][aA][zZ][aA][dD][ |-][dD][uU][mM]*' 
         AND (warp.type = 1 OR owner.uuid = 'fedf6ee0-8573-4588-89cf-5951e2596795' OR invited.uuid = 'fedf6ee0-8573-4588-89cf-5951e2596795')

select warp.name from warp join player using(player_id) 
where player.uuid = "fedf6ee0-8573-4588-89cf-5951e2596795"
*/