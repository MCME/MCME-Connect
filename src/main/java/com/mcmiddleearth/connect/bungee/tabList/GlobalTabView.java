/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.connect.bungee.tabList;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.PlayerListItem;

/**
 *
 * @author Eriol_Eandur
 */
public class GlobalTabView implements ITabView {

    Set<UUID> viewers = new HashSet<>();
    
    @Override
    public void handleAddPlayer(ProxiedPlayer triggerPlayer, Set<TabViewPlayerItem> tabViewItems) {
        if(tabViewItems.isEmpty()) {
            return;
        }
        PlayerListItem packet = new PlayerListItem();
        PlayerListItem.Item[] items = new PlayerListItem.Item[tabViewItems.size()];
        Iterator<TabViewPlayerItem> iterator = tabViewItems.iterator();
        for(int i = 0; i<tabViewItems.size();i++) {
            TabViewPlayerItem tabViewItem = iterator.next();
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUuid(tabViewItem.getUuid());
            item.setUsername(tabViewItem.getUsername());
            item.setDisplayName(tabViewItem.getDisplayname());
            item.setGamemode(tabViewItem.getGamemode());
            String[][] prop = tabViewItem.getProperties();
            if(prop != null) {
                item.setProperties(prop.clone());
            }
            item.setPing(tabViewItems.iterator().next().getPing());
            items[i] = item;
        }
        packet.setItems(items);
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);

        sendToViewers(packet);
    }

    @Override
    public void handleUpdateGamemode(ProxiedPlayer player, Set<TabViewPlayerItem> tabViewItems) {
        if(tabViewItems.isEmpty()) {
            return;
        }
        PlayerListItem packet = new PlayerListItem();
        PlayerListItem.Item[] items = new PlayerListItem.Item[tabViewItems.size()];
        Iterator<TabViewPlayerItem> iterator = tabViewItems.iterator();
        for(int i = 0; i<tabViewItems.size();i++) {
            TabViewPlayerItem tabViewItem = iterator.next();
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUuid(tabViewItem.getUuid());
            item.setGamemode(tabViewItem.getGamemode());
            items[i] = item;
        }
        packet.setItems(items);
        packet.setAction(PlayerListItem.Action.UPDATE_GAMEMODE);

        sendToViewers(packet);
    }

    @Override
    public void handleUpdateLatency(ProxiedPlayer player, Set<TabViewPlayerItem> tabViewItems) {
        if(tabViewItems.isEmpty()) {
            return;
        }
        PlayerListItem packet = new PlayerListItem();
        PlayerListItem.Item[] items = new PlayerListItem.Item[tabViewItems.size()];
        Iterator<TabViewPlayerItem> iterator = tabViewItems.iterator();
        for(int i = 0; i<tabViewItems.size();i++) {
            TabViewPlayerItem tabViewItem = iterator.next();
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUuid(tabViewItem.getUuid());
            item.setPing(tabViewItem.getPing());
            items[i] = item;
        }
        packet.setItems(items);
        packet.setAction(PlayerListItem.Action.UPDATE_LATENCY);

        sendToViewers(packet);
    }

    @Override
    public void handleUpdateDisplayName(ProxiedPlayer player, Set<TabViewPlayerItem> tabViewItems) {
        if(tabViewItems.isEmpty()) {
            return;
        }
        PlayerListItem packet = new PlayerListItem();
        PlayerListItem.Item[] items = new PlayerListItem.Item[tabViewItems.size()];
        Iterator<TabViewPlayerItem> iterator = tabViewItems.iterator();
        for(int i = 0; i<tabViewItems.size();i++) {
            TabViewPlayerItem tabViewItem = iterator.next();
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUuid(tabViewItem.getUuid());
            item.setDisplayName(tabViewItem.getDisplayname());
            items[i] = item;
        }
        packet.setItems(items);
        packet.setAction(PlayerListItem.Action.UPDATE_DISPLAY_NAME);

        sendToViewers(packet);
    }

    @Override
    public void handleRemovePlayer(ProxiedPlayer player, Set<TabViewPlayerItem> tabViewItems) {
        if(tabViewItems.isEmpty()) {
            return;
        }
        PlayerListItem packet = new PlayerListItem();
        PlayerListItem.Item[] items = new PlayerListItem.Item[tabViewItems.size()];
        Iterator<TabViewPlayerItem> iterator = tabViewItems.iterator();
        for(int i = 0; i<tabViewItems.size();i++) {
            TabViewPlayerItem tabViewItem = iterator.next();
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUuid(tabViewItem.getUuid());
            items[i] = item;
        }
        packet.setItems(items);
        packet.setAction(PlayerListItem.Action.REMOVE_PLAYER);

        sendToViewers(packet);
    }

    @Override
    public synchronized void addViewer(ProxiedPlayer player) {
Logger.getLogger(GlobalTabView.class.getSimpleName()).info("AddPlayer: "+player.getName()+" **********************************");
        if(player.getUniqueId()!=null) {
            viewers.add(player.getUniqueId());
            Set<TabViewPlayerItem> tabViewItems = PlayerItemManager.getPlayerItems();
            if(!tabViewItems.isEmpty()) {
                PlayerListItem packet = new PlayerListItem();
                PlayerListItem.Item[] items = new PlayerListItem.Item[tabViewItems.size()];
Logger.getLogger(GlobalTabView.class.getSimpleName()).info("items: "+items.length);
                Iterator<TabViewPlayerItem> iterator = tabViewItems.iterator();
                for(int i = 0; i<tabViewItems.size();i++) {
                    TabViewPlayerItem tabViewItem = iterator.next();
                    PlayerListItem.Item item = new PlayerListItem.Item();
                    item.setUuid(tabViewItem.getUuid());
                    item.setUsername(tabViewItem.getUsername());
Logger.getLogger(GlobalTabView.class.getSimpleName()).info("PlayerItem: "+tabViewItem.getUsername());
                    item.setDisplayName(tabViewItem.getDisplayname());
                    item.setGamemode(tabViewItem.getGamemode());
                    String[][] prop = tabViewItem.getProperties();
                    if(prop != null) {
                        item.setProperties(prop.clone());
                    }
                    item.setPing(tabViewItems.iterator().next().getPing());
                    items[i] = item;
                }
                packet.setItems(items);
                packet.setAction(PlayerListItem.Action.ADD_PLAYER);

                player.unsafe().sendPacket(packet);
            }
        }
    }

    @Override
    public synchronized void removeViewer(ProxiedPlayer player) {
        boolean removed = viewers.remove(player.getUniqueId());
viewers.forEach(viewer -> Logger.getLogger(GlobalTabView.class.getSimpleName()).info("stored: "+viewer.toString()));
Logger.getLogger(GlobalTabView.class.getSimpleName()).info("remove viewer 2: "+player.getUniqueId().toString()+" "+removed);
        if(player.getUniqueId()!=null) {
Logger.getLogger(GlobalTabView.class.getSimpleName()).info("remove viewer 3");
            Set<TabViewPlayerItem> tabViewItems = PlayerItemManager.getPlayerItems();
            if(!tabViewItems.isEmpty()) {
                PlayerListItem packet = new PlayerListItem();
                PlayerListItem.Item[] items = new PlayerListItem.Item[tabViewItems.size()];
Logger.getLogger(GlobalTabView.class.getSimpleName()).info("items: "+items.length);
                Iterator<TabViewPlayerItem> iterator = tabViewItems.iterator();
                for(int i = 0; i<tabViewItems.size();i++) {
                    TabViewPlayerItem tabViewItem = iterator.next();
                    PlayerListItem.Item item = new PlayerListItem.Item();
                    item.setUuid(tabViewItem.getUuid());
                    items[i] = item;
                }
                packet.setItems(items);
                packet.setAction(PlayerListItem.Action.REMOVE_PLAYER);

                player.unsafe().sendPacket(packet);
            }
        }
    }

    @Override
    public synchronized boolean isViewer(ProxiedPlayer player) {
        return viewers.contains(player.getUniqueId());
    }

    private synchronized void sendToViewers(PlayerListItem packet) {
        if(packet.getAction().equals(PlayerListItem.Action.ADD_PLAYER)
           || packet.getAction().equals(PlayerListItem.Action.REMOVE_PLAYER)) {
            Logger.getLogger(GlobalTabView.class.getSimpleName()).info("Sending packet!");
            Logger.getLogger(GlobalTabView.class.getSimpleName()).info("action: "+packet.getAction().name());
            for(PlayerListItem.Item item : packet.getItems()) {
                Logger.getLogger(GlobalTabView.class.getSimpleName()).info("Items: "+packet.getItems().length);
                Logger.getLogger(GlobalTabView.class.getSimpleName()).info("uuid: "+item.getUuid());
                Logger.getLogger(GlobalTabView.class.getSimpleName()).info("username: "+item.getUsername());
                Logger.getLogger(GlobalTabView.class.getSimpleName()).info("displayName: "+item.getDisplayName());
                Logger.getLogger(GlobalTabView.class.getSimpleName()).info("ping: "+item.getPing());
                Logger.getLogger(GlobalTabView.class.getSimpleName()).info("gamemode: "+item.getGamemode());
                if(item.getProperties()!=null) {
                    Logger.getLogger(GlobalTabView.class.getSimpleName()).info("Properties: "+item.getProperties().length);
                    for (String[] propertie : item.getProperties()) {
                        Logger.getLogger(GlobalTabView.class.getSimpleName()).info("Name: " + propertie[0]);
                        Logger.getLogger(GlobalTabView.class.getSimpleName()).info("Value: " + propertie[1]);
                    }
                }
            }
        }
Logger.getLogger(GlobalTabView.class.getSimpleName()).info("Viewers: " + viewers.size());
        viewers.forEach(uuid -> {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
            if(player!=null) {
Logger.getLogger(GlobalTabView.class.getSimpleName()).info("Send packet to: " + player.getName());
                player.unsafe().sendPacket(packet);
            }
        });
    }
}
