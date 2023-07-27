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
package com.mcmiddleearth.connect.bungee.Handler;

import com.mcmiddleearth.connect.bungee.ConnectBungeePlugin;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

/**
 *
 * @author Eriol_Eandur
 */
public class ConnectHandler {
    
    public static boolean handle(String sender, String server, boolean welcomeMsg, Callback<Boolean> callback) {
        ProxiedPlayer source = ProxyServer.getInstance().getPlayer(sender);
        ServerInfo target = ProxyServer.getInstance().getServerInfo(server);
        Server origin = source.getServer();
        if(target!=null && !origin.getInfo().getName().equals(server)) {
            if(welcomeMsg) {
                ChatMessageHandler.handle(server, sender, ChatColor.YELLOW+"Welcome to '"+server+"'!", 
                                          ConnectBungeePlugin.getConnectDelay());
            }
            source.connect(target,callback);
            return true;
        }
        return false;
    }
}