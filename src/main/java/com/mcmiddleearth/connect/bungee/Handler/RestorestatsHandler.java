/*
 * Copyright (C) 2019 Eriol_Eandur
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
package com.mcmiddleearth.connect.bungee.Handler;

import com.mcmiddleearth.connect.bungee.ConnectBungeePlugin;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eriol_Eandur
 */
public class RestorestatsHandler {

    private static Set<UUID> blacklist = new HashSet<>();
    
    //private static String bungeeBase = "/home/devserver/dev-bungee/";
    private static String bungeeBase = "/media/ssd1/bungee-mcme/";
    
    private static String backupFolder = bungeeBase+"oldplayerstats-data/backupOfRestoredPlayerdata";
    private static String newplayerServerFolder = bungeeBase+"servers-mcme/newplayer/newplayer";
    private static String restoreFolder = bungeeBase+"oldplayerstats-data";
    
    private static String serverPlayerStats = bungeeBase+"servers-mcme/"+"<server>/<world>/stats";
    
    public static void handle(ProxiedPlayer player, String[] message) {
        boolean joinOnly = message.length>1 && message[1].equalsIgnoreCase("joinDateOnly");
        boolean restoreAll = message.length>1 && message[1].equalsIgnoreCase("allStats");
        if(!joinOnly && !restoreAll) {
            player.sendMessage(new ComponentBuilder(
                                    "This command will reset your playerstats to the values you had at Nov 2nd 2019 when the MCME Bungee network was implemented. "
                                   +"The command will disconnect you from the server for a minute to restore the data. If you really want to do this use: \n"
                                   +"-'"+ChatColor.WHITE+"/restorestats joinDateOnly"+ChatColor.GOLD+"' to keep your stats and restore your first join date only.\n"
                                   +"OR\n"
                                   +"-'"+ChatColor.WHITE+"/restorestats allStats"+ChatColor.GOLD+"' to reset all your stats including your first join date. ")
                                    .color(ChatColor.GOLD).create());
            return;
        }
        UUID uuid = player.getUniqueId();
        Path backupPlayerDataFile = Paths.get(backupFolder+"/playerdata/"+uuid.toString()+".dat");
        if(Files.exists(backupPlayerDataFile)) {
            player.sendMessage(new ComponentBuilder(
                                    "Your player stats were already restored. If you think this is an error, please contact an admin.")
                                    .color(ChatColor.RED).create());
            return;
        }
        Path restorePlayerDataFile = Paths.get(restoreFolder+"/playerdata/"+uuid.toString()+".dat");
        if(!Files.exists(restorePlayerDataFile)) {
            player.sendMessage(new ComponentBuilder(
                                    "There is no backup of your playerdata to restore If you think this is an error, please contact an admin.")
                                    .color(ChatColor.RED).create());
            return;
        }
        Path restoreStatsFile = Paths.get(restoreFolder+"/stats/"+uuid.toString()+".json");
        if(!joinOnly && !Files.exists(restoreStatsFile)) {
            player.sendMessage(new ComponentBuilder(
                                    "There is no backup of your stats to restore If you think this is an error, please contact an admin.")
                                    .color(ChatColor.RED).create());
            return;
        }
        player.disconnect(new ComponentBuilder(
                                "Restoring your statistics. Please wait a minute before rejoining.")
                                .color(ChatColor.WHITE).create());
        blacklist.add(player.getUniqueId());
        ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
            try {
                Path serverPlayerDataFile = Paths.get(newplayerServerFolder+"/playerdata/"+uuid.toString()+".dat");
                Files.copy(serverPlayerDataFile, backupPlayerDataFile);

                Files.copy(restorePlayerDataFile, serverPlayerDataFile,StandardCopyOption.REPLACE_EXISTING);

                if(!joinOnly) {
                    Path backupStatsFile = Paths.get(backupFolder+"/stats/"+uuid.toString()+".json");
                    Path serverStatsFile = Paths.get(newplayerServerFolder+"/stats/"+uuid.toString()+".json");
                    Files.copy(serverStatsFile, backupStatsFile);

                    for(String server: ProxyServer.getInstance().getServers().keySet()) {
                        String filename = serverPlayerStats+"/"+uuid.toString()+".json";
                        filename = filename.replace("<world>", server);
                        filename = filename.replace("<server>", (server.equals("world")?"mainworld":server));
                        serverStatsFile = Paths.get(filename);
                        Files.copy(restoreStatsFile,serverStatsFile,StandardCopyOption.REPLACE_EXISTING);
                    }
                    resetStatistics(player);
                }
            } catch (IOException ex) {
                Logger.getLogger(RestorestatsHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            blacklist.remove(player.getUniqueId());
        }, 5, TimeUnit.SECONDS);
    }
    
    public static void resetStatistics(ProxiedPlayer player) {
        try {
            Map config = ConnectBungeePlugin.getConfig().getSection("database");
            String dbUser = (String) config.get("user");
            String dbPassword = (String) config.get("password");
            String dbName = (String) config.get("dbName");
            String dbIp = (String) config.get("ip");
            int port = (Integer) config.get("port");
            //MySQLDataSource dataBase = new MySQLDataSource(dbIp,port,dbName);
            try (Connection dbConnection = DriverManager.getConnection(
                    "jdbc:mysql://"+dbIp+":"+port+"/"+dbName,
                    dbUser, dbPassword)) {
                ResultSet result = dbConnection.createStatement()
                        .executeQuery("SELECT id FROM mcmeconnect_statistic "
                                + "WHERE uuid = '"+player.getUniqueId().toString()+"'");
                if(result.first()) {
                    int id = result.getInt(1);
                    result.close();
                    dbConnection.createStatement()
                            .execute("DELETE FROM mcmeconnect_statistic "
                                    + "WHERE uuid = '"+player.getUniqueId().toString()+"'");
                    dbConnection.createStatement()
                            .execute("DELETE FROM mcmeconnect_statistic_entity "
                                    + "WHERE id = "+id);
                    dbConnection.createStatement()
                            .execute("DELETE FROM mcmeconnect_statistic_material "
                                    + "WHERE id = "+id);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(RestorestatsHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    public static Set<UUID> getBlacklist() {
        return blacklist;
    }
}
