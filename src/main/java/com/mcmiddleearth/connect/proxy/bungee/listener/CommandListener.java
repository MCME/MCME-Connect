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
package com.mcmiddleearth.connect.proxy.bungee.listener;

import com.mcmiddleearth.base.bungee.player.BungeeMcmePlayer;
import com.mcmiddleearth.connect.proxy.bungee.ConnectBungeePlugin;
import com.mcmiddleearth.connect.proxy.core.handler.CommandHandler;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 *
 * @author Eriol_Eandur
 */
public class CommandListener implements Listener {

    public CommandListener() {
    }
    
    @EventHandler
    public void onChat(ChatEvent event) {
        if(event.isCommand() && event.getSender() instanceof ProxiedPlayer) {
            if(CommandHandler.handleChatEvent(new BungeeMcmePlayer(ConnectBungeePlugin.getInstance(), (ProxiedPlayer) event.getSender()),
                                                         event.getMessage())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        event.getSuggestions().addAll(CommandHandler.processGetSuggestions(event.getCursor(),
                event.getSender() instanceof ProxiedPlayer ?
                        new BungeeMcmePlayer(ConnectBungeePlugin.getInstance(), (ProxiedPlayer) event.getSender()):
                        null));
    }
    
}
