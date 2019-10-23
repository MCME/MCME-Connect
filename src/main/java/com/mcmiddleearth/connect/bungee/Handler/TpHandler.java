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
public class TpHandler {
    
    public static boolean handle(String sender, String server, String target) {
        Callback<Boolean> callback = (connected, error) -> {
            if(connected) {
                ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
                    ProxiedPlayer player = ProxyServer.getInstance().getPlayer(sender);
                    //ProxiedPlayer destination = ProxyServer.getInstance().getPlayer(target);
    //Logger.getGlobal().info("onChat tp to player: "+player.getName());
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF(Channel.TP);
                    out.writeUTF(sender);
                    out.writeUTF(target);
                    ProxyServer.getInstance().getServerInfo(server).sendData(Channel.MAIN, out.toByteArray(),true);   
                }, ConnectBungeePlugin.getConnectDelay(), TimeUnit.MILLISECONDS);
            }
        };
        return (ConnectHandler.handle(sender, server, callback));/*if {
            ProxiedPlayer destination = ProxyServer.getInstance().getPlayer(target);
            ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
Logger.getGlobal().info("onChat tp to player");
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF(Channel.TP);
                out.writeUTF(sender);
                out.writeUTF(target);
                destination.getServer().sendData(Channel.MAIN, out.toByteArray());   
            }, ConnectBungeePlugin.getConnectDelay(), TimeUnit.MILLISECONDS);
            return true;
        }
        return false;*/
    }
}
