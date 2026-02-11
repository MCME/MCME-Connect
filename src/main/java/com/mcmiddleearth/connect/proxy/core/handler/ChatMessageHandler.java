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

import com.mcmiddleearth.base.core.message.Message;
import com.mcmiddleearth.base.core.player.McmeProxyPlayer;
import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.proxy.core.McmeConnect;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Eriol_Eandur
 */
public class ChatMessageHandler {

    public static boolean handle(String server, String recipient, Message message, int delay) {
        McmeConnect.getProxyPlugin().getTask( () -> {
            Collection<McmeProxyPlayer> players = new HashSet<>();
            if(recipient.equals(Channel.ALL)) {
                if(server.equals(Channel.ALL)) {
                    players = McmeConnect.getProxy().getPlayers();
                } else {
                    players = McmeConnect.getProxy().getPlayers(McmeConnect.getProxy().getServerInfo(server));
                }
            } else {
                McmeProxyPlayer player = McmeConnect.getProxy().getPlayer(recipient);
                if(player != null && (server.equals(Channel.ALL)
                        || (player.getServerInfo() != null && player.getServerInfo().getName().equals(server)))) {
                    players.add(player);
                }
            }
            /*Collection<McmeProxyPlayer> finalPlayers = new HashSet<>(players);
            Audience audience = ConnectBungeePlugin.getAudiences()
                    .filter(player->player instanceof ProxiedPlayer && finalPlayers.contains((ProxiedPlayer) player));
            audience.sendMessage(LegacyComponentSerializer.builder().build().deserialize(message));*/

            players.forEach(player -> player.sendMessage(message));
        }).schedule(delay, TimeUnit.MILLISECONDS);
        return true;
    }
}
