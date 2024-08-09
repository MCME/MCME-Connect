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

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.base.core.player.McmeProxyPlayer;
import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.proxy.core.McmeConnect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Eriol_Eandur
 */
public class LegacyPlayerHandler {

    private static final Set<UUID> legacyPlayers = new HashSet<>();

    public static void handle(McmeProxyPlayer player, String joinedServer) {
        if(!McmeConnect.getConfig().isLegacyRedirectEnabled()) {
            return;
        }
        String redirectServer = McmeConnect.getConfig().getLegacyRedirectFrom();
        if(legacyPlayers.contains(player.getUniqueId())
                && joinedServer.equals(redirectServer)) {
            String target = McmeConnect.getConfig().getLegacyRedirectTo();
            McmeConnect.getProxyPlugin().getTask(() -> {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF(Channel.LEGACY);
                out.writeUTF(player.getName());
                out.writeUTF(target);
                McmeConnect.getProxy().sendPluginMessage(McmeConnect.getProxy().getServerInfo(redirectServer),
                                                         Channel.MAIN, out.toByteArray(), true);
            }).schedule(McmeConnect.getConfig().getConnectDelay(), TimeUnit.MILLISECONDS);
        }
    }

    public static void loadLegacyPlayers() {
        if(!McmeConnect.getProxyPlugin().getDataFolder().exists()) {
            McmeConnect.getProxyPlugin().getDataFolder().mkdir();
        }
        File file = new File(McmeConnect.getProxyPlugin().getDataFolder(),"legacyPlayer.uid");
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                McmeConnect.getLogger().error("IOException", ex);
            }
        }
        try(Scanner scanner = new Scanner(file))
        {
            while(scanner.hasNext()) {
                legacyPlayers.add(UUID.fromString(scanner.nextLine()));
            }
        } catch (FileNotFoundException ex) {
            McmeConnect.getLogger().error("FileNotFoundException", ex);
        }
    }

}
