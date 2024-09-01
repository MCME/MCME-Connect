package com.mcmiddleearth.connect.proxy.velocity.command;

import com.mcmiddleearth.connect.proxy.core.handler.RestartHandler;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;

public class RebootCommand implements SimpleCommand {


    @Override
    public void execute(Invocation invocation) {
        if(invocation.source() instanceof ConsoleCommandSource) {
            RestartHandler.handle(null, new String[]{"proxy"});
        }
    }
}
