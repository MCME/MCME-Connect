package com.mcmiddleearth.connect.tabList;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.ConnectPlugin;
import net.ess3.api.events.AfkStatusChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.logging.Logger;

public class AfkListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void afkStatusChange(AfkStatusChangeEvent event) {
        Logger.getGlobal().info("AFK EvENT: "+event.getValue());
        setAfk(event.getAffected().getBase(),event.getValue());
    }

    private void setAfk(Player player, boolean afk) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(Channel.AFK);
        out.writeUTF(player.getUniqueId().toString());
        out.writeBoolean(afk);
        player.sendPluginMessage(ConnectPlugin.getInstance(), Channel.MAIN, out.toByteArray());
    }
}
