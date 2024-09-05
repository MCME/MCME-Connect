package com.mcmiddleearth.connect.proxy.velocity.command;

import com.mcmiddleearth.connect.proxy.core.handler.RestartHandler;
import com.velocitypowered.api.proxy.ConsoleCommandSource;

public class RebootCommand extends ConnectCommand {

    public RebootCommand(String permission) {
        super(permission);
    }

    @Override
    public void execute(Invocation invocation) {
        if(invocation.source() instanceof ConsoleCommandSource) {
            RestartHandler.handle(null, new String[]{"proxy"});
        } else {
            super.execute(invocation);
        }
    }
}
