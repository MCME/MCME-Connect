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
package com.mcmiddleearth.connect.proxy.core.handler;

import com.mcmiddleearth.base.core.message.McmeColors;
import com.mcmiddleearth.base.core.message.MessageColor;
import com.mcmiddleearth.base.core.player.McmeProxyPlayer;
import com.mcmiddleearth.base.core.server.McmeServerInfo;
import com.mcmiddleearth.connect.proxy.core.McmeConnect;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Eriol_Eandur
 */
public class RestorestatsHandler {

    private static final Set<UUID> blacklist = new HashSet<>();
    
    private static String getBasePath() {
        String path = McmeConnect.getConfig().getRestorestatsBasePath();
        if(path.isEmpty()) {
            McmeConnect.getLogger().warn("restorestatsBasePath is not configured in config.yml. Restorestats feature is disabled.");
            return null;
        }
        if(!path.endsWith("/")) {
            path = path + "/";
        }
        return path;
    }
    
    public static void handle(McmeProxyPlayer player, String[] message) {
        boolean joinOnly = message.length>1 && message[1].equalsIgnoreCase("joinDateOnly");
        boolean restoreAll = message.length>1 && message[1].equalsIgnoreCase("allStats");
        String bungeeBase = getBasePath();
        if(bungeeBase == null) {
            player.sendMessage(McmeConnect.errorMessage("Restorestats feature is not configured. Please contact an admin."));
            return;
        }
        String backupFolder = bungeeBase + "oldplayerstats-data/backupOfRestoredPlayerdata";
        String newplayerServerFolder = bungeeBase + "servers-mcme/newplayer/newplayer";
        String restoreFolder = bungeeBase + "oldplayerstats-data";
        String serverPlayerStats = bungeeBase + "servers-mcme/" + "<server>/<world>/stats";
        if(!joinOnly && !restoreAll) {
            player.sendMessage(McmeConnect.infoMessage(
                    "This command will reset your playerstats to the values you had at Nov 2nd 2019 when the MCME Bungee network was implemented. "
                   +"The command will disconnect you from the server for a minute to restore the data. If you really want to do this use: \n"
                   +"-'")
                .add("/restorestats joinDateOnly", McmeColors.INFO_STRESSED)
                .add(" to keep your stats and restore your first join date only.\n"
                        +"OR\n"
                        +"-'")
                .add("/restorestats allStats", McmeColors.INFO_STRESSED)
                .add("' to reset all your stats including your first join date. "));
            return;
        }
        UUID uuid = player.getUniqueId();
        Path backupPlayerDataFile = Paths.get(backupFolder+"/playerdata/"+uuid.toString()+".dat");
        if(Files.exists(backupPlayerDataFile)) {
            player.sendMessage(McmeConnect.errorMessage(
                        "Your player stats were already restored. If you think this is an error, please contact an admin."));
            return;
        }
        Path restorePlayerDataFile = Paths.get(restoreFolder+"/playerdata/"+uuid+".dat");
        if(!Files.exists(restorePlayerDataFile)) {
            player.sendMessage(McmeConnect.errorMessage(
                        "There is no backup of your playerdata to restore If you think this is an error, please contact an admin."));
            return;
        }
        Path restoreStatsFile = Paths.get(restoreFolder+"/stats/"+uuid+".json");
        if(!joinOnly && !Files.exists(restoreStatsFile)) {
            player.sendMessage(McmeConnect.errorMessage(
                    "There is no backup of your stats to restore If you think this is an error, please contact an admin."));
            return;
        }
        player.disconnect(McmeConnect.message("Restoring your statistics. Please wait a minute before rejoining.",
                                                MessageColor.WHITE));
        blacklist.add(player.getUniqueId());
        McmeConnect.getProxyPlugin().getTask( () -> {
            try {
                Path serverPlayerDataFile = Paths.get(newplayerServerFolder+"/playerdata/"+uuid+".dat");
                Files.copy(serverPlayerDataFile, backupPlayerDataFile);

                Files.copy(restorePlayerDataFile, serverPlayerDataFile,StandardCopyOption.REPLACE_EXISTING);

                if(!joinOnly) {
                    Path backupStatsFile = Paths.get(backupFolder+"/stats/"+uuid+".json");
                    Path serverStatsFile = Paths.get(newplayerServerFolder+"/stats/"+uuid+".json");
                    Files.copy(serverStatsFile, backupStatsFile);

                    for(String server: McmeConnect.getProxy().getAllServerInfo().stream().map(McmeServerInfo::getName).toList()) {
                        String filename = serverPlayerStats+"/"+uuid+".json";
                        filename = filename.replace("<world>", server);
                        filename = filename.replace("<server>", (server.equals("world")?"mainworld":server));
                        serverStatsFile = Paths.get(filename);
                        Files.copy(restoreStatsFile,serverStatsFile,StandardCopyOption.REPLACE_EXISTING);
                    }
                    resetStatistics(player);
                }
            } catch (IOException ex) {
                McmeConnect.getProxyPlugin().getMcmeLogger().error( "IOException", ex);
            }
            blacklist.remove(player.getUniqueId());
        }).schedule(5, TimeUnit.SECONDS);
    }
    
    public static void resetStatistics(McmeProxyPlayer player) {
        try {
            Map<String,Object> config = McmeConnect.getConfig().getDatabaseConfig();
            String dbUser = (String) config.get("user");
            String dbPassword = (String) config.get("password");
            String dbName = (String) config.get("dbName");
            String dbIp = (String) config.get("ip");
            int port = (Integer) config.get("port");
            //MySQLDataSource dataBase = new MySQLDataSource(dbIp,port,dbName);
            try (Connection dbConnection = DriverManager.getConnection(
                    "jdbc:mysql://"+dbIp+":"+port+"/"+dbName,
                    dbUser, dbPassword)) {
                try (PreparedStatement selectStmt = dbConnection.prepareStatement(
                        "SELECT id FROM mcmeconnect_statistic WHERE uuid = ?")) {
                    selectStmt.setString(1, player.getUniqueId().toString());
                    ResultSet result = selectStmt.executeQuery();
                    if(result.first()) {
                        int id = result.getInt(1);
                        result.close();
                        try (PreparedStatement deleteStats = dbConnection.prepareStatement(
                                "DELETE FROM mcmeconnect_statistic WHERE uuid = ?")) {
                            deleteStats.setString(1, player.getUniqueId().toString());
                            deleteStats.executeUpdate();
                        }
                        try (PreparedStatement deleteEntity = dbConnection.prepareStatement(
                                "DELETE FROM mcmeconnect_statistic_entity WHERE id = ?")) {
                            deleteEntity.setInt(1, id);
                            deleteEntity.executeUpdate();
                        }
                        try (PreparedStatement deleteMat = dbConnection.prepareStatement(
                                "DELETE FROM mcmeconnect_statistic_material WHERE id = ?")) {
                            deleteMat.setInt(1, id);
                            deleteMat.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            McmeConnect.getProxyPlugin().getMcmeLogger().error( "SQLException", ex);
        }
        
    }

    public static Set<UUID> getBlacklist() {
        return blacklist;
    }
}
