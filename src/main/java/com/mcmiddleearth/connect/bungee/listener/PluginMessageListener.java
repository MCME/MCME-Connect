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
package com.mcmiddleearth.connect.bungee.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.connect.bungee.Handler.ConnectHandler;
import com.mcmiddleearth.connect.bungee.Handler.TpposHandler;
import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.bungee.Handler.ChatMessageHandler;
import com.mcmiddleearth.connect.bungee.Handler.RestartHandler;
import com.mcmiddleearth.connect.bungee.Handler.TitleHandler;
import com.mcmiddleearth.connect.bungee.warp.MyWarpDBConnector;
import java.util.logging.Logger;
import net.md_5.bungee.api.ProxyServer;
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
        //if(event.getTag().equals("BungeeCord")) return;
//Logger.getGlobal().info("Plugin Message! "+event.getTag());
        if(event.getTag().equals(Channel.MAIN)) {
//Logger.getGlobal().info("Plugin Connect Message!");
            ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
            String subchannel = in.readUTF();
            switch (subchannel) {
                case Channel.CONNECT:
                    {
                        String server = in.readUTF();
                        String sender = in.readUTF();
                        ConnectHandler.handle(sender,server, true, (connected, error) -> {});
                        //tp to spawn
                        break;
                    }
                case Channel.TPPOS:
                    {
                        Logger.getGlobal().info("reading TPPOS message!");
                        String server = in.readUTF();
                        String sender = in.readUTF();
                        String world = in.readUTF();
                        String locLine = in.readUTF();
                        TpposHandler.handle(sender, server, world, locLine, "");
                        //String[] locData = locLine.split(";");
                        //connect to server
                        //Logger.getGlobal().info("found teleport data!");
                        break;
                    }
                case Channel.MESSAGE:
                    {
                        String server = in.readUTF();
                        String recipient = in.readUTF();
                        String message = in.readUTF();
                        int delay = in.readInt();
                        ChatMessageHandler.handle(server,recipient, message, delay);
                        break;
                    }
                case Channel.TITLE:
                    {
                        String server = in.readUTF();
                        String recipient = in.readUTF();
                        String title = in.readUTF();
                        String subtitle = in.readUTF();
                        int intro = in.readInt();
                        int show = in.readInt();
                        int extro = in.readInt();
                        int delay = in.readInt();
                        TitleHandler.handle(server,recipient, title, subtitle, intro, show, extro, delay);
                        break;
                    }
                case Channel.WORLD_UUID:
                {
//Logger.getGlobal().info("world uuid!");

                    String uuid = in.readUTF();
                    String worldName = in.readUTF();
                    MyWarpDBConnector.addWorldUUID(uuid, worldName);
                    break;
                }
                case Channel.RESTART:
                    boolean shutdown = in.readBoolean();
                    String player = in.readUTF();
                    String[] servers = in.readUTF().split(" ");
                    RestartHandler.handle(ProxyServer.getInstance().getPlayer(player), servers, shutdown);
                    break;
                default:
                    break;
            }
        }
    }
}
