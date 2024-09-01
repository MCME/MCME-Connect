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
package com.mcmiddleearth.connect.proxy.velocity.listener;

import com.mcmiddleearth.base.velocity.player.VelocityMcmePlayer;
import com.mcmiddleearth.connect.proxy.core.handler.CommandHandler;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import com.velocitypowered.api.proxy.Player;

/**
 *
 * @author Eriol_Eandur
 */
public class CommandListener {

    public CommandListener() {
    }
    
    @Subscribe
    public void onCommand(CommandExecuteEvent event) {
//McmeConnect.getLogger().info("command: "+event.getCommand());
        if(event.getCommandSource() instanceof Player player) {
//McmeConnect.getLogger().info("Player");
            if(CommandHandler.handleChatEvent(new VelocityMcmePlayer(player),
                                                         "/"+event.getCommand())) {
                event.setResult(CommandExecuteEvent.CommandResult.denied());
            }
        }
        /*if(event.getCommand().equals("reboot") && !(event.getCommandSource() instanceof ConsoleCommandSource)) {
            event.setResult(CommandExecuteEvent.CommandResult.forwardToServer());
        }*/
    }

    @Subscribe
    public void onTabComplete(TabCompleteEvent event) {
        event.getSuggestions().addAll(CommandHandler.processGetSuggestions(event.getPartialMessage(),
                event.getPlayer() != null ?
                        new VelocityMcmePlayer(event.getPlayer()):
                        null));
    }
    
}
