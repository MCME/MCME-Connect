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
package com.mcmiddleearth.connect;

import com.mcmiddleearth.connect.listener.ConnectPluginListener;
import com.mcmiddleearth.connect.listener.PlayerListener;
import com.mcmiddleearth.connect.restart.RestartCommand;
import com.mcmiddleearth.connect.restart.StopCommand;
import com.mcmiddleearth.connect.restart.RestartHandler;
import com.mcmiddleearth.connect.restart.RestartScheduler;
import com.mcmiddleearth.connect.statistics.StatisticDBConnector;
import com.mcmiddleearth.connect.statistics.StatisticListener;
import com.mcmiddleearth.connect.tabList.AfkListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author Eriol_Eandur
 */
public class ConnectPlugin extends JavaPlugin {
    
    private static JavaPlugin instance;
    
    private static StatisticDBConnector statisticStorage;
    
    private static String discordChannel;
    
    private BukkitTask statisticUpdater;
    
    private static RestartScheduler restartScheduler;

    private static BukkitTask serverinfoScheduler;
    
    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        RestartHandler.init();
        if(getConfig().getBoolean("syncStatistic",true)) {
            statisticStorage = new StatisticDBConnector(getConfig().getConfigurationSection("database"));
            Bukkit.getPluginManager().registerEvents(new StatisticListener(), this);
        }
        Bukkit.getServer().getMessenger()
                .registerOutgoingPluginChannel(this, "BungeeCord");
        discordChannel = getConfig().getString("discordChannel","");
        Bukkit.getServer().getMessenger()
                .registerOutgoingPluginChannel(this, Channel.MAIN);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new AfkListener(), this);
        Bukkit.getServer().getMessenger()
                .registerIncomingPluginChannel(this, Channel.MAIN, new ConnectPluginListener());
        restartScheduler = new RestartScheduler();
        serverinfoScheduler = new ServerInfoUpdater().runTaskTimer(this,600, 20);
        getCommand("restart").setExecutor(new RestartCommand());
        getCommand("stop").setExecutor(new StopCommand());
    }
    
    @Override
    public void onDisable() {
        if(statisticStorage != null) {
            Bukkit.getOnlinePlayers().forEach(player-> {
                ConnectPlugin.getStatisticStorage().saveStatisticSync(player);
            });
        }
        ConnectPlugin.getStatisticStorage().disconnect();
        restartScheduler.cancel();
        serverinfoScheduler.cancel();
    }

    public static JavaPlugin getInstance() {
        return instance;
    }

    public static StatisticDBConnector getStatisticStorage() {
        return statisticStorage;
    }

    public static String getDiscordChannel() {
        return discordChannel;
    }

    public static RestartScheduler getRestartScheduler() {
        return restartScheduler;
    }

 }
