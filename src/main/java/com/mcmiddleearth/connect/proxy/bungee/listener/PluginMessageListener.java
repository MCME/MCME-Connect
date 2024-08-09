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
package com.mcmiddleearth.connect.proxy.bungee.listener;

import com.mcmiddleearth.base.bungee.server.BungeeMcmeServerInfo;
import com.mcmiddleearth.connect.proxy.bungee.ConnectBungeePlugin;
import com.mcmiddleearth.connect.proxy.core.McmeConnect;
import com.mcmiddleearth.connect.proxy.core.handler.PluginMessageHandler;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 *
 * @author Eriol_Eandur
 */
public class PluginMessageListener implements Listener {

    public PluginMessageListener() {
    }
    
    @EventHandler
    public void onMessage(PluginMessageEvent event) {
        if(PluginMessageHandler.handlePluginMessage(event.getTag(),
                    event.getSender() instanceof Server ? new BungeeMcmeServerInfo(((Server)event.getSender()).getInfo()) : null,
                    event.getReceiver() instanceof ProxiedPlayer ? ((ConnectBungeePlugin)McmeConnect.getProxyPlugin()).getPlayer((ProxiedPlayer) event.getReceiver()) : null,
                    event.getData())) {
            event.setCancelled(true);
        }
    }
}
