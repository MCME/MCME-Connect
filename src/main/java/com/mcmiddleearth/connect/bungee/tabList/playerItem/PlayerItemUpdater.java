package com.mcmiddleearth.connect.bungee.tabList.playerItem;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.Permission;
import com.mcmiddleearth.connect.bungee.ConnectBungeePlugin;
import com.mcmiddleearth.connect.bungee.tabList.TabViewCommand;
import com.mcmiddleearth.connect.bungee.tabList.TabViewManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.protocol.packet.PlayerListItem;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerItemUpdater {

    private ScheduledTask updateTask;

    private Map<String,Map<UUID, TabViewPlayerItem>> itemSnapshot;

    public void invalid_PlayerItemUpdater() {
        updateTask = ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
            if(itemSnapshot == null) {
                itemSnapshot = PlayerItemManager.getSnapshot();
            } else {
                //Map<UUID, ServerSwitchInfo> switches = getSwitches();

                ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {

                }, 500, TimeUnit.MILLISECONDS);
            }
        }, 10, ConnectBungeePlugin.getConfig().getInt("TabListUpdateSeconds",2), TimeUnit.SECONDS);
    }

    private static class ServerSwitchInfo {

    }

    public PlayerItemUpdater() {
        updateTask = ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
if(TabViewCommand.showItems || TabViewCommand.showTabViews) {
    Logger.getGlobal().info("******************update***************");
    if(TabViewCommand.showItems) {
        PlayerItemManager.showItems();
    }
    if(TabViewCommand.showTabViews) {
        TabViewManager.showTabViews();
    }
}
            Map<String,Map<UUID, TabViewPlayerItem>> removalCache = new HashMap<>();
            Map<String,Map<UUID, TabViewPlayerItem>> itemSnapshot = PlayerItemManager.getSnapshot();
            for (Map.Entry<String, ServerInfo> server : ProxyServer.getInstance().getServers().entrySet()) {
                Map<UUID, TabViewPlayerItem> thisServerRemovalCache = new HashMap<>();
                List<PlayerListItem.Item> removal = new ArrayList<>();
                Map<UUID, TabViewPlayerItem> itemMap = PlayerItemManager.getPlayerItems(server.getKey());
                if (itemMap != null) {
                    for (TabViewPlayerItem item : itemMap.values()) {
                        ProxiedPlayer search = ProxyServer.getInstance().getPlayer(item.getUuid());

                        if (search == null || !search.getServer().getInfo().getName().equals(server.getValue().getName())) {
                            PlayerListItem.Item removalItem = new PlayerListItem.Item();
                            removalItem.setUuid(item.getUuid());
                            removal.add(removalItem);
                            thisServerRemovalCache.put(item.getUuid(),item);
                        }
                    }
                }
                removalCache.put(server.getKey(),thisServerRemovalCache);
                if(!removal.isEmpty()) {
                    PlayerListItem packet = new PlayerListItem();
                    packet.setItems(removal.toArray(new PlayerListItem.Item[0]));
                    TabViewManager.handleRemovePlayerPacket(null, packet);
                }
            }
            ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
                removalCache.forEach((server,map) -> map.forEach((uuid, item) -> {
                    ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
                    if (player != null && player.isConnected() && !player.getServer().getInfo().getName().equals(server) //get item from server where player was connected previously
                                       && player.hasPermission(Permission.SYNC_GAMEMODE)
                                       && ConnectBungeePlugin.isGamemodeSyncEnabled(player.getServer().getInfo().getName())
                                       && ConnectBungeePlugin.isGamemodeSyncEnabled(server)) {
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF(Channel.GAMEMODE);
                        out.writeUTF(player.getUniqueId().toString());
                        out.writeShort(item.getGamemode());
//Logger.getGlobal().info("sendGamemode: "+player.getName()+" "+player.isConnected()+" "+player.getServer().getInfo().getName()+" "+item.getGamemode()+" ");
                        player.getServer().getInfo().sendData(Channel.MAIN, out.toByteArray(),true);
                    } else {
//Logger.getGlobal().info("null player or not connected!!!!!!");
                    }
                }));
                for (Map.Entry<String, ServerInfo> server : ProxyServer.getInstance().getServers().entrySet()) {
                    if (!server.getValue().getPlayers().isEmpty()) {
                        ProxiedPlayer sender = server.getValue().getPlayers().stream().findFirst().orElse(null);
                        List<PlayerListItem.Item> items = new ArrayList<>();
                        String serverName = server.getValue().getName();
                        Map<UUID,TabViewPlayerItem> serverStoredItemMap = PlayerItemManager.getPlayerItems(serverName);
                        if(serverStoredItemMap != null) {
                            for (ProxiedPlayer player : server.getValue().getPlayers()) {
                                UUID uuid = player.getUniqueId();
                                TabViewPlayerItem stored = serverStoredItemMap.get(uuid);
                                if (stored == null) {
/*Logger.getGlobal().info("Trying to fetch stored item from snapshot!!!!!! For "+player.getName()+" "+serverName+" "+player.getUniqueId());
itemSnapshot.forEach((logServer,map) -> {
    Logger.getGlobal().info(logServer);
    map.forEach((logUuid,item) -> Logger.getGlobal().info("- "+logUuid.toString()));
});*/
                                    stored = itemSnapshot.get(serverName).get(player.getUniqueId());
                                    if (stored == null) {
if (TabViewCommand.showItems || TabViewCommand.showTabViews) {
    Logger.getGlobal().info("Can't find stored item!!!!!! For "+player.getName());
    if (TabViewCommand.showItems) {
        PlayerItemManager.showItems();
    }
    if (TabViewCommand.showTabViews) {
        TabViewManager.showTabViews();
    }
}
                                        stored = new TabViewPlayerItem(player.getUniqueId(), player.getName());
                                        stored.setPing(player.getPing());
                                    //} else {
                                    }
                                }
                                PlayerListItem.Item item = new PlayerListItem.Item();
                                item.setUuid(stored.getUuid());
//Logger.getGlobal().info("Set item Gamemode For "+player.getName()+" "+stored.getGamemode());
                                item.setGamemode(stored.getGamemode());
                                item.setProperties(stored.getProperties());
                                item.setPing(stored.getPing());
                                item.setUsername(stored.getUsername());
                                items.add(item);
                            }
                        }
                        if (!items.isEmpty()) {
                            PlayerListItem packet = new PlayerListItem();
                            packet.setItems(items.toArray(new PlayerListItem.Item[0]));
                            TabViewManager.handleAddPlayerPacket(sender, packet);
                        }
                    }
                }
            }, 500, TimeUnit.MILLISECONDS);
        }, 10, ConnectBungeePlugin.getConfig().getInt("TabListUpdateSeconds",2), TimeUnit.SECONDS);
    }

    public void disable() {
        updateTask.cancel();
    }
}
