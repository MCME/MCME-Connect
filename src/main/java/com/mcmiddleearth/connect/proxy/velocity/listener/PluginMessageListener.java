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
package com.mcmiddleearth.connect.proxy.velocity.listener;

import com.mcmiddleearth.base.velocity.player.VelocityMcmePlayer;
import com.mcmiddleearth.base.velocity.server.VelocityMcmeServerInfo;
import com.mcmiddleearth.connect.proxy.core.McmeConnect;
import com.mcmiddleearth.connect.proxy.core.handler.PluginMessageHandler;
import com.mcmiddleearth.connect.proxy.velocity.ConnectVelocityPlugin;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;

/**
 *
 * @author Eriol_Eandur
 */
public class PluginMessageListener {

    public PluginMessageListener() {
    }
    
    @Subscribe
    public void onMessage(PluginMessageEvent event) {
//McmeConnect.getLogger().info(event.toString());
        if(PluginMessageHandler.handlePluginMessage(event.getIdentifier().getId(),
                    event.getSource() instanceof ServerConnection connection ?
                            new VelocityMcmeServerInfo(((ConnectVelocityPlugin)McmeConnect.getPlugin()).getProxyServer(),
                            (connection.getServerInfo())) : null,
                    event.getTarget() instanceof Player player ?
                            new VelocityMcmePlayer(player) : null,
                    event.getData())) {
            event.setResult(PluginMessageEvent.ForwardResult.handled());
//McmeConnect.getLogger().info("handled");
        }
    }
}
