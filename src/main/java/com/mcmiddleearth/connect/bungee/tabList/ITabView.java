/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.connect.bungee.tabList;

import java.util.Set;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author Eriol_Eandur
 */
public interface ITabView {
    
    public void handleAddPlayer(ProxiedPlayer player, Set<TabViewPlayerItem> items);
    
    public void handleUpdateGamemode(ProxiedPlayer player, Set<TabViewPlayerItem> items);
    
    public void handleUpdateLatency(ProxiedPlayer player, Set<TabViewPlayerItem> items);
    
    public void handleUpdateDisplayName(ProxiedPlayer player, Set<TabViewPlayerItem> items);
    
    public void handleRemovePlayer(ProxiedPlayer player, Set<TabViewPlayerItem> items);

    public void addViewer(ProxiedPlayer player);
    
    public void removeViewer(ProxiedPlayer player);
    
    public boolean isViewer(ProxiedPlayer player);
    
}
