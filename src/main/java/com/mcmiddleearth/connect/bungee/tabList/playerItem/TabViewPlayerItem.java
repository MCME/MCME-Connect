/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.connect.bungee.tabList.playerItem;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.log.Log;
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
    private boolean afk;
    private boolean vanished;
    
    public TabViewPlayerItem(PlayerListItem.Item item) {
        uuid = item.getUuid();
        username = item.getUsername();
        /*if(username==null) { //unsafe: Possibly access a player who just left!!! Makes other players get kicked from bungee
            username = ProxyServer.getInstance().getPlayer(uuid).getName();
        }*/
        displayname = item.getDisplayName();
        gamemode = item.getGamemode();
        ping = item.getPing();
        if(item.getProperties()!=null) {
            properties = Arrays.copyOf(item.getProperties(),item.getProperties().length);
        } else {
            properties = null;
        }
        Log.info("ViewItem",item.getUsername()+" ");
        afk = false;
    }
    
    public boolean sameData(TabViewPlayerItem other) {
        boolean result =  equals(other) 
                && username.equals(other.username)
                && ((displayname==null && other.displayname==null)
                     || (displayname!=null && displayname.equals(other.displayname)))
                && gamemode == other.gamemode
                && ping == other.ping;
//        if(!result) {
/*Logger.getLogger(TabViewPlayerItem.class.getName()).info("uuuid: "+uuid+" "+other.uuid);
Logger.getLogger(TabViewPlayerItem.class.getName()).info("username: "+username+" "+other.username);
Logger.getLogger(TabViewPlayerItem.class.getName()).info("displayname: "+displayname+" "+other.displayname);
Logger.getLogger(TabViewPlayerItem.class.getName()).info("displayname: "+gamemode+" "+other.gamemode);
Logger.getLogger(TabViewPlayerItem.class.getName()).info("displayname: "+ping+" "+other.ping);*/
            
//        }
        return result;
    }

    public byte[] toByteArray(boolean remove) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(Channel.PLAYER);
        out.writeBoolean(remove);
        out.writeUTF(getUuid().toString());
        String name = getUsername();
        if(name==null) {
            name = "null player";//ProxyServer.getInstance().getPlayer(getUuid()).getName();
        }
        out.writeUTF(name);
        String display = getDisplayname();
        if(display==null) {
            display = name;
        }
        out.writeUTF(display);
        return out.toByteArray();
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

    public boolean getAfk() { return this.afk; }

    public void setAfk(boolean afk) { this.afk = afk; }

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
