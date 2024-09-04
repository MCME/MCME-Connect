package com.mcmiddleearth.connect.proxy.velocity;

import com.earth2me.essentials.commands.Commandme;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.mcmiddleearth.base.core.message.Message;
import com.mcmiddleearth.base.velocity.AbstractVelocityPlugin;
import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.proxy.core.McmeConnect;
import com.mcmiddleearth.connect.proxy.core.McmeConnectConfig;
import com.mcmiddleearth.connect.proxy.velocity.command.ConnectCommand;
import com.mcmiddleearth.connect.proxy.velocity.command.RebootCommand;
import com.mcmiddleearth.connect.proxy.velocity.listener.CommandListener;
import com.mcmiddleearth.connect.proxy.velocity.listener.ConnectionListener;
import com.mcmiddleearth.connect.proxy.velocity.listener.PluginMessageListener;
import com.mojang.brigadier.tree.CommandNode;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.stream.Collectors;

@Plugin(id = "mcmeconnect", name = "MCME-Connect", version = "2.0.0",
        url = "https://github.com/MCME/MCME-Connect", description = "Plugin to connect MCME servers in a Velocity network",
        authors = {"Eriol_Eandur"})
public class ConnectVelocityPlugin extends AbstractVelocityPlugin{

    @Inject
    public ConnectVelocityPlugin(Logger logger, ProxyServer proxyServer, @DataDirectory Path dataDirectory) {
        super(logger, proxyServer, dataDirectory);
        McmeConnect.setLogger(getMcmeLogger());
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        File configFile = new File(getDataFolder(), McmeConnectConfig.FILE_NAME);
        saveResourceToFile(McmeConnectConfig.FILE_NAME, configFile);

        McmeConnect.enable(this);

        getProxyServer().getChannelRegistrar().register(MinecraftChannelIdentifier.from(Channel.MAIN));

        getProxyServer().getEventManager().register(this, new PluginMessageListener());
        getProxyServer().getEventManager().register(this, new CommandListener());
        getProxyServer().getEventManager().register(this, new ConnectionListener());

        CommandManager commandManager = getProxyServer().getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("reboot").plugin(this).build();
        commandManager.register(commandMeta, new RebootCommand());
        registerConnectCommand("tp");
        registerConnectCommand("tphere");
        registerConnectCommand("tpa");
        registerConnectCommand("tpahere");
        registerConnectCommand("tpaccept","tpyes");
        registerConnectCommand("tpdeny", "tpno");
        registerConnectCommand("tpacancel");
        registerConnectCommand("theme");
        registerConnectCommand("survival");
        registerConnectCommand("mvtp","switch");
        registerConnectCommand("warp", "to");
        registerConnectCommand("reboot");
        registerConnectCommand("stop");
        registerConnectCommand("restorestats");

        getMcmeProxy().getConsole().sendMessage(createMessage().add("Enabled on Velocity proxy!"));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        McmeConnect.disable();
    }

/*    @Subscribe
    public void onChat(PlayerChatEvent event) {
        McmeConnect.getLogger().info("Player Chat in ConnectVelocityPlugin: "+event.getMessage());
    }*/

    private void registerConnectCommand(String command, String... aliases) {
        CommandManager commandManager = getProxyServer().getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder(command).plugin(this).aliases(aliases).build();
        commandManager.register(commandMeta, new ConnectCommand());
    }

/*    @Subscribe
    public void onAvailableCommand(PlayerAvailableCommandsEvent event) {
        McmeConnect.getLogger().info("Player available Commands event in ConnectVelocityPlugin: "+ Joiner.on(";")
                .join(event.getRootNode().getChildren().stream().map(CommandNode::getName).collect(Collectors.toList())));
    }*/

    @Override
    public Message getMessagePrefix() {
        return  createMessage().add("[MCME-Connect] ");
    }
}
