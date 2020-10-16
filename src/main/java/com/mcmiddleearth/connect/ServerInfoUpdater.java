package com.mcmiddleearth.connect;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Logger;

public class ServerInfoUpdater extends BukkitRunnable {

    @Override
    public void run() {
        if(!Bukkit.getOnlinePlayers().isEmpty()) {
            Player player = Bukkit.getOnlinePlayers().iterator().next();
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(Channel.SERVER_INFO);

            double[] tps = Bukkit.getTPS();
            for (int i = 0; i < tps.length; i++) {
                out.writeDouble(tps[i]);
            }
            out.writeDouble(Bukkit.getAverageTickTime());
            out.writeInt(Bukkit.getCurrentTick());
            out.writeUTF(Bukkit.getVersion());
            out.writeUTF(Bukkit.getBukkitVersion());
            out.writeUTF(Bukkit.getMinecraftVersion());
            out.writeInt(Bukkit.getViewDistance());
            out.writeUTF(Bukkit.getWorldType());
            player.sendPluginMessage(ConnectPlugin.getInstance(), Channel.MAIN, out.toByteArray());
        }
    }
}
