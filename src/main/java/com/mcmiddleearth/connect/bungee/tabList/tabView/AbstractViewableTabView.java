package com.mcmiddleearth.connect.bungee.tabList.tabView;

import com.google.common.collect.Sets;
import com.mcmiddleearth.connect.bungee.ConnectBungeePlugin;
import com.mcmiddleearth.connect.bungee.tabList.playerItem.PlayerItemManager;
import com.mcmiddleearth.connect.bungee.tabList.playerItem.TabViewPlayerItem;
import com.mcmiddleearth.connect.bungee.tabList.tabView.configuration.ViewableTabViewConfig;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.Property;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItemUpdate;
import net.md_5.bungee.protocol.packet.PlayerListItemRemove;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class AbstractViewableTabView implements ITabView{

    private final Set<UUID> viewers = new HashSet<>();

    private IHeaderFooter headerFooter;

    private final ViewableTabViewConfig config;

    public AbstractViewableTabView(ViewableTabViewConfig config) {
        this.config = config;
        setHeaderFooter(new SimpleHeaderFooter(config.getHeader(),
                config.getFooter()));
        ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
            viewers.forEach(viewer -> {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(viewer);
                if(player!=null && headerFooter!=null) {
                    headerFooter.send(player);
                }
            });
        }, 2, 1 , TimeUnit.SECONDS);
        ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
            viewers.forEach(viewer -> {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(viewer);
                if(player!=null) {
                    handleUpdateDisplayName(player, PlayerItemManager.getPlayerItems());
                }
            });
        }, 20, 2 , TimeUnit.SECONDS);

    }

    @Override
    public void handleUpdate(ProxiedPlayer vanillaRecipient, Set<TabViewPlayerItem> tabViewItems, EnumSet<PlayerListItemUpdate.Action> actions) {
        if(tabViewItems.isEmpty()) {
            return;
        }
        PlayerListItemUpdate packet = new PlayerListItemUpdate();
        packet.setItems(createTabviewItems(tabViewItems,actions));
        packet.setActions(actions);

        sendToViewers(viewers, packet);
    }

    @Override
    public void handleAddPlayer(ProxiedPlayer vanillaRecipient, Set<TabViewPlayerItem> tabViewItems) {
        if(tabViewItems.isEmpty()) {
            return;
        }
        PlayerListItemUpdate packet = new PlayerListItemUpdate();
        packet.setItems(createTabviewItems(tabViewItems,PlayerListItemUpdate.Action.ADD_PLAYER));
        packet.setActions(EnumSet.of(PlayerListItemUpdate.Action.ADD_PLAYER));

        sendToViewers(viewers, packet);
    }

    @Override
    public void handleUpdateGamemode(ProxiedPlayer vanillaRecipient, Set<TabViewPlayerItem> tabViewItems) {
        if(tabViewItems.isEmpty()) {
            return;
        }
        PlayerListItemUpdate packet = new PlayerListItemUpdate();
        packet.setItems(createTabviewItems(tabViewItems,PlayerListItemUpdate.Action.UPDATE_GAMEMODE));
        packet.setActions(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_GAMEMODE));

        sendToViewers(viewers, packet);
    }

    @Override
    public void handleUpdateLatency(ProxiedPlayer vanillaRecipient, Set<TabViewPlayerItem> tabViewItems) {
        if(tabViewItems.isEmpty()) {
            return;
        }
        PlayerListItemUpdate packet = new PlayerListItemUpdate();
        packet.setItems(createTabviewItems(tabViewItems,PlayerListItemUpdate.Action.UPDATE_LATENCY));
        packet.setActions(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_LATENCY));

        sendToViewers(viewers, packet);
    }

    @Override
    public void handleUpdateDisplayName(ProxiedPlayer vanillaRecipient, Set<TabViewPlayerItem> tabViewItems) {
        if(tabViewItems.isEmpty()) {
            return;
        }
        PlayerListItemUpdate packet = new PlayerListItemUpdate();
        packet.setItems(createTabviewItems(tabViewItems,PlayerListItemUpdate.Action.UPDATE_DISPLAY_NAME));
        packet.setActions(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_DISPLAY_NAME));

        sendToViewers(viewers, packet);
    }

    @Override
    public void handleRemovePlayer(ProxiedPlayer vanillaRecipient, Set<TabViewPlayerItem> tabViewItems) {
        if(tabViewItems.isEmpty()) {
            return;
        }
        PlayerListItemRemove packet = new PlayerListItemRemove();
        packet.setUuids(tabViewItems.stream().map(TabViewPlayerItem::getUuid).toArray(UUID[]::new));
        //packet.setItems(createTabviewItems(tabViewItems,PlayerListItem.Action.REMOVE_PLAYER));
        //packet.setActions(EnumSet.of(PlayerListItemUpdate.Action.REMOVE_PLAYER));

        sendToViewers(viewers, packet);
    }

    @Override
    public void handleHeaderFooter(ProxiedPlayer vanillaRecipient, PlayerListHeaderFooter packet) {
        //do nothing!
    }

    @Override
    public synchronized void addViewer(ProxiedPlayer player) {
        if(player.getUniqueId()!=null && isViewerAllowedOn(player.getServer().getInfo().getName())) {
            viewers.add(player.getUniqueId());
            Set<TabViewPlayerItem> tabViewItems = PlayerItemManager.getPlayerItems();
            if(!tabViewItems.isEmpty()) {
                PlayerListItemUpdate packet = new PlayerListItemUpdate();
                packet.setItems(createTabviewItems(tabViewItems,PlayerListItemUpdate.Action.ADD_PLAYER));
                packet.setActions(EnumSet.of(PlayerListItemUpdate.Action.ADD_PLAYER));

                sendToViewers(Sets.newHashSet(player.getUniqueId()), packet);
            }
        }
    }

    @Override
    public synchronized void removeViewer(ProxiedPlayer player) {
        if(player==null) return;
        boolean removed = viewers.remove(player.getUniqueId());
        if(player.getUniqueId()!=null) {
            Set<TabViewPlayerItem> tabViewItems = PlayerItemManager.getPlayerItems();
            if(!tabViewItems.isEmpty()) {
                PlayerListItemRemove packet = new PlayerListItemRemove();
                packet.setUuids(tabViewItems.stream().map(TabViewPlayerItem::getUuid).toArray(UUID[]::new));
                //packet.setItems(createTabviewItems(tabViewItems,PlayerListItemUpdate.Action.REMOVE_PLAYER));
                //packet.setActions(EnumSet(PlayerListItemUpdate.Action.REMOVE_PLAYER);

                sendToViewers(Sets.newHashSet(player.getUniqueId()), packet);
            }
        }
    }

    @Override
    public synchronized boolean isViewer(ProxiedPlayer player) {
        return viewers.contains(player.getUniqueId());
    }

    protected abstract void sendToViewers(Set<UUID> viewers, PlayerListItemUpdate packet);
    protected abstract void sendToViewers(Set<UUID> viewers, PlayerListItemRemove packet);

    @Override
    public boolean isViewerAllowedOn(String server) {
        return getConfig().getViewerServers().contains(server);
    }

    @Override
    public boolean isViewerAllowed(ProxiedPlayer player) {
        return isViewerAllowedOn(player.getServer().getInfo().getName())
                && (config.getPermission().equals("") || player.hasPermission(config.getPermission()));
    }

    protected abstract boolean isDisplayed(TabViewPlayerItem item);

    private net.md_5.bungee.protocol.packet.PlayerListItem.Item[] createTabviewItems(Set<TabViewPlayerItem> playerItems, PlayerListItemUpdate.Action action) {
        return createTabviewItems(playerItems, EnumSet.of(action));
    }

    private net.md_5.bungee.protocol.packet.PlayerListItem.Item[] createTabviewItems(Set<TabViewPlayerItem> playerItems, EnumSet<PlayerListItemUpdate.Action> actions) {
        List<net.md_5.bungee.protocol.packet.PlayerListItem.Item> itemList = new ArrayList<>();
        playerItems.stream().filter(item -> item.getUsername()!=null && isDisplayed(item))
                            .sorted(Comparator.comparing(tabViewPlayerItem -> tabViewPlayerItem.getUsername().toLowerCase()))
                   .forEachOrdered(playerItem -> {
            net.md_5.bungee.protocol.packet.PlayerListItem.Item item = new net.md_5.bungee.protocol.packet.PlayerListItem.Item();
            item.setUuid(playerItem.getUuid());
            for(PlayerListItemUpdate.Action action: actions) {
                switch (action) {
                    case ADD_PLAYER:
                        item.setUsername(playerItem.getUsername());
                        Property[] prop = playerItem.getProperties();
                        if (prop != null) {
                            item.setProperties(prop.clone());
                        }
                        item.setDisplayName(config.getDisplayName(playerItem));
                        item.setGamemode(playerItem.getGamemode());
                        item.setPing(playerItem.getPing());
                        break;
                    case UPDATE_DISPLAY_NAME:
                        item.setDisplayName(config.getDisplayName(playerItem));
                        break;
                    case UPDATE_LATENCY:
                        item.setPing(playerItem.getPing());
                        break;
                    case UPDATE_GAMEMODE:
                        item.setGamemode(playerItem.getGamemode());
                        break;
                    case UPDATE_LISTED:
                        item.setListed(!playerItem.isVanished());
                }
            }
            itemList.add(item);
        });
        return itemList.toArray(new net.md_5.bungee.protocol.packet.PlayerListItem.Item[0]);
    }

    public Set<UUID> getViewers() {
        return viewers;
    }

    protected void setHeaderFooter(IHeaderFooter headerFooter) {
        this.headerFooter = headerFooter;
    }

    @Override
    public ViewableTabViewConfig getConfig() {
        return config;
    }

    @Override
    public int getPriority(String server) { return getConfig().getPriority(server);}
}
