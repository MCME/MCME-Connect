/*
 * Copyright (C) 2019 MCME
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mcmiddleearth.connect.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.ConnectPlugin;
import com.mcmiddleearth.connect.events.PlayerConnectEvent;
import com.mcmiddleearth.connect.restart.RestartHandler;
import com.mcmiddleearth.connect.tabList.ConnectedPlayer;
import com.mcmiddleearth.connect.tabList.PlayerList;
import com.mcmiddleearth.connect.util.ConnectUtil;
import com.onarandombox.MultiverseCore.MultiverseCore;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Eriol_Eandur
 */
public class ConnectPluginListener implements PluginMessageListener {
    
    
    public ConnectPluginListener() {
    }
    
    

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals(Channel.MAIN)) {
          return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
//Logger.getGlobal().info("Connect Plugin Message: "+subchannel);
        if (subchannel.equals(Channel.TPPOS)) {
            String playerData = in.readUTF();
            String worldData = in.readUTF();
            String[] locData = in.readUTF().split(";");
            runAfterArrival(playerData, source -> {
                source.sendMessage(ChatColor.GOLD+"Teleporting ...");
                World world = Bukkit.getWorld(worldData);
                if(world!=null) {
                    Location location = new Location(world,Double.parseDouble(locData[0]),
                                                         Double.parseDouble(locData[1]),
                                                         Double.parseDouble(locData[2]),
                                                         Float.parseFloat(locData[3]),
                                                         Float.parseFloat(locData[4]));
                    source.teleport(location);
                }
            });
        } else if (subchannel.equals(Channel.TP)) {
            String sourceData = in.readUTF();
            String name = in.readUTF();
            runAfterArrival(sourceData, source -> {
                source.sendMessage(ChatColor.GOLD+"Teleporting to "+ChatColor.RED+name+ChatColor.GOLD+".");
                Player destination = Bukkit.getPlayer(name);
                if(destination!=null) {
                    source.teleport(destination);
                }
            });
        } else if (subchannel.equals(Channel.TITLE)) {
            String recipient = in.readUTF();
            String title = in.readUTF();
            String subtitle = in.readUTF();
            int intro = in.readInt();
            int show = in.readInt();
            int extro = in.readInt();
            Collection<Player> players = new HashSet<>();
            if(recipient.equals(Channel.ALL)) {
                players.addAll(Bukkit.getOnlinePlayers());
            } else {
                players.add(Bukkit.getPlayer(recipient));
            }
            players.forEach(p -> {
                p.sendTitle(title, subtitle, intro, show, extro);
            });
        } else if (subchannel.equals(Channel.COMMAND)) {
            String recipient = in.readUTF();
            String command = in.readUTF();
            runAfterArrival(recipient, source -> {
                Bukkit.dispatchCommand(source, command.substring(1));
            });
        } else if (subchannel.equals(Channel.SPAWN)) {
            String name = in.readUTF();
            runAfterArrival(name, p -> {
                Location spawn = p.getWorld().getSpawnLocation().clone();
                try {
                    spawn = ((MultiverseCore)Bukkit.getPluginManager().getPlugin("Multiverse-Core"))
                        .getMVWorldManager().getMVWorld(p.getWorld().getName())
                        .getSpawnLocation().clone();
                } catch (NullPointerException ex) {}
                p.teleport(spawn);//.add(0.5,0,0.5));
            });
        } else if(subchannel.equals(Channel.DISCORD)) {
            String discordChannel = in.readUTF();
            String discordMessage = in.readUTF();
            ConnectUtil.sendDiscord(discordChannel,discordMessage);
        } else if(subchannel.equals(Channel.LEGACY)) {
            String playerName = in.readUTF();
            String target = in.readUTF();
            runAfterArrival(playerName, p -> {
                if(ConnectPlugin.getStatisticStorage()!=null) {
                    ConnectPlugin.getStatisticStorage().saveStaticstic(p, pp-> {
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF(Channel.CONNECT);
                        out.writeUTF(target);
                        pp.sendPluginMessage(ConnectPlugin.getInstance(), "BungeeCord", out.toByteArray());
                    });
                } else {
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF(Channel.CONNECT);
                    out.writeUTF(target);
                    p.sendPluginMessage(ConnectPlugin.getInstance(), "BungeeCord", out.toByteArray());
                }
            });
        } else if(subchannel.equals(Channel.RESTART)) {
            boolean shutdown = in.readBoolean();
            String playerName = in.readUTF();
            String servers = in.readUTF();
            runAfterArrival(playerName, p -> {
                if(servers.length()>0) {
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF(Channel.RESTART);
                    out.writeBoolean(shutdown);
                    out.writeUTF(playerName);
                    out.writeUTF(servers);
                    p.sendPluginMessage(ConnectPlugin.getInstance(), Channel.MAIN, out.toByteArray());
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(!shutdown) {
                            RestartHandler.restartServer();
                        } else {
                            RestartHandler.stopServer();
                        }
                    }
                }.runTaskLater(ConnectPlugin.getInstance(), 60);
            });
        } else if(subchannel.equals(Channel.JOIN)) {
            String playerName = in.readUTF();
            Player arrivingPlayer = Bukkit.getPlayer(playerName);
            if(arrivingPlayer==null) return;
            String reason = in.readUTF();
            runAfterArrival(playerName, p -> {
                Bukkit.getServer().getPluginManager()
                      .callEvent(new PlayerConnectEvent(arrivingPlayer,PlayerConnectEvent.ConnectReason.valueOf(reason)));
            });
        } else if(subchannel.equals(Channel.PLAYER)) {
            boolean remove = in.readBoolean();
            UUID uuid = UUID.fromString(in.readUTF());
            String name = in.readUTF();
            String displayName = in.readUTF();
            ConnectedPlayer connectedPlayer = new ConnectedPlayer(uuid,name);
            connectedPlayer.setDisplayName(displayName);
            if(!remove) {
                PlayerList.addPlayer(connectedPlayer);
            } else {
                PlayerList.removePlayer(connectedPlayer);
            }
        } else if(subchannel.equals(Channel.GAMEMODE)) {
            Player p = Bukkit.getPlayer(UUID.fromString(in.readUTF()));
            short gm = in.readShort();
Logger.getLogger("ConnectPluginListener").info("receiveGamemode: "+p.getName()+" "+gm+" ");
            switch(gm) {
                case 0:
                    p.setGameMode(GameMode.SURVIVAL);
                    break;
                case 1:
                    p.setGameMode(GameMode.CREATIVE);
                    break;
                case 2:
                    p.setGameMode(GameMode.ADVENTURE);
                    break;
                case 3:
                    p.setGameMode(GameMode.SPECTATOR);
                    break;
            }
        }
    }

    private void runAfterArrival(String playerName, Consumer<Player> callback) {
        new BukkitRunnable() {
            int counter = 40;
            @Override
            public void run() {
                Player source = Bukkit.getPlayer(playerName);
                if(source==null) {
                    if(counter==0) {
                        Logger.getLogger("ConnectionPluginListener").info("WARNING! Expected player didn't arrive!");
                        cancel();
                    } else {
                        counter--;
                    }
                } else {
                    callback.accept(source);
                    cancel();
                }
            }
        }.runTaskTimer(ConnectPlugin.getInstance(), 1, 10);
    }
}
