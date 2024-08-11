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

import com.mcmiddleearth.base.core.player.McmeProxyPlayer;
import com.mcmiddleearth.base.core.taskScheduling.Callback;
import com.mcmiddleearth.connect.proxy.core.McmeConnect;
import com.mcmiddleearth.base.net.kyori.adventure.text.Component;
import com.mcmiddleearth.base.net.kyori.adventure.text.format.NamedTextColor;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author Eriol_Eandur
 */
public class ThemeHandler {
    
    public static boolean handle(McmeProxyPlayer sender, String server, String command) {
        Callback<Boolean> callback = (connected, error) -> {
            if(connected) {
                McmeConnect.getProxyPlugin().getTask(() -> {
                   sender.sendMessage(Component
                            .text("All Themed-build commands need to be issued from Themed-build world. You were teleported there.")
                                    .color(NamedTextColor.RED));
                    CommandHandler.handle(server, sender.getName(),command);
                }).schedule(McmeConnect.getConfig().getConnectDelay(), TimeUnit.MILLISECONDS);
            }
        };
        return (ConnectionHandler.handleConnectPlayerToServer(sender.getName(), server, true, callback));
    }
}
