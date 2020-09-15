package com.mcmiddleearth.connect.bungee.tabList.tabView.configuration;

import com.mcmiddleearth.connect.bungee.YamlConfiguration;
import com.mcmiddleearth.connect.bungee.tabList.TabViewManager;
import com.mcmiddleearth.connect.bungee.tabList.playerItem.TabViewPlayerItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewableTabViewConfig implements ITabViewConfig{

    private List<String> viewerServers;
    private List<String> displayedServers;
    private Map<String, Integer> priority = new HashMap<>();
    private String header;
    private String footer;
    private IPlayerItemConfig config;

    public ViewableTabViewConfig(YamlConfiguration config) {
        reload(config);
    }

    public void reload(YamlConfiguration config) {
        viewerServers = config.getStringList("viewerServers");
        if(viewerServers==null) viewerServers = new ArrayList<>();
        displayedServers = config.getStringList("displayedServers");
        if(displayedServers==null) displayedServers = new ArrayList<>();
        header = config.getString("header", "§eWelcome to §6§lMCME §f{Player}");
        footer = config.getString("footer", "§6Time: §e{Time} §4| §6Node: §e{Server}\n§6Ping: {Ping} §4| {TPS_1} tps");
        priority.clear();
        ((Map<String,Object>)config.getValue("priority")).forEach((server,serverPriority) -> {
            priority.put(server,(Integer)serverPriority);
        });
        String playerItemConfig = config.getString("playerItem","default");
        this.config = TabViewManager.getPlayerItemConfig(playerItemConfig);
    }

    public String getFooter() {
        return footer;
    }

    public String getHeader() {
        return header;
    }

    public String getDisplayName(TabViewPlayerItem item) {
        return config.getDisplayName(item);
    }

    public List<String> getViewerServers() {
        return viewerServers;
    }

    public List<String> getDisplayedServers() {
        return displayedServers;
    }

    public int getPriority(String server) {
        if(server == null || !priority.containsKey(server)) {
            if(priority.containsKey("default")) {
                return priority.get("default");
            } else {
                return (priority.values().isEmpty()?10:priority.values().iterator().next());
            }
        }
        return priority.get(server);
    }
}
