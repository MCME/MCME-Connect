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
package com.mcmiddleearth.connect.bungee;

/**
 *
 * @author Eriol_Eandur
 */

import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.bungee.Handler.RestartHandler;
import com.mcmiddleearth.connect.bungee.Handler.TpaHandler;
import com.mcmiddleearth.connect.bungee.Handler.TpahereHandler;
import com.mcmiddleearth.connect.bungee.listener.CommandListener;
import com.mcmiddleearth.connect.bungee.listener.ConnectionListener;
import com.mcmiddleearth.connect.bungee.listener.PluginMessageListener;
import com.mcmiddleearth.connect.bungee.listener.TestListener;
import com.mcmiddleearth.connect.bungee.tabList.TabViewCommand;
import com.mcmiddleearth.connect.bungee.tabList.TabViewManager;
import com.mcmiddleearth.connect.bungee.tabList.playerItem.PlayerItemUpdater;
import com.mcmiddleearth.connect.bungee.vanish.VanishHandler;
import com.mcmiddleearth.connect.bungee.vanish.VanishListener;
import com.mcmiddleearth.connect.bungee.warp.MyWarpDBConnector;
import com.mcmiddleearth.connect.bungee.watchdog.ServerWatchdog;
import com.mcmiddleearth.connect.log.BungeeLog;
import com.mcmiddleearth.connect.log.Log;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectBungeePlugin extends Plugin {
    
    private static ConnectBungeePlugin instance;
    
    private static int connectDelay = 200;
    private static Set<UUID> legacyPlayers = new HashSet<>();
    private static boolean legacyRedirectEnabled = true;
    private static String legacyRedirectFrom = "newplayerworld";
    private static String legacyRedirectTo = "world";
    
    private static ServerWatchdog watcher;
    
    private static final YamlConfiguration config = new YamlConfiguration();
            
    private static File configFile;
    
    private static final Set<String> noMVTP = new HashSet<>();
          
    private static PlayerItemUpdater playerItemUpdater;

    private static MyWarpDBConnector myWarpConnector;
    
    private static boolean myWarpEnabled;
    
    private RestartScheduler restartScheduler;
    private ScheduledTask tpaCleanupScheduler;
    private ScheduledTask tpahereCleanupScheduler;

    private TabViewCommand tabViewCommand;

    private final Map<String,ServerInformation> serverInformation = new HashMap<>();

    private Log logger;
    
    @Override
    public void onEnable() {
        instance = this;
        configFile = new File(getDataFolder(),"config.yml");
        saveDefaultConfig(configFile, "config.yml");
        loadConfig();
        logger = new BungeeLog();
        RestartHandler.init();
        tpaCleanupScheduler = TpaHandler.startCleanupScheduler();
        tpahereCleanupScheduler = TpahereHandler.startCleanupScheduler();
        restartScheduler = new RestartScheduler();
        if(config.getBoolean("serverWatchdog", true)) {
            watcher = new ServerWatchdog();
        }
        loadLegacyPlayers();
        VanishHandler.setPvSupport(config.getBoolean("premiumVanish", false));
        myWarpEnabled = (Boolean) getConfig().getSection("myWarp").get("enabled");
        if(myWarpEnabled) {
            myWarpConnector = new MyWarpDBConnector(getConfig().getSection("myWarp"));
        }
        if(VanishHandler.isPvSupport()) {
            VanishHandler.loadVanished();
            getProxy().getPluginManager().registerListener(this, new VanishListener());
        }
        ProxyServer.getInstance().registerChannel(Channel.MAIN);
        //getProxy().getPluginManager().registerListener(this, new TestListener());
        getProxy().getPluginManager().registerListener(this, new PluginMessageListener());
        getProxy().getPluginManager().registerListener(this, new CommandListener());
        getProxy().getPluginManager().registerListener(this, 
                         new ConnectionListener());
        getProxy().getPluginManager().registerListener(this, new TabViewManager());
        playerItemUpdater = new PlayerItemUpdater();
        TabViewManager.init();
        tabViewCommand = new TabViewCommand();
        ProxyServer.getInstance().getPluginManager().registerCommand(this, tabViewCommand);
    }
    
    @Override
    public void onDisable() {
        watcher.stopWatchdog();
        myWarpConnector.disconnect();
        restartScheduler.cancel();
        tpaCleanupScheduler.cancel();
        tpahereCleanupScheduler.cancel();
        playerItemUpdater.disable();
        logger.disable();
    }
    
    public static boolean isMvtpDisabled(String server) {
        return noMVTP.contains(server);
    }
    
    private void loadLegacyPlayers() {
        if(!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File file = new File(getDataFolder(),"legacyPlayer.uid");
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(ConnectBungeePlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try(Scanner scanner = new Scanner(file))
        {
            while(scanner.hasNext()) {
                legacyPlayers.add(UUID.fromString(scanner.nextLine()));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConnectBungeePlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void loadConfig() {
        config.load(configFile);
        legacyRedirectEnabled = config.getBoolean("legacyRedirect.enabled",true);
        legacyRedirectFrom = config.getString("legacyRedirect.from","newPlayer");
        legacyRedirectTo = config.getString("legacyRedirect.to","build");
        noMVTP.addAll(config.getStringList("disableMVTP"));
        connectDelay = config.getInt("connectDelay",200);
    }
    
    public void saveDefaultConfig(File configFile, String resource) {
        if(!configFile.exists()) {
            try {
                configFile.createNewFile();
                try(InputStreamReader in = new InputStreamReader(getResourceAsStream(resource));
                    FileWriter fw = new FileWriter(configFile)) {
                    char[] buf = new char[1024];
                    int read = 1;
                    while(read > 0) {
                        read = in.read(buf);
                        if(read>0) 
                            fw.write(buf,0,read);
                    }
                    fw.flush();
                    fw.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ConnectBungeePlugin.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
    }

    public static ConnectBungeePlugin getInstance() {
        return instance;
    }

    public static int getConnectDelay() {
        return connectDelay;
    }

    public static Set<UUID> getLegacyPlayers() {
        return legacyPlayers;
    }

    public static boolean isLegacyRedirectEnabled() {
        return legacyRedirectEnabled;
    }

    public static String getLegacyRedirectFrom() {
        return legacyRedirectFrom;
    }

    public static String getLegacyRedirectTo() {
        return legacyRedirectTo;
    }

    public static ServerWatchdog getWatcher() {
        return watcher;
    }

    public static YamlConfiguration getConfig() {
        return config;
    }

    public static Set<String> getNoMVTP() {
        return noMVTP;
    }

    public static MyWarpDBConnector getMyWarpConnector() {
        return myWarpConnector;
    }

    public static boolean isMyWarpEnabled() {
        return myWarpEnabled;
    }

    public ServerInformation getServerInformation(String name) {
        ServerInformation info =  serverInformation.get(name);
        if(info==null) {
            info = new ServerInformation(name);
            serverInformation.put(name,info);
        }
        return info;
    }

    public TabViewCommand getTabViewCommand() {
        return tabViewCommand;
    }

    public static boolean isGamemodeSyncEnabled(String server) {
        return getConfig().getBoolean("syncGamemode."+server, false);
    }
}