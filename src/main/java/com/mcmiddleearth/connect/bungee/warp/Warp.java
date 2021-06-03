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
package com.mcmiddleearth.connect.bungee.warp;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author Eriol_Eandur
 */
public class Warp {
    
    private String server;
    private String world;
    private String name;
    private String location;
    private String welcomeMessage;
    private boolean visibleToEveryone;
    private UUID owner;
    private Set<UUID> invited = new HashSet<>();


    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public boolean isVisible(ProxiedPlayer player) {
        return visibleToEveryone || player.getUniqueId().equals(owner) || invited.contains(player.getUniqueId());
    }

    public void setInvited(Set<UUID> invitedPlayers) {
        if(invitedPlayers==null) {
            invited = new HashSet<>();
        } else {
            invited = invitedPlayers;
        }
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public void setPublic(boolean isPublic) {
        visibleToEveryone = isPublic;
    }
}
