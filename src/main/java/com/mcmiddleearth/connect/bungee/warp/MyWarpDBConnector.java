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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.mariadb.jdbc.MySQLDataSource;

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
    
    private final MySQLDataSource dataBase;
    
    private static Connection dbConnection;
    
    private static PreparedStatement getWarp;
    
    private static File worldFile = new File(ConnectBungeePlugin.getInstance().getDataFolder(),"world.uuid");
        
    private static Map<String, String> worldUUID = new HashMap<>();
    
    @Getter
    private boolean connected = false;
    
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    
    public MyWarpDBConnector(Map config) {
        if(config==null) {
            config = new HashMap();
        }
        dbUser = (String) config.get("user");
        dbPassword = (String) config.get("password");//,"mywarp");
        dbName = (String) config.get("dbName");//,"mywarp");
        dbIp = (String) config.get("ip");//, "localhost");
        port = (Integer) config.get("port");//,3306);
        dataBase = new MySQLDataSource(dbIp,port,dbName);
        loadWorldUUIDs();
        executeAsync( player -> {
            connect();
            checkConnection();
        }, null);
    }
    
    private void executeAsync(Consumer<ProxiedPlayer> method, ProxiedPlayer player) {
        ProxyServer.getInstance().getScheduler().runAsync(ConnectBungeePlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                method.accept(player);
            }
        });
    }
    
    private boolean checkConnection() {
        try {
            if(connected && dbConnection.isValid(5)) {
                connected = true;
                return true;
            } else {
                throw new SQLException();
            }
        } catch (SQLException ex) {
            Logger.getLogger(MyWarpDBConnector.class.getName()).log(Level.SEVERE, "No DB connection!!", ex);
            return false;
        }
    }
    
    private void connect() {
        try {
            dbConnection = dataBase.getConnection(dbUser, dbPassword);
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
            connected = true;
        } catch (SQLException ex) {
            Logger.getLogger(MyWarpDBConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static Warp getWarp(ProxiedPlayer player, String name) {
        try {
            getWarp.setString(1, addWildcards(name));
            getWarp.setString(2, player.getUniqueId().toString());
            getWarp.setString(3, player.getUniqueId().toString());
//Logger.getGlobal().info(getWarp.toString());
            ResultSet result = getWarp.executeQuery();
            if(result.first()) {
                //if(!result.isLast()) {
                    //several matching warps found
                //    return null;
                //}
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
        } catch (SQLException ex) {
            Logger.getLogger(MyWarpDBConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private static String addWildcards(String name) {
        String result = "";
        for(int i = 0; i<name.length();i++) {
            String sub = name.substring(i,i+1);
            if(sub.matches("[a-z]|[A-Z]")) {
                result = result + "["+sub.toLowerCase()+sub.toUpperCase()+"]";
            } else if(sub.matches(" |-")) {
                result = result + "[ |-]";
            }
        }
        return "^"+result;
    }
    
    public static void addWorldUUID(String uuid, String worldName) {
        worldUUID.put(uuid, worldName);
        saveWorldUUIDs();
    }
    
    private static void loadWorldUUIDs() {
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
    
    private static void saveWorldUUIDs() {
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