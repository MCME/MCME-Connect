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
package com.mcmiddleearth.connect.util;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.ConnectPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.logging.Logger;

/**
 *
 * @author Eriol_Eandur
 */
public class ConnectUtil {
    
    public static void teleportPlayer(Player player, String server, String world, Location location) {
        if (player == null) throw new NullPointerException("player can't be null");
        if (server == null) throw new NullPointerException("server can't be null");
        if (world == null) throw new NullPointerException("world can't be null");
        if (location == null) throw new NullPointerException("location can't be null");

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(Channel.TPPOS);
        out.writeUTF(server);
        out.writeUTF(player.getName());
        out.writeUTF(world);
        out.writeUTF(location.getX()+";"+location.getY()+";"+location.getZ()+";"+location.getYaw()+";"+location.getPitch());
        Logger.getGlobal().info("teleport player "+player.getName()+" to server: "+server);
        player.sendPluginMessage(ConnectPlugin.getInstance(), Channel.MAIN, out.toByteArray());
    }
    
    public static void sendMessage(Player sender, String server, String recipient, String message, int delay) {
        if (sender == null) throw new NullPointerException("sender can't be null");
        if (server == null) throw new NullPointerException("server can't be null");
        if (recipient == null) throw new NullPointerException("recipient can't be null");
        if (message == null) throw new NullPointerException("message can't be null");

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(Channel.MESSAGE);
        out.writeUTF(server);
        out.writeUTF(recipient);
        out.writeUTF(message);
        out.writeInt(delay);
        sender.sendPluginMessage(ConnectPlugin.getInstance(), Channel.MAIN, out.toByteArray());
    }
   
    public static void sendTitle(Player sender, String server, String recipient, 
                                 String title, String subtitle, int intro, int show, int extro, int delay) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(Channel.TITLE);
        out.writeUTF(server);
        out.writeUTF(recipient);
        out.writeUTF(title);
        out.writeUTF(subtitle);
        out.writeInt(intro);
        out.writeInt(show);
        out.writeInt(extro);
        out.writeInt(delay);
        sender.sendPluginMessage(ConnectPlugin.getInstance(), Channel.MAIN, out.toByteArray());
    }

    public static void sendWorldUUID(Player sender, UUID uid, String name) {
        Logger.getGlobal().info("Sending world uuid: "+uid.toString()+" "+name);
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(Channel.WORLD_UUID);
        out.writeUTF(uid.toString());
        out.writeUTF(name);
        sender.sendPluginMessage(ConnectPlugin.getInstance(), Channel.MAIN, out.toByteArray());
    }
}
