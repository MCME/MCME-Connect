package com.mcmiddleearth.connect.proxy.velocity;

import com.google.inject.Inject;
import com.mcmiddleearth.base.core.message.Message;
import com.mcmiddleearth.base.velocity.AbstractVelocityPlugin;
import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.proxy.core.McmeConnect;
import com.mcmiddleearth.connect.proxy.core.McmeConnectConfig;
import com.mcmiddleearth.connect.proxy.velocity.command.RebootCommand;
import com.mcmiddleearth.connect.proxy.velocity.listener.CommandListener;
import com.mcmiddleearth.connect.proxy.velocity.listener.ConnectionListener;
import com.mcmiddleearth.connect.proxy.velocity.listener.PluginMessageListener;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;

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

        RebootCommand rebootCommand = new RebootCommand();
        CommandManager commandManager = getProxyServer().getCommandManager();
        CommandMeta reportMeta = commandManager.metaBuilder("reboot")
                .plugin(this)
                .build();
        commandManager.register(reportMeta, rebootCommand);

        getMcmeProxy().getConsole().sendMessage(createMessage().add("Enabled on Velocity proxy!"));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        McmeConnect.disable();
    }

    @Subscribe
    public void onTabComplete(TabCompleteEvent event) {
//McmeConnect.getLogger().info("TabComplete in ConnectVelocityPlugin");
    }

    @Override
    public Message getMessagePrefix() {
        return  createMessage().add("[MCME-Connect] ");
    }
}
