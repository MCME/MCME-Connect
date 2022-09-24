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

import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.bungee.ConnectBungeePlugin;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Eriol_Eandur
 */
public class ChatMessageHandler {

    public static boolean handle(String server, String recipient, String message, int delay) {
        ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
            Collection<ProxiedPlayer> players = new HashSet<>();
            if(recipient.equals(Channel.ALL)) {
                if(server.equals(Channel.ALL)) {
                    players = ProxyServer.getInstance().getPlayers();
                } else {
                    players = ProxyServer.getInstance().getServerInfo(server).getPlayers();
                }
            } else {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(recipient);
                if(player != null && (server.equals(Channel.ALL)
                        || player.getServer().getInfo().getName().equals(server))) {
                    players.add(player);
                }
            }
            Collection<ProxiedPlayer> finalPlayers = new HashSet<>(players);
            Audience audience = ConnectBungeePlugin.getAudiences()
                    .filter(player->player instanceof ProxiedPlayer && finalPlayers.contains((ProxiedPlayer) player));
            audience.sendMessage(LegacyComponentSerializer.builder().build().deserialize(message));

            /*players.forEach(player -> {
                player.sendMessage(new ComponentBuilder(message).create());
            });*/
        }, delay, TimeUnit.MILLISECONDS);
        return true;
    }
}
