package com.mcmiddleearth.connect.proxy.core.handler;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.base.core.player.McmeProxyPlayer;
import com.mcmiddleearth.connect.Channel;

public class DiscordMessageHandler {

    public static void handle(McmeProxyPlayer sender, String channel, String message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(Channel.DISCORD);
        out.writeUTF(channel);
        out.writeUTF(message);
        sender.getServerInfo().sendData(Channel.MAIN, out.toByteArray(), true);
    }
}

