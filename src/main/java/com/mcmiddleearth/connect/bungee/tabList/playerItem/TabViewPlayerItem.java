/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.connect.bungee.tabList.playerItem;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.connect.Channel;
import net.md_5.bungee.protocol.Property;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import org.bukkit.GameMode;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

/**
 *
 * @author Eriol_Eandur
 */
public class TabViewPlayerItem {
    
    private UUID uuid;
    private String username;
    private String displayname;
    private int gamemode;
    private Property[] properties;
    private int ping;
    private boolean afk;
    private boolean vanished;
    
    public TabViewPlayerItem(PlayerListItem.Item item) {
//Logger.getGlobal().info("uuid: "+item.getUuid().toString());
        uuid = item.getUuid();
//Logger.getGlobal().info("username: "+item.getUsername());
        username = item.getUsername();
//Logger.getGlobal().info("displayname: "+item.getDisplayName());
        displayname = item.getDisplayName();
//Logger.getGlobal().info("gamemode: "+item.getGamemode());
        gamemode = (item.getGamemode()!=null?item.getGamemode():2);
//Logger.getGlobal().info("ping: "+item.getPing());
        ping = (item.getPing()!=null?item.getPing():-1);
        if(item.getProperties()!=null) {
            properties = Arrays.copyOf(item.getProperties(),item.getProperties().length);
        } else {
            properties = null;
        }
        afk = false;
    }

    public TabViewPlayerItem(UUID uuid, String username){
        this.username = username;
        this.uuid = uuid;
        properties = new Property[0];
    }

    public boolean sameData(TabViewPlayerItem other) {
        boolean result =  equals(other) 
                && ((username == null && other.username == null)
                     || (username!=null && username.equals(other.username)))
                && ((displayname==null && other.displayname==null)
                     || (displayname!=null && displayname.equals(other.displayname)))
                && gamemode == other.gamemode
                && ping == other.ping;
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

    public Property[] getProperties() {
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

    public boolean isVanished() {
        return vanished;
    }

    public void setVanished(boolean vanished) {
        this.vanished = vanished;
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

    public TabViewPlayerItem clone() {
        TabViewPlayerItem clone =  new TabViewPlayerItem(uuid,username);
        clone.setPing(this.ping);
        clone.setGamemode(this.gamemode);
        clone.setAfk(this.afk);
        clone.setDisplayname(this.displayname);
        clone.properties = this.getProperties();
        clone.vanished = this.vanished;
        return clone;
    }
}
