/*
 * Copyright (C) 2019 Eriol_Eandur
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
import net.md_5.bungee.api.ProxyServer;

/**
 *
 * @author Eriol_Eandur
 */
public class CommandHandler {
    
    public static void handle(String server, String commandSender, String command) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(Channel.COMMAND);
        out.writeUTF(commandSender);
        out.writeUTF(command);
//Logger.getGlobal().info("Server send data: "+server);
        ProxyServer.getInstance().getServerInfo(server).sendData(Channel.MAIN, out.toByteArray(),true);
    }
}
