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
package com.mcmiddleearth.connect.bungee;

import com.mcmiddleearth.connect.proxy.bungee.ConnectBungeePlugin;
import com.mcmiddleearth.connect.proxy.core.handler.RestartHandler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Eriol_Eandur
 */
public class RestartScheduler {

    private final List<DayOfWeek> restartDays = new ArrayList<>();
    private final List<LocalTime> restartTimes = new ArrayList<>();
    
    private final List<ScheduledTask> restartTasks = new ArrayList<>();
    
    private boolean restartScheduled = false;
    
    private ScheduledTask task; 
    
    private static File restartScheduleFile = new File(ConnectBungeePlugin.getInstance().getDataFolder(),"restartSchedule.yml");

    public RestartScheduler() {
        loadConfig();
        task = ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () -> {
            LocalDateTime now = LocalDateTime.now();
            DayOfWeek day = now.getDayOfWeek();
            if(!restartScheduled) {
                for(int i=0; i<restartDays.size();i++) {
                    if(day.equals(restartDays.get(i))) {
                        LocalDateTime restart = restartTimes.get(i).atDate(LocalDate.now());
                        if(now.isBefore(restart.minusMinutes(9)) 
                                && now.isAfter(restart.minusMinutes(10))) {
                            restartScheduled = true;
                            ProxyServer.getInstance().broadcast(new ComponentBuilder(ChatColor.BOLD+"MCME network will restart in 10 minutes.")
                                                                .color(ChatColor.RED).create());
                            runLater(() -> ProxyServer.getInstance().broadcast(new ComponentBuilder(ChatColor.BOLD+"MCME network will restart in 5 minutes.")
                                                                .color(ChatColor.RED).create()),300);
                            runLater(() -> ProxyServer.getInstance().broadcast(new ComponentBuilder(ChatColor.BOLD+"MCME network will restart in 1 minutes.")
                                                                .color(ChatColor.RED).create()),540);
                            runLater(() -> ProxyServer.getInstance().broadcast(new ComponentBuilder(ChatColor.BOLD+"MCME network is restarting ...")
                                                                .color(ChatColor.RED).create()),600);
                            runLater(() -> RestartHandler.restartProxy(false),602);
                        }
                    }
                }
            }
        }, 1, 1, TimeUnit.MINUTES); 
        //loadConfig();
    }
    
    public final void loadConfig() {
        List<String> restarts = ConnectBungeePlugin.getConfig()
                                             .getStringList("scheduledRestarts");
        restartDays.clear();
        restartTimes.clear();

        if(restarts!=null) {
            for(String line: restarts) {
                String[] split = line.split(" ");
                if(split.length>1) {
                    if(split[0].equalsIgnoreCase("all")) {
                        LocalTime time = LocalTime.parse(split[1]);
                        for(DayOfWeek day : DayOfWeek.values()) {
                            restartDays.add(day);
                            restartTimes.add(time);
                        }
                    } else {
                        DayOfWeek day = DayOfWeek.valueOf(split[0]);
                        LocalTime time = LocalTime.parse(split[1]);
                        restartDays.add(day);
                        restartTimes.add(time);
                    }
                }
            }
        }
    }
    
    public void cancel() {
        task.cancel();
    }
    
    public void cancelRestart() {
        restartScheduled = false;
        restartTasks.forEach(task -> task.cancel());
        restartTasks.clear();
    }
    
    interface Callback{void call();}
    
    private void runLater(Callback callback, int delaySeconds) {
        restartTasks.add(ProxyServer.getInstance().getScheduler().schedule(ConnectBungeePlugin.getInstance(), () ->  {
            if(restartScheduled) {
                callback.call();
            }
        }, delaySeconds, TimeUnit.SECONDS));
    }

    public List<DayOfWeek> getRestartDays() {
        return restartDays;
    }

    public List<LocalTime> getRestartTimes() {
        return restartTimes;
    }
}

