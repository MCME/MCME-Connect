/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.connect.bungee.tabList.tabView;

import com.mcmiddleearth.connect.bungee.tabList.playerItem.PlayerItemManager;
import com.mcmiddleearth.connect.bungee.tabList.playerItem.TabViewPlayerItem;
import com.mcmiddleearth.connect.bungee.tabList.tabView.configuration.ViewableTabViewConfig;

/**
 *
 * @author Eriol_Eandur
 */
public class ServerTabView extends VanishSupportTabView {

    public ServerTabView(ViewableTabViewConfig config) {
        super(config);
    }

    @Override
    protected boolean isDisplayed(TabViewPlayerItem item) {
        return getConfig().getDisplayedServers().contains(PlayerItemManager.getServer(item));
    }


}
