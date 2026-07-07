package com.mcmiddleearth.connect.proxy.core.handler;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.base.core.message.Message;
import com.mcmiddleearth.base.core.player.McmeProxyPlayer;
import com.mcmiddleearth.base.core.server.McmeServerInfo;
import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.proxy.core.McmeConnect;

import java.util.UUID;

public class PluginMessageHandler {

    public static boolean handlePluginMessage(String channel,
                                              McmeServerInfo messageSender,
                                              McmeProxyPlayer messageReceiver,
                                              byte[] data) {
        if(channel.equals(Channel.MAIN)) {
            if(messageSender == null) {
                McmeConnect.getLogger().warn("Rejected plugin message on channel " + channel + " from non-server source");
                return true;
            }
            ByteArrayDataInput in = ByteStreams.newDataInput(data);
            String subchannel = in.readUTF();
            switch (subchannel) {
                case Channel.CONNECT:
                {
                    String server = in.readUTF();
                    String sender = in.readUTF();
                    ConnectionHandler.handleConnectPlayerToServer(sender,server, true, (connected, error) -> {});
                    //tp to spawn
                    break;
                }
                case Channel.TPPOS:
                {
                    String server = in.readUTF();
                    String sender = in.readUTF();
                    String world = in.readUTF();
                    String locLine = in.readUTF();
                    TpposHandler.handle(sender, server, world, locLine, McmeConnect.getPlugin().emptyMessage());
                    break;
                }
                case Channel.MESSAGE:
                {
                    String server = in.readUTF();
                    String recipient = in.readUTF();
                    String rawMessage = in.readUTF();
//McmeConnect.getLogger().info("Message: "+rawMessage);
                    Message message = McmeConnect.getPlugin().deserializeMessage(rawMessage);
                    int delay = in.readInt();
                    ChatMessageHandler.handle(server,recipient, message, delay);
                    break;
                }
                case Channel.TITLE:
                {
                    String server = in.readUTF();
                    String recipient = in.readUTF();
                    String title = in.readUTF();
                    String subtitle = in.readUTF();
                    int intro = in.readInt();
                    int show = in.readInt();
                    int extro = in.readInt();
                    int delay = in.readInt();
                    TitleHandler.handle(server,recipient, title, subtitle, intro, show, extro, delay);
                    break;
                }
                case Channel.WORLD_UUID:
                {
                    String uuid = in.readUTF();
                    String worldName = in.readUTF();
                    McmeConnect.getMyWarpConnector().addWorldUUID(uuid, worldName);
                    break;
                }
                case Channel.RESTART:
                {
                    boolean shutdown = in.readBoolean();
                    String player = in.readUTF();
                    String[] servers = in.readUTF().split(" ");
                    McmeProxyPlayer restartPlayer = McmeConnect.getProxy().getPlayer(player);
                    if(restartPlayer != null && restartPlayer.hasPermission("mcmeconnect.restart")) {
                        RestartHandler.handle(restartPlayer, servers, shutdown);
                    } else {
                        McmeConnect.getLogger().warn("Rejected RESTART request from player " + player + " - insufficient permissions");
                    }
                    break;
                }
                case Channel.SERVER_INFO:
                    String server = messageSender.getName();
                    McmeConnect.getServerInformation(server).updateFromPluginMessage(in);
                    break;
                case Channel.AFK:
                    String uuid = in.readUTF();
                    boolean afk = in.readBoolean();
                    McmeProxyPlayer afkPlayer = McmeConnect.getProxy().getPlayer(UUID.fromString(uuid));
                    if(afkPlayer!=null) {
                        //TabViewManager.handleUpdateAfk(afkPlayer, afk);
                    }
                    break;
                case Channel.PLAYER:
                    //McmeServerInfo info = messageReceiver.getServerInfo();
                    //PlayerItemManager.sendAllPlayerList(info);
                    break;
                default:
                    break;
            }
            return true;
        }
        return false;
    }
}
