package com.mcmiddleearth.connect.proxy.velocity;

import com.google.inject.Inject;
import com.mcmiddleearth.base.core.message.Message;
import com.mcmiddleearth.base.velocity.AbstractVelocityPlugin;
import com.mcmiddleearth.connect.Channel;
import com.mcmiddleearth.connect.Permission;
import com.mcmiddleearth.connect.proxy.core.McmeConnect;
import com.mcmiddleearth.connect.proxy.core.McmeConnectConfig;
import com.mcmiddleearth.connect.proxy.core.warp.WarpHandler;
import com.mcmiddleearth.connect.proxy.velocity.command.ConnectCommand;
import com.mcmiddleearth.connect.proxy.velocity.command.RebootCommand;
import com.mcmiddleearth.connect.proxy.velocity.listener.CommandListener;
import com.mcmiddleearth.connect.proxy.velocity.listener.ConnectionListener;
import com.mcmiddleearth.connect.proxy.velocity.listener.PluginMessageListener;
import com.mcmiddleearth.connect.proxy.velocity.listener.VanishListener;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

// velocity-plugin.json is generated from this annotation at compile time (annotation processor
// path in pom.xml), so keep version in sync with the pom.
@Plugin(id = "mcmeconnect", name = "MCME-Connect", version = "3.0.3",
        url = "https://github.com/MCME/MCME-Connect", description = "Plugin to connect MCME servers in a Velocity network",
        authors = {"Eriol_Eandur"},
        dependencies = {@Dependency(id = "mcme-base")})
public class ConnectVelocityPlugin extends AbstractVelocityPlugin{

    private final Logger logger;

    @Inject
    public ConnectVelocityPlugin(Logger logger, ProxyServer proxyServer, @DataDirectory Path dataDirectory) {
        super(logger, proxyServer, dataDirectory);
        this.logger = logger;
        McmeConnect.setLogger(getMcmeLogger());
    }

    @Override
    public void enable() {
        File configFile = new File(getDataFolder(), McmeConnectConfig.FILE_NAME);
        saveResourceToFile(McmeConnectConfig.FILE_NAME, configFile);

        McmeConnect.enable(this);

        getProxyServer().getChannelRegistrar().register(MinecraftChannelIdentifier.from(Channel.MAIN));

        getProxyServer().getEventManager().register(this, new PluginMessageListener());
        getProxyServer().getEventManager().register(this, new CommandListener());
        getProxyServer().getEventManager().register(this, new ConnectionListener());
        getProxyServer().getEventManager().register(this, new VanishListener());

        CommandManager commandManager = getProxyServer().getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("reboot").plugin(this).build();
        commandManager.register(commandMeta, new RebootCommand(Permission.RESTART));
        registerConnectCommand("tp", Permission.TP);
        registerConnectCommand("tphere", Permission.TPHERE);
        registerConnectCommand("tpa", Permission.TPA);
        registerConnectCommand("tpahere", Permission.TPA);
        registerConnectCommand("tpaccept", null,"tpyes");
        registerConnectCommand("tpdeny", null, "tpno");
        registerConnectCommand("tpacancel", null);
        registerConnectCommand("theme", null);
        registerConnectCommand("survival", Permission.SURVIVAL);
        registerConnectCommand("mvtp", null,"switch");
        registerMyWarpBridgeCommands();
        registerConnectCommand("stop", Permission.STOP);
        registerConnectCommand("restorestats", null);

        List<String> servers = getProxyServer().getAllServers().stream()
                .map(registeredServer -> registeredServer.getServerInfo().getName()).toList();
        servers.forEach(name -> registerConnectCommand(name, Permission.WORLD+"."+name));

        getMcmeProxy().getConsole().sendMessage(createMessage().add("Enabled on Velocity proxy!"));
    }

    @Override
    public void disable() {
        McmeConnect.disable();
    }

    /**
     * Registers the legacy MyWarp cross-server bridge on /warp and /to, but only when it is
     * both wanted and free to take. See {@link WarpHandler#shouldRegisterWarpCommands}.
     * <p>
     * This used to be an unconditional {@code registerConnectCommand("warp", null, "to")}. Because
     * Velocity replaces an existing alias without complaint, and Connect enables after
     * mcme-warps-velocity, that quietly took /warp and /to away from MCME-Warps on every start.
     * The null permission made it worse: {@code ConnectCommand.hasPermission} returns true when
     * the permission is null, so the commands were also stripped of their permission check and
     * mcmewarps.cmd.warp was never evaluated.
     */
    private void registerMyWarpBridgeCommands() {
        CommandManager commandManager = getProxyServer().getCommandManager();
        boolean aliasAlreadyRegistered = commandManager.hasCommand("warp") || commandManager.hasCommand("to");
        boolean myWarpEnabled = McmeConnect.getConfig().isMyWarpEnabled();
        if(!WarpHandler.shouldRegisterWarpCommands(myWarpEnabled, aliasAlreadyRegistered)) {
            logger.info("Not registering the MyWarp bridge on /warp and /to ({}).",
                    aliasAlreadyRegistered
                            ? "another plugin already owns the command - leaving it alone"
                            : "myWarp.enabled is false");
            return;
        }
        registerConnectCommand("warp", null, "to");
    }

    private void registerConnectCommand(String command, String permission, String... aliases) {
        CommandManager commandManager = getProxyServer().getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder(command).plugin(this).aliases(aliases).build();
        commandManager.register(commandMeta, new ConnectCommand(permission));
    }

    @Override
    public Message getMessagePrefix() {
        return  createMessage().add("[MCME-Connect] ");
    }
}
