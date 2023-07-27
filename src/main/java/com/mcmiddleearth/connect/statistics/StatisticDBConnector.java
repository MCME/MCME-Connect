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
package com.mcmiddleearth.connect.statistics;

import com.mcmiddleearth.connect.ConnectPlugin;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.Statistic;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Eriol_Eandur
 */
public class StatisticDBConnector {

    private final String dbUser;
    private final String dbPassword;
    private final String dbName;
    private final String dbIp;
    private final int port;

    private Connection dbConnection;

    private PreparedStatement updatePlayerStats;
    private PreparedStatement insertPlayerStats;
    private PreparedStatement selectPlayerStats;

    //private PreparedStatement updatePlayerMatStats;
    //private PreparedStatement insertPlayerMatStats;
    private PreparedStatement selectPlayerMatStats;
    private PreparedStatement selectPlayerAllMatStats;

    //private PreparedStatement updatePlayerEntityStats;
    //private PreparedStatement insertPlayerEntityStats;
    private PreparedStatement selectPlayerEntityStats;
    private PreparedStatement selectPlayerAllEntityStats;

    private PreparedStatement selectPlayerId;


    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private BukkitTask keepAliveTask;
    private boolean connected;

    public StatisticDBConnector(ConfigurationSection config) {
        if (config == null) {
            config = new MemoryConfiguration();
        }
        dbUser = config.getString("user", "development");
        dbPassword = config.getString("password", "development");
        dbName = config.getString("dbName", "development");
        dbIp = config.getString("ip", "localhost");
        port = config.getInt("port", 3306);

        connect();
        keepAliveTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkConnection();
            }
        }.runTaskTimerAsynchronously(ConnectPlugin.getInstance(), 0, 1200);
    }

    private void executeAsync(Consumer<Player> method, Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!connected) {
                    if (dbConnection != null) {
                        try {
                            dbConnection.close();
                        } catch (SQLException ex) {
                            Logger.getLogger(StatisticDBConnector.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    connect();
                }
                method.accept(player);
            }
        }.runTaskAsynchronously(ConnectPlugin.getInstance());
    }

    private synchronized void checkConnection() {
        try {
            if (connected && dbConnection.isValid(5)) {
                //ConnectPlugin.getInstance().getLogger().log(Level.INFO,
                //        "Successfully checked connection to statistics database.");
            } else {
                if (dbConnection != null) {
                    dbConnection.close();
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        connect();
                        ConnectPlugin.getInstance().getLogger().log(Level.INFO, "Reconnecting to statistics database.");
                    }
                }.runTaskAsynchronously(ConnectPlugin.getInstance());
            }
        } catch (SQLException ex) {
            Logger.getLogger(StatisticDBConnector.class.getName()).log(Level.SEVERE, null, ex);
            connected = false;
        }
    }

    private synchronized void connect() {
        try {
            dbConnection = DriverManager.getConnection(
                    "jdbc:mysql://"+dbIp+":"+port+"/"+dbName,
                    dbUser, dbPassword);

            checkTables();

            StringBuilder insertGeneral = new StringBuilder("INSERT INTO mcmeconnect_statistic (uuid");
            StringBuilder updateGeneral = new StringBuilder("UPDATE mcmeconnect_statistic SET ");
            StringBuilder selectGeneral = new StringBuilder("SELECT ");

            //String insertMat = "INSERT INTO mcmeconnect_statistic_material (id, material";
            //String updateMat = "UPDATE mcmeconnect_statistic_material SET ";
            StringBuilder selectMat = new StringBuilder("SELECT ");
            StringBuilder selectAllMat = new StringBuilder("SELECT material");

            //String insertEntity = "INSERT INTO mcmeconnect_statistic_entity (id, entity, ?)";
            //String updateEntity = "UPDATE mcmeconnect_statistic_entity SET ? = ?";
            StringBuilder selectEntity = new StringBuilder("SELECT ");
            StringBuilder selectAllEntity = new StringBuilder("SELECT entity");

            String generalSeparator = "";
            String materialSeparator = "";
            String entitySeparator = "";
            for (Statistic stat : Statistic.values()) {
                switch (stat.getType()) {
                    case UNTYPED:
                        insertGeneral.append(", ").append(stat.name());
                        updateGeneral.append(generalSeparator).append(stat.name()).append("=?");
                        selectGeneral.append(generalSeparator).append(stat.name());
                        generalSeparator = ", ";
                        break;
                    case BLOCK:
                    case ITEM:
                        selectMat.append(materialSeparator).append(getName(stat));
                        selectAllMat.append(", ").append(getName(stat));
                        materialSeparator = ", ";
                        break;
                    case ENTITY:
                        selectEntity.append(entitySeparator).append(stat.name());
                        selectAllEntity.append(", ").append(stat.name());
                        entitySeparator = ", ";
                        break;
                }
            }
            updateGeneral.append(" WHERE uuid = ?");
            insertGeneral.append(") VALUES (?");
            selectGeneral.append(" FROM mcmeconnect_statistic WHERE uuid = ?");

            //updateMat = updateMat + " WHERE id = ?, material = ?";
            //insertMat = insertMat + ") VALUES (?, ?";
            selectMat.append(" FROM mcmeconnect_statistic_material WHERE id = ? AND material = ?");
            selectAllMat.append(" FROM mcmeconnect_statistic_material WHERE id = ?");

            selectEntity.append(" FROM mcmeconnect_statistic_entity WHERE id = ? AND entity = ?");
            selectAllEntity.append(" FROM mcmeconnect_statistic_entity WHERE id = ?");

            for (Statistic stat : Statistic.values()) {
                if (stat.getType() == Statistic.Type.UNTYPED) {
                    insertGeneral.append(", ?");
                }
            }
            insertGeneral.append(")");
            //insertMat = insertMat + ")";
            insertPlayerStats = dbConnection.prepareStatement(insertGeneral.toString());
            updatePlayerStats = dbConnection.prepareStatement(updateGeneral.toString());
            selectPlayerStats = dbConnection.prepareStatement(selectGeneral.toString());
            insertPlayerStats.setQueryTimeout(10);
            updatePlayerStats.setQueryTimeout(10);
            selectPlayerStats.setQueryTimeout(10);

            //insertPlayerMatStats = dbConnection.prepareStatement(insertMat);
            //updatePlayerMatStats = dbConnection.prepareStatement(updateMat);
            selectPlayerMatStats = dbConnection.prepareStatement(selectMat.toString());
            selectPlayerAllMatStats = dbConnection.prepareStatement(selectAllMat.toString());
            selectPlayerStats.setQueryTimeout(10);
            selectPlayerStats.setQueryTimeout(10);

            selectPlayerEntityStats = dbConnection.prepareStatement(selectEntity.toString());
            selectPlayerAllEntityStats = dbConnection.prepareStatement(selectAllEntity.toString());
            selectPlayerEntityStats.setQueryTimeout(10);
            selectPlayerAllEntityStats.setQueryTimeout(10);

            selectPlayerId = dbConnection
                    .prepareStatement("SELECT id FROM mcmeconnect_statistic WHERE uuid = ?");
            selectPlayerId.setFetchSize(1);
            selectPlayerId.setQueryTimeout(10);

            connected = true;
        } catch (SQLException ex) {
            Logger.getLogger(StatisticDBConnector.class.getName()).log(Level.SEVERE, null, ex);
            connected = false;
        }
    }

    public synchronized void disconnect() {
        connected = false;
        if (keepAliveTask != null) {
            keepAliveTask.cancel();
        }
        if (dbConnection != null) {
            try {
                updatePlayerStats.close();
                insertPlayerStats.close();
                selectPlayerStats.close();
                selectPlayerMatStats.close();
                selectPlayerAllMatStats.close();
                selectPlayerEntityStats.close();
                selectPlayerAllEntityStats.close();
                selectPlayerId.close();
                dbConnection.close();
            } catch (SQLException ex) {
                Logger.getLogger(StatisticDBConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private synchronized void checkTablesSync() {
        try {
            Logger.getLogger(ConnectPlugin.class.getName()).info("checking tables...");
            StringBuilder statement = new StringBuilder("CREATE TABLE IF NOT EXISTS mcmeconnect_statistic (uuid VARCHAR(50), id INT AUTO_INCREMENT");
            for (Statistic stat : Statistic.values()) {
                if (stat.getType().equals(Statistic.Type.UNTYPED)) {
                    statement.append(", ").append(stat.name()).append(" INT");
                }
            }
            statement.append(", KEY(id))");
            dbConnection.createStatement().execute(statement.toString());
            PreparedStatement checkColumns = dbConnection.prepareStatement("SELECT * FROM mcmeconnect_statistic");
            checkColumns.setFetchSize(1);
            ResultSet result = checkColumns.executeQuery();
            if (result.next()) {
                for (Statistic stat : Statistic.values()) {
                    if (stat.getType().equals(Statistic.Type.UNTYPED)) {
                        try {
                            result.findColumn(stat.name());
                        } catch (SQLException ex) {
                            Logger.getLogger(ConnectPlugin.class.getName()).info("add column " + stat.name());
                            statement = new StringBuilder("ALTER TABLE mcmeconnect_statistic ADD COLUMN "
                                    + stat.name() + " INT");
                            dbConnection.createStatement().execute(statement.toString());
                        }
                    }
                }
            }
            result.close();

            statement = new StringBuilder("CREATE TABLE IF NOT EXISTS mcmeconnect_statistic_material (id INT, material VARCHAR(50)");
            for (Statistic stat : Statistic.values()) {
                switch (stat.getType()) {
                    case BLOCK:
                    case ITEM:
                        statement.append(", ").append(getName(stat)).append(" INT");
                        break;
                }
            }
            statement.append(")");
            dbConnection.createStatement().execute(statement.toString());
            checkColumns = dbConnection.prepareStatement("SELECT * FROM mcmeconnect_statistic_material");
            checkColumns.setFetchSize(1);
            result = checkColumns.executeQuery();
            if (result.next()) {
                for (Statistic stat : Statistic.values()) {
                    if (stat.getType().equals(Statistic.Type.BLOCK)
                            || stat.getType().equals(Statistic.Type.ITEM)) {
                        try {
                            result.findColumn(getName(stat));
                        } catch (SQLException ex) {
                            Logger.getLogger(ConnectPlugin.class.getName()).info("add column " + stat.name());
                            statement = new StringBuilder("ALTER TABLE mcmeconnect_statistic_material ADD COLUMN "
                                    + getName(stat) + " INT");
                            dbConnection.createStatement().execute(statement.toString());
                        }
                    }
                }
            }
            result.close();

            statement = new StringBuilder("CREATE TABLE IF NOT EXISTS mcmeconnect_statistic_entity (id INT, entity VARCHAR(50)");
            for (Statistic stat : Statistic.values()) {
                if (stat.getType() == Statistic.Type.ENTITY) {
                    statement.append(", ").append(stat.name()).append(" INT");
                }
            }
            statement.append(")");
            dbConnection.createStatement().execute(statement.toString());
            checkColumns = dbConnection.prepareStatement("SELECT * FROM mcmeconnect_statistic_entity");
            checkColumns.setFetchSize(1);
            result = checkColumns.executeQuery();
            if (result.next()) {
                for (Statistic stat : Statistic.values()) {
                    if (stat.getType().equals(Statistic.Type.ENTITY)) {
                        try {
                            result.findColumn(stat.name());
                        } catch (SQLException ex) {
                            Logger.getLogger(ConnectPlugin.class.getName()).info("add column " + stat.name());
                            statement = new StringBuilder("ALTER TABLE mcmeconnect_statistic_entity ADD COLUMN "
                                    + stat.name() + " INT");
                            dbConnection.createStatement().execute(statement.toString());
                        }
                    }
                }
            }
            result.close();
        } catch (SQLException ex) {
            Logger.getLogger(StatisticDBConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void checkTables() {
        executeAsync(player -> checkTablesSync(), null);
    }

    private synchronized void loadStatisticSync(Player player) {
            try {
                selectPlayerStats.setString(1, player.getUniqueId().toString());
                ResultSet result = selectPlayerStats.executeQuery();
                if(result.next()) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            try {
                                for(Statistic stat : Statistic.values()) {
                                    if(stat.getType().equals(Statistic.Type.UNTYPED)) {
                                        int value = result.getInt(stat.name());
                                        player.setStatistic(stat, value);
                                    }
                                }
                                result.close();
                            } catch (SQLException ex) {
                                Logger.getLogger(StatisticDBConnector.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }.runTask(ConnectPlugin.getInstance());
                } else {
                    result.close();
                }
                int id = getPlayerId(player.getUniqueId());
                if(id>=0) {
                    selectPlayerAllMatStats.setInt(1, id);
                    ResultSet matResult = selectPlayerAllMatStats.executeQuery();
                    if(matResult.next()) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                try {
                                    do {
                                        Material mat;
                                        boolean save = false;
                                        try {
                                            mat = Material.valueOf(matResult.getString("material"));
                                        } catch(IllegalArgumentException ex) {
                                            try {
                                                mat = mappingOf(matResult.getString("material"));
                                                save = true;
                                            } catch (IllegalArgumentException ex2) {
                                                mat = null;
                                            }
                                        }
                                        if(mat != null) {
                                            for(Statistic stat : Statistic.values()) {
                                                if(stat.getType().equals(Statistic.Type.BLOCK)
                                                        ||stat.getType().equals(Statistic.Type.ITEM)) {
                                                    int value = matResult.getInt(getName(stat));
                                                    if(value > 0) {
                                                        try {
                                                            player.setStatistic(stat, mat, value);
                                                            if(save) {
                                                                saveMaterialStatsSync(player, stat, mat, value);
                                                            }
                                                        } catch(IllegalArgumentException ex) {
                                                            Logger.getLogger(StatisticDBConnector.class.getName()).log(Level.WARNING, null, ex);
                                                        }
                                                    }
                                                }
                                            }
                                            if(save) {
                                                deleteMatStat(id, matResult.getString("material"));
                                            }
                                        }
                                    } while(matResult.next());
                                    matResult.close();
                                } catch (SQLException ex) {
                                    Logger.getLogger(StatisticDBConnector.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }.runTask(ConnectPlugin.getInstance());
                    } else {
                        result.close();
                    }
                    selectPlayerAllEntityStats.setInt(1, id);
                    ResultSet entityResult = selectPlayerAllEntityStats.executeQuery();
                    if(entityResult.next()) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                try {
                                    do {
                                        EntityType entity = EntityType.valueOf(entityResult.getString("entity"));
                                        for(Statistic stat : Statistic.values()) {
                                            if(stat.getType().equals(Statistic.Type.ENTITY)) {
                                                int value = entityResult.getInt(stat.name());
                                                if(value>0) {
                                                    player.setStatistic(stat, entity, value);   
                                                }
                                            }
                                        }
                                    } while(entityResult.next());
                                    entityResult.close();
                                } catch (SQLException ex) {
                                    Logger.getLogger(StatisticDBConnector.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }.runTask(ConnectPlugin.getInstance());
                    } else {
                        result.close();
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(StatisticDBConnector.class.getName()).log(Level.SEVERE, null, ex);
                connected = false;
            }
    }

    public void loadStatistic(Player p) {
        executeAsync(this::loadStatisticSync, p);
    }

    public void loadStaticstic(Player p, Consumer<Player> callback) {
        executeAsync(player -> {
            loadStatisticSync(player);
            new BukkitRunnable() {
                @Override
                public void run() {
                    callback.accept(player);
                }
            }.runTask(ConnectPlugin.getInstance());
        }, p);
    }

    public synchronized void saveStatisticSync(Player player) {
        try {
            selectPlayerStats.setString(1, player.getUniqueId().toString());
            ResultSet result = selectPlayerStats.executeQuery();
            if (result.next()) {
                updateStats(player, result);
            } else {
                insertStats(player);
            }
            result.close();
        } catch (SQLException ex) {
            Logger.getLogger(StatisticDBConnector.class.getName()).log(Level.SEVERE, null, ex);
            connected = false;
        }
    }

    public void saveStatistic(Player p) {
        executeAsync(this::saveStatisticSync, p);
    }

    public void saveStaticstic(Player p, Consumer<Player> callback) {
        executeAsync(player -> {
            saveStatisticSync(player);
            new BukkitRunnable() {
                @Override
                public void run() {
                    callback.accept(player);
                }
            }.runTask(ConnectPlugin.getInstance());
        }, p);
    }

    private synchronized void updateStats(Player player, ResultSet result) throws SQLException {
        int i = 1;
        for (Statistic stat : Statistic.values()) {
            if (stat.getType().equals(Statistic.Type.UNTYPED)) {
                int saved = result.getInt(stat.name());
                int current = player.getStatistic(stat);
                updatePlayerStats.setInt(i, Math.max(saved, current) );
                i++;
            }
        }
        updatePlayerStats.setString(i, player.getUniqueId().toString());
        updatePlayerStats.executeUpdate();
    }

    private synchronized void insertStats(Player player) throws SQLException {
        insertPlayerStats.setString(1, player.getUniqueId().toString());
        int i = 2;
        for (Statistic stat : Statistic.values()) {
            if (stat.getType().equals(Statistic.Type.UNTYPED)) {
                insertPlayerStats.setInt(i, player.getStatistic(stat));
                i++;
            }
        }
        insertPlayerStats.executeUpdate();
    }

    private synchronized void saveMaterialStatsSync(Player player, Statistic stat, Material mat, int value) {
        try {
            int id = getPlayerId(player.getUniqueId());
            if (id >= 0) {
                selectPlayerMatStats.setInt(1, id);
                selectPlayerMatStats.setString(2, mat.name());
                ResultSet result = selectPlayerMatStats.executeQuery();
                if (result.next()) {
                    int saved = result.getInt(getName(stat));
                    if(saved < value) {
                        updateMatStat(id, stat, mat, value);
                    }
                } else {
                    insertMatStat(id, stat, mat, value);
                }
                result.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(StatisticDBConnector.class.getName()).log(Level.SEVERE, null, ex);
            connected = false;
        }
    }

    public void saveMaterialStats(Player p, Statistic stat,
                                  Material mat, int value) {
        executeAsync(player -> saveMaterialStatsSync(player, stat, mat, value), p);
    }

    private synchronized void updateMatStat(int id, Statistic stat, Material mat, int value) throws SQLException {
        String statement = "UPDATE mcmeconnect_statistic_material SET " + getName(stat)
                + " = " + value + " WHERE id = " + id + " AND material = '" + mat.name() + "'";
        dbConnection.createStatement().execute(statement);
    }

    private synchronized void insertMatStat(int id, Statistic stat, Material mat, int value) throws SQLException {
        String statement = "INSERT INTO mcmeconnect_statistic_material (id, material, "+getName(stat)
                +") VALUES (" + id + ", '"+ mat.name()+"', "+ value + ")";
        dbConnection.createStatement().execute(statement);
    }

    private synchronized void deleteMatStat(int id, String mat) throws SQLException {
        String statement = "DELETE FROM mcmeconnect_statistic_material "
                +"WHERE id = "+id+" AND material = '"+ mat + "'";
        dbConnection.createStatement().execute(statement);
    }

    private synchronized void saveEntityStatsSync(Player player, Statistic stat,
                                      EntityType entity, int value) {
        try {
            int id = getPlayerId(player.getUniqueId());
            if(id>=0) {
                selectPlayerEntityStats.setInt(1, id);
                selectPlayerEntityStats.setString(2, entity.name());
                ResultSet result = selectPlayerEntityStats.executeQuery();
                if(result.next()) {
                    result.close();
                    updateEntityStat(id, stat, entity, value);
                } else {
                    result.close();
                    insertEntityStat(id, stat, entity, value);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(StatisticDBConnector.class.getName()).log(Level.SEVERE, null, ex);
            connected = false;
        }
    }

    public void saveEntityStats(Player p, Statistic stat,
                                EntityType entity, int value) {
        executeAsync(player -> saveEntityStatsSync(player, stat, entity, value), p);
    }

    private synchronized void updateEntityStat(int id, Statistic stat, EntityType entity, int value) throws SQLException {
        String statement = "UPDATE mcmeconnect_statistic_entity SET " + stat.name()
                + " = " + value + " WHERE id = " + id + " AND entity = '" + entity.name() + "'";
        dbConnection.createStatement().execute(statement);
    }

    private synchronized void insertEntityStat(int id, Statistic stat, EntityType entity, int value) throws SQLException {
        String statement = "INSERT INTO mcmeconnect_statistic_entity (id, entity, " + stat.name()
                + ") VALUES (" + id + ", '" + entity.name() + "', " + value + ")";
        dbConnection.createStatement().execute(statement);
    }

    private synchronized int getPlayerId(UUID uuid) throws SQLException {
        selectPlayerId.setString(1, uuid.toString());
        ResultSet result = selectPlayerId.executeQuery();
        int playerId = -1;
        if (result.next()) {
            playerId = result.getInt("id");
        }
        result.close();
        return playerId;
    }
    
    public String getName(Statistic stat) throws IllegalArgumentException{
        return (stat.name().equals("DROP")?"DROP_":stat.name());
    }

    private Material mappingOf(String name) {
        String[] split = name.split("_");
        String result = "";
        if(   split[split.length-1].equals("BLOCK")
           || split[split.length-1].equals("ITEM")) {
            for(int i = 0; i<split.length-1; i++) {
                if(!result.equals("")) {
                    result = result+"_";
                }
                result = result + split[i];
            }
            return Material.valueOf(result);
        }
        return null;
    }
}
