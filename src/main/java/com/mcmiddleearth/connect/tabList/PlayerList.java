package com.mcmiddleearth.connect.tabList;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.ConnectPlugin;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Logger;

public class PlayerList {

    private final static Collection<ConnectedPlayer> connectedPlayers = new HashSet<>();

    public static Collection<ConnectedPlayer> getConnectedPlayers() {
        return new HashSet<>(connectedPlayers);
    }

    public static ConnectedPlayer getConnectedPlayer(UUID uuid) {
        return connectedPlayers.stream().filter(player -> player.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public static void addPlayer(ConnectedPlayer player) {
        connectedPlayers.add(player);
connectedPlayers.forEach(listPlayer->  {
    Logger.getGlobal().info(""+listPlayer.getUuid()+" "+listPlayer.getName()+" "+listPlayer.getDisplayName());
});
    }

    public static void removePlayer(ConnectedPlayer player) {
        connectedPlayers.remove(player);
    }

    public static void requestPlayerList(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(Channel.PLAYER);
Logger.getGlobal().info("request player list");
        player.sendPluginMessage(ConnectPlugin.getInstance(), Channel.MAIN, out.toByteArray());
    }

}
