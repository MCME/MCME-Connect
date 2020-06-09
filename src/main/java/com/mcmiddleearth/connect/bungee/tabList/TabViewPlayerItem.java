/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.connect.bungee.tabList;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;
import net.md_5.bungee.protocol.packet.PlayerListItem;

/**
 *
 * @author Eriol_Eandur
 */
public class TabViewPlayerItem {
    
    private UUID uuid;
    private String username;
    private String displayname;
    private int gamemode;
    private String[][] properties;
    private int ping;
    
    public TabViewPlayerItem(PlayerListItem.Item item) {
        uuid = item.getUuid();
        username = item.getUsername();
        displayname = item.getDisplayName();
        gamemode = item.getGamemode();
        ping = item.getPing();
        if(item.getProperties()!=null) {
            properties = Arrays.copyOf(item.getProperties(),item.getProperties().length);
        } else {
            properties = null;
        }
    }
    
    public boolean sameData(TabViewPlayerItem other) {
        boolean result =  equals(other) 
                && username.equals(other.username)
                && ((displayname==null && other.displayname==null)
                     || displayname.equals(other.displayname))
                && gamemode == other.gamemode
                && ping == other.ping;
        if(!result) {
Logger.getLogger(TabViewPlayerItem.class.getName()).info("uuuid: "+uuid+" "+other.uuid);
Logger.getLogger(TabViewPlayerItem.class.getName()).info("username: "+username+" "+other.username);
Logger.getLogger(TabViewPlayerItem.class.getName()).info("displayname: "+displayname+" "+other.displayname);
Logger.getLogger(TabViewPlayerItem.class.getName()).info("displayname: "+gamemode+" "+other.gamemode);
Logger.getLogger(TabViewPlayerItem.class.getName()).info("displayname: "+ping+" "+other.ping);
            
        }
        return result;
    }
    
    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayname() {
        return displayname;
    }

    public String[][] getProperties() {
        return properties;
    }

    public int getGamemode() {
        return gamemode;
    }

    public int getPing() {
        return ping;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public void setGamemode(int gamemode) {
        this.gamemode = gamemode;
    }

    public void setPing(int ping) {
        this.ping = ping;
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof TabViewPlayerItem) {
            return uuid.equals(((TabViewPlayerItem)other).uuid);
        } 
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + Objects.hashCode(this.uuid);
        return hash;
    }
}
