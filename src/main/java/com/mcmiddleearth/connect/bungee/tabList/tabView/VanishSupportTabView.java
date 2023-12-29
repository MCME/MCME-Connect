package com.mcmiddleearth.connect.bungee.tabList.tabView;

import com.mcmiddleearth.connect.bungee.tabList.PacketLogger;
import com.mcmiddleearth.connect.bungee.tabList.playerItem.TabViewPlayerItem;
import com.mcmiddleearth.connect.bungee.tabList.tabView.configuration.ViewableTabViewConfig;
import com.mcmiddleearth.connect.bungee.vanish.VanishHandler;
import com.mcmiddleearth.connect.log.Log;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.Property;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItemRemove;
import net.md_5.bungee.protocol.packet.PlayerListItemUpdate;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public abstract class VanishSupportTabView extends AbstractViewableTabView {

    public VanishSupportTabView(ViewableTabViewConfig config) {
        super(config);
    }

    @Override
    public void handleVanishPlayer(TabViewPlayerItem tabViewItem) {
        PlayerListItemRemove packet = new PlayerListItemRemove();
        /*PlayerListItem.Item[] items = new PlayerListItem.Item[1];
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUuid(tabViewItem.getUuid());
        item.setDisplayName(getConfig().getDisplayName(tabViewItem));
        items[0] = item;
        packet.setItems(items);
        packet.setAction(PlayerListItem.Action.REMOVE_PLAYER);*/
        packet.setUuids(new UUID[]{tabViewItem.getUuid()});
        sendVanishFakeToViewers(getViewers(), packet);
    }

    @Override
    public void handleUnvanishPlayer(TabViewPlayerItem tabViewItem) {
        PlayerListItemUpdate packet = new PlayerListItemUpdate();
        net.md_5.bungee.protocol.packet.PlayerListItem.Item[] items = new net.md_5.bungee.protocol.packet.PlayerListItem.Item[1];
        net.md_5.bungee.protocol.packet.PlayerListItem.Item item = new net.md_5.bungee.protocol.packet.PlayerListItem.Item();
        item.setUuid(tabViewItem.getUuid());
        item.setUsername(tabViewItem.getUsername());
        item.setDisplayName(getConfig().getDisplayName(tabViewItem));
        item.setGamemode(tabViewItem.getGamemode());
        Property[] prop = tabViewItem.getProperties();
        if(prop != null) {
            item.setProperties(prop.clone());
        }
        item.setPing(tabViewItem.getPing());
        items[0] = item;
        packet.setItems(items);
        packet.setActions(EnumSet.of(PlayerListItemUpdate.Action.ADD_PLAYER));
        sendVanishFakeToViewers(getViewers(), packet);
    }

    protected synchronized void sendVanishFakeToViewers(Set<UUID> viewers, PlayerListItemUpdate packet) {
        PlayerListItemUpdate packetDisplay = new PlayerListItemUpdate();
        packetDisplay.setItems(packet.getItems());
        packetDisplay.setActions(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_DISPLAY_NAME));
        viewers.forEach(uuid -> {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
            if (player != null) {
                if (!VanishHandler.hasVanishSeePermission(player)) {
                    PacketLogger.sendItem(player,packet);
                } else {
                    PacketLogger.sendItem(player,packetDisplay);
                }
            }
        });
    }

    protected synchronized void sendVanishFakeToViewers(Set<UUID> viewers, PlayerListItemRemove packet) {
        PlayerListItemUpdate packetDisplay = new PlayerListItemUpdate();
        PlayerListItem.Item[] items = new PlayerListItem.Item[packet.getUuids().length];
        for(int i = 0; i< packet.getUuids().length; i++) {
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUuid(packet.getUuids()[i]);
            items[i] = item;
        }
        packetDisplay.setItems(items);
        packetDisplay.setActions(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_DISPLAY_NAME));
        //packetDisplay.setUuids(packet.getUuids());
        //packetDisplay.setActions(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_DISPLAY_NAME));
        viewers.forEach(uuid -> {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
            if (player != null) {
                if (!VanishHandler.hasVanishSeePermission(player)) {
                    PacketLogger.sendItem(player,packet);
                } else {
                    PacketLogger.sendItem(player,packetDisplay);
                }
            }
        });
    }

    @Override
    protected synchronized void sendToViewers(Set<UUID> viewers, PlayerListItemUpdate packet) {
        String component = "tab.out";
        /*switch(packet.getAction()) {
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
        }*/
        PlayerListItemUpdate publicPacket = new PlayerListItemUpdate();
        publicPacket.setActions(packet.getActions());
        net.md_5.bungee.protocol.packet.PlayerListItem.Item[] items = packet.getItems();
        items = Arrays.stream(items).filter(item -> !VanishHandler.isVanished(item.getUuid())).toArray(net.md_5.bungee.protocol.packet.PlayerListItem.Item[]::new);
        publicPacket.setItems(items);

        viewers.forEach(uuid -> {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
            if(player!=null && VanishHandler.hasVanishSeePermission(player)) {
                PacketLogger.sendItem(player,packet);
            }
        });
        viewers.forEach(uuid -> {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
            if(player!=null && !VanishHandler.hasVanishSeePermission(player)) {
                PacketLogger.sendItem(player,publicPacket);
            }
        });
    }

    @Override
    protected synchronized void sendToViewers(Set<UUID> viewers, PlayerListItemRemove packet) {
        String component = "tab.out";
        /*switch(packet.getAction()) {
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
        }*/
        PlayerListItemRemove publicPacket = new PlayerListItemRemove();
        UUID[] uuids = packet.getUuids();
        uuids = Arrays.stream(uuids).filter(uuid-> !VanishHandler.isVanished(uuid)).toArray(UUID[]::new);
        publicPacket.setUuids(uuids);

        viewers.forEach(uuid -> {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
            if(player!=null && VanishHandler.hasVanishSeePermission(player)) {
                PacketLogger.sendItem(player,packet);
            }
        });
        viewers.forEach(uuid -> {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
            if(player!=null && !VanishHandler.hasVanishSeePermission(player)) {
                PacketLogger.sendItem(player,publicPacket);
            }
        });
    }

    private void logPacketOut(String component, PlayerListItemUpdate packet) {
        Log.LogLevel level = Log.LogLevel.VERBOSE;
        //Log.log(component, Log.LogLevel.INFO,"Sending: "+packet.getAction().name());
        for(net.md_5.bungee.protocol.packet.PlayerListItem.Item item : packet.getItems()) {
            Log.log(component,level,"Items: "+packet.getItems().length);
            Log.log(component,level,"uuid: "+item.getUuid());
            Log.log(component,level,"username: "+item.getUsername());
            Log.log(component,level,"displayName: "+item.getDisplayName());
            Log.log(component,level,"ping: "+item.getPing());
            Log.log(component,level,"gamemode: "+item.getGamemode());
            level = Log.LogLevel.FREQUENT;
            if(item.getProperties()!=null) {
                Log.log(component,level,"Properties: "+item.getProperties().length);
                for (Property propertie : item.getProperties()) {
                    Log.log(component,level,"Name: " + propertie.getName());
                    Log.log(component,level,"Value: " + propertie.getValue());
                }
            }
        }
    }

}
