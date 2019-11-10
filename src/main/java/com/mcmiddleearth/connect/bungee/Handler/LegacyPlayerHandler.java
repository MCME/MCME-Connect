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
import java.util.logging.Logger;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author Eriol_Eandur
 */
public class LegacyPlayerHandler {
    
    public static void handle(ProxiedPlayer player, String server, String target) {
        ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(Channel.LEGACY);
            out.writeUTF(player.getName());
            out.writeUTF(target);
// Logger.getGlobal().info("sent LEGACY message: "+server+" - "+ player.getName()+" - "+target);
           ProxyServer.getInstance().getServerInfo(server).sendData(Channel.MAIN, out.toByteArray(),true);   
        }, ConnectBungeePlugin.getConnectDelay(), TimeUnit.MILLISECONDS);
    }
}
