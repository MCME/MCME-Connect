package com.mcmiddleearth.connect.bungee.tabList;

import com.mcmiddleearth.connect.bungee.vanish.VanishHandler;
import com.mcmiddleearth.connect.log.Log;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.PlayerListItem;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntFunction;

public abstract class VanishSupportTabView implements ITabView {

    protected synchronized void sendVanishFakeToViewers(Set<UUID> viewers, PlayerListItem packet) {
        PlayerListItem packetDisplay = new PlayerListItem();
        packetDisplay.setItems(packet.getItems());
        packetDisplay.setAction(PlayerListItem.Action.UPDATE_DISPLAY_NAME);
        viewers.forEach(uuid -> {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
            if (player != null) {
                if (!VanishHandler.hasVanishSeePermission(player)) {
                    player.unsafe().sendPacket(packet);
                } else {
                    player.unsafe().sendPacket(packetDisplay);
                }
            }
        });
    }

    protected synchronized void sendToViewers(Set<UUID> viewers, PlayerListItem packet) {
        String component = "tab.out";
        switch(packet.getAction()) {
            case ADD_PLAYER:
                logPacketOut(component + ".add", packet);
                break;
            case UPDATE_DISPLAY_NAME:
                logPacketOut(component + ".display", packet);
                break;
            case REMOVE_PLAYER:
                logPacketOut(component + ".remove", packet);
                break;
            case UPDATE_GAMEMODE:
                logPacketOut(component + ".gamemode", packet);
                break;
            case UPDATE_LATENCY:
                logPacketOut(component + ".latency", packet);
                break;
        }
//Logger.getLogger(GlobalTabView.class.getSimpleName()).info("Viewers: " + viewers.size());
        viewers.forEach(uuid -> {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
            if(player!=null && VanishHandler.hasVanishSeePermission(player)) {
//Logger.getLogger(GlobalTabView.class.getSimpleName()).info("Send packet to: " + player.getName());
                player.unsafe().sendPacket(packet);
            }
        });
        PlayerListItem.Item[] items = packet.getItems();
        items = Arrays.stream(items).filter(item -> !VanishHandler.isVanished(item.getUuid())).toArray(PlayerListItem.Item[]::new);
        packet.setItems(items);
        viewers.forEach(uuid -> {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
            if(player!=null && !VanishHandler.hasVanishSeePermission(player)) {
//Logger.getLogger(GlobalTabView.class.getSimpleName()).info("Send packet to: " + player.getName());
                player.unsafe().sendPacket(packet);
            }
        });
    }

    private void logPacketOut(String component, PlayerListItem packet) {
        Log.LogLevel level = Log.LogLevel.VERBOSE;
        Log.log(component, Log.LogLevel.INFO,"Sending: "+packet.getAction().name());
        for(PlayerListItem.Item item : packet.getItems()) {
            Log.log(component,level,"Items: "+packet.getItems().length);
            Log.log(component,level,"uuid: "+item.getUuid());
            Log.log(component,level,"username: "+item.getUsername());
            Log.log(component,level,"displayName: "+item.getDisplayName());
            Log.log(component,level,"ping: "+item.getPing());
            Log.log(component,level,"gamemode: "+item.getGamemode());
            level = Log.LogLevel.FREQUENT;
            if(item.getProperties()!=null) {
                Log.log(component,level,"Properties: "+item.getProperties().length);
                for (String[] propertie : item.getProperties()) {
                    Log.log(component,level,"Name: " + propertie[0]);
                    Log.log(component,level,"Value: " + propertie[1]);
                }
            }
        }
    }

}
