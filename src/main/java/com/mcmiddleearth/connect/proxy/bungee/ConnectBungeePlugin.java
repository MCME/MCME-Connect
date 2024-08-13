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
package com.mcmiddleearth.connect.proxy.bungee;

/**
 *
 * @author Eriol_Eandur
 */

import com.mcmiddleearth.base.bungee.AbstractBungeePlugin;
import com.mcmiddleearth.base.core.message.Message;
import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.bungee.tabList.TabViewCommand;
import com.mcmiddleearth.connect.bungee.tabList.TabViewManager;
import com.mcmiddleearth.connect.bungee.tabList.playerItem.PlayerItemUpdater;
import com.mcmiddleearth.connect.proxy.bungee.listener.VanishListener;
import com.mcmiddleearth.connect.proxy.bungee.listener.CommandListener;
import com.mcmiddleearth.connect.proxy.bungee.listener.ConnectionListener;
import com.mcmiddleearth.connect.proxy.bungee.listener.PluginMessageListener;
import com.mcmiddleearth.connect.proxy.core.McmeConnect;
import com.mcmiddleearth.connect.proxy.core.McmeConnectConfig;
import com.mcmiddleearth.connect.proxy.core.handler.VanishHandler;
import com.mcmiddleearth.base.net.kyori.adventure.audience.Audience;
import com.mcmiddleearth.base.net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.File;

public class ConnectBungeePlugin extends AbstractBungeePlugin {
    
    private static ConnectBungeePlugin instance;

    private static PlayerItemUpdater playerItemUpdater;

    private TabViewCommand tabViewCommand;

    private static BungeeAudiences audiences;

    //private Log logger;
    
    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;
        McmeConnect.enable(this);
        File configFile = new File(getDataFolder(), McmeConnectConfig.FILE_NAME);
        saveResourceToFile(McmeConnectConfig.FILE_NAME, configFile);
        //loadConfig();
        //logger = new BungeeLog();
        audiences = getAdventure();//BungeeAudiences.create(ConnectBungeePlugin.getInstance());
        if(VanishHandler.isPvSupport()) {
            getProxy().getPluginManager().registerListener(this, new VanishListener());
        }
        ProxyServer.getInstance().registerChannel(Channel.MAIN);
        //getProxy().getPluginManager().registerListener(this, new TestListener());
        getProxy().getPluginManager().registerListener(this, new PluginMessageListener());
        getProxy().getPluginManager().registerListener(this, new CommandListener());
        getProxy().getPluginManager().registerListener(this, new ConnectionListener());
        //getProxy().getPluginManager().registerListener(this, new TabViewManager());
        //playerItemUpdater = new PlayerItemUpdater();
        TabViewManager.init();
        tabViewCommand = new TabViewCommand();
        ProxyServer.getInstance().getPluginManager().registerCommand(this, tabViewCommand);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        McmeConnect.disable();
        //playerItemUpdater.disable();
        //audiences.close();
        //logger.disable();
    }

    @Override
    public Message getMessagePrefix() {
        return  createMessage().add("[MCME-Connect] ");
    }

    /*private void loadConfig() {
        config.load(configFile);
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
    }*/

    public static ConnectBungeePlugin getInstance() {
        return instance;
    }

    /*public static YamlConfiguration getConfig() {
        return config;
    }*/

    public TabViewCommand getTabViewCommand() {
        return tabViewCommand;
    }

    public static BungeeAudiences getAudiences() {
        return audiences;
    }

    public static Audience getAudience(ProxiedPlayer player) {
        return audiences.player(player);
    }

}