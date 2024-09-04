package com.mcmiddleearth.connect.proxy.velocity.command;

import com.google.common.base.Joiner;
import com.mcmiddleearth.base.velocity.server.VelocityMcmeProxy;
import com.mcmiddleearth.connect.proxy.core.McmeConnect;
import com.mcmiddleearth.connect.proxy.core.handler.CommandHandler;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ConnectCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
McmeConnect.getLogger().info("ConnectCommand.execute");
        //Nothing to do here: Execution is handled in CommandListener.onCommand
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
McmeConnect.getLogger().info("ConnectCommand.suggetsAsync");
        if(invocation.source() instanceof Player player) {
            String cursor = "/" + Joiner.on(" ").join(invocation.alias(), invocation.arguments());
            return CompletableFuture.supplyAsync(() -> CommandHandler
                    .processGetSuggestions(cursor, ((VelocityMcmeProxy) McmeConnect.getProxy()).getPlayer(player)));
        }
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }
}
