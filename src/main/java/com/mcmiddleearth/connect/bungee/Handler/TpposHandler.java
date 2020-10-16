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

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.bungee.ConnectBungeePlugin;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author Eriol_Eandur
 */
public class TpposHandler {
    
    public static boolean handle(String sender, String server, String world, 
                                 String location, String message) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(sender);
        if(player!=null) {
            Callback<Boolean> callback = (connected, error) -> {
                if(connected) {
                    ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF(Channel.TPPOS);
                        out.writeUTF(sender);
                        out.writeUTF(world);
                        out.writeUTF(location);
                        ProxyServer.getInstance().getServerInfo(server).sendData(Channel.MAIN, out.toByteArray());
                        if(!message.equals("")) {
                            ChatMessageHandler.handle(server, sender, message, 400);
                        }
                    }, ConnectBungeePlugin.getConnectDelay(), TimeUnit.MILLISECONDS);
                }
            };
            if(!player.getServer().getInfo().getName().equals(server)) {
                ConnectHandler.handle(sender, server, true, callback);
            } else {
                callback.done(Boolean.TRUE, null);
            }
        }
        return true;
    }
}
