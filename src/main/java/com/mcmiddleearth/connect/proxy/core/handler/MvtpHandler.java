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
package com.mcmiddleearth.connect.proxy.core.handler;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.base.core.player.McmeProxyPlayer;
import com.mcmiddleearth.base.core.server.McmeServerInfo;
import com.mcmiddleearth.base.core.taskScheduling.Callback;
import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.proxy.core.McmeConnect;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author Eriol_Eandur
 */
public class MvtpHandler {
    
    public static boolean handle(String sender, String server) {
        Callback<Boolean> callback = (connected, error) -> {
            if(connected) {
                McmeConnect.getProxyPlugin().getTask( () -> {
                    McmeServerInfo dest = McmeConnect.getProxy().getServerInfo(server);
                    McmeProxyPlayer player = McmeConnect.getProxyPlugin().getPlayer(sender);
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF(Channel.SPAWN);
                    out.writeUTF(sender);
                    McmeConnect.getProxy().sendPluginMessage(McmeConnect.getProxy().getServerInfo(server),
                                                             Channel.MAIN, out.toByteArray(),true);
                }).schedule(McmeConnect.getConfig().getConnectDelay(), TimeUnit.MILLISECONDS);
            }
        };
        return (ConnectionHandler.handleConnectPlayerToServer(sender, server, true, callback)); //if {
    }
}
