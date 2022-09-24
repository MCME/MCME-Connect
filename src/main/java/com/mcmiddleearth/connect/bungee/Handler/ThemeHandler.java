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

import com.mcmiddleearth.connect.bungee.ConnectBungeePlugin;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author Eriol_Eandur
 */
public class ThemeHandler {
    
    public static boolean handle(ProxiedPlayer sender, String server, String command) {
        Callback<Boolean> callback = (connected, error) -> {
            if(connected) {
                ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
                    ConnectBungeePlugin.getAudience(sender).sendMessage(Component
                            .text("All Themed-build commands need to be issued from Themed-build world. You were teleported there.")
                                    .color(NamedTextColor.RED));
                    CommandHandler.handle(server, sender.getName(),command);
                }, ConnectBungeePlugin.getConnectDelay(), TimeUnit.MILLISECONDS);
            }
        };
        return (ConnectHandler.handle(sender.getName(), server, true, callback)); //if {
    }
}
