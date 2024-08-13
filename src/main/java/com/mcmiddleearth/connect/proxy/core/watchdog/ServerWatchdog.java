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
package com.mcmiddleearth.connect.proxy.core.watchdog;

import com.mcmiddleearth.base.core.message.McmeColors;
import com.mcmiddleearth.base.core.message.Message;
import com.mcmiddleearth.base.core.taskScheduling.Task;
import com.mcmiddleearth.connect.Permission;
import com.mcmiddleearth.connect.proxy.core.McmeConnect;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Eriol_Eandur
 */
public class ServerWatchdog {

    Task watchdog;

    List<String> downList = new ArrayList<>();
    
    List<String> upList = new ArrayList<>();
    
    
    public ServerWatchdog() {
        watchdog = McmeConnect.getProxyPlugin().getTask( () -> {
            downList.clear();
            upList.clear();
            McmeConnect.getProxy().getAllServerInfo().forEach(info -> {
                String finalName = info.getName();
                info.ping((result, error) -> {
                    if(error!=null && !(error.getMessage()!=null && error.getMessage().equals(""))) {
                        downList.add(finalName);
                    } else {
                        upList.add(finalName);
                    }
                });
            });
            McmeConnect.getProxyPlugin().getTask( () -> {
                if(!downList.isEmpty()) {
                    downList.sort((one,two) -> (one==null?-1:(two==null?1:one.compareToIgnoreCase(two))));
                    String single = (downList.size()>1?"":"s");
                    String multi = (downList.size()>1?"s":"");
                    Message message = McmeConnect.errorMessage("WARNING! Server"+multi+" ")
                            .add(downList.get(0), McmeColors.ERROR_STRESSED);
                    for(int i=1; i<downList.size(); i++) {
                        if(i < downList.size()-1) {
                            message.add(" , ");
                        } else {
                            message.add(" and ");
                        }
                        message.add(downList.get(i), McmeColors.ERROR_STRESSED);
                    }
                    message.add(" seem" + single + " to be down.");
                    McmeConnect.getProxy().getPlayers().stream()
                               .filter(player -> player.hasPermission(Permission.WATCHDOG))
                               .forEach(player -> player.sendMessage(message));
                }
            }).schedule(10, TimeUnit.SECONDS);
        });
        watchdog.scheduleRepeating(2, 2, TimeUnit.MINUTES);
    }  
    
    public void stopWatchdog() {
        watchdog.cancel();
    }

    public List<String> getUpList() {
        return upList;
    }
}
