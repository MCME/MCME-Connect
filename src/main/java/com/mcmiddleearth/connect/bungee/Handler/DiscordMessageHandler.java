package com.mcmiddleearth.connect.bungee.Handler;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.connect.Channel;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class DiscordMessageHandler {

    public static void handle(ProxiedPlayer sender, String channel, String message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(Channel.DISCORD);
        out.writeUTF(channel);
        out.writeUTF(message);
        sender.getServer().getInfo().sendData(Channel.MAIN, out.toByteArray(), true);
    }
}

