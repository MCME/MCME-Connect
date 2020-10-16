package com.mcmiddleearth.connect.bungee.tabList.tabView.configuration;

import com.mcmiddleearth.connect.bungee.YamlConfiguration;
import com.mcmiddleearth.connect.bungee.tabList.playerItem.TabViewPlayerItem;

public interface IPlayerItemConfig {

    void reload(YamlConfiguration config);

    String getDisplayName(TabViewPlayerItem item);
}
