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
package com.mcmiddleearth.connect.proxy.core;

import com.mcmiddleearth.base.core.message.MessageColor;
import com.mcmiddleearth.base.core.message.MessageDecoration;
import com.mcmiddleearth.base.core.taskScheduling.Task;
import com.mcmiddleearth.connect.proxy.core.handler.RestartHandler;

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
    
    private final List<Task> restartTasks = new ArrayList<>();
    
    private boolean restartScheduled = false;
    
    private final Task task;
    
    public RestartScheduler() {
        loadConfig();
        task = McmeConnect.getProxyPlugin().getTask( () -> {
            LocalDateTime now = LocalDateTime.now();
            DayOfWeek day = now.getDayOfWeek();
            if(!restartScheduled) {
                for(int i=0; i<restartDays.size();i++) {
                    if(day.equals(restartDays.get(i))) {
                        LocalDateTime restart = restartTimes.get(i).atDate(LocalDate.now());
                        if(now.isBefore(restart.minusMinutes(9))
                                && now.isAfter(restart.minusMinutes(11))) {
                            restartScheduled = true;
                            McmeConnect.getProxy().broadcast(McmeConnect.infoMessage()
                                    .add("MCME network will restart in 10 minutes.", MessageColor.RED, MessageDecoration.BOLD));
                            runLater(() -> McmeConnect.getProxy().broadcast(McmeConnect.infoMessage()
                                    .add("MCME network will restart in 5 minutes.", MessageColor.RED, MessageDecoration.BOLD)),
                                    300);
                            runLater(() -> McmeConnect.getProxy().broadcast(McmeConnect.infoMessage()
                                    .add("MCME network will restart in 1 minutes.", MessageColor.RED, MessageDecoration.BOLD)),
                                    540);
                            runLater(() -> McmeConnect.getProxy().broadcast(McmeConnect.infoMessage()
                                    .add("MCME network is restarting ...", MessageColor.RED, MessageDecoration.BOLD)),
                                    600);
                            runLater(() -> RestartHandler.restartProxy(false),602);
                        }
                    }
                }
            }
        });
        task.scheduleRepeating(1, 1, TimeUnit.MINUTES);
        //loadConfig();
    }
    
    public final void loadConfig() {
        List<String> restarts = McmeConnect.getConfig().getScheduledRestarts();
        restartDays.clear();
        restartTimes.clear();

        if(restarts!=null) {
            for(String line: restarts) {
                String[] split = line.split(" ");
                if(split.length>1) {
                    try {
                        if(split[0].equalsIgnoreCase("all")) {
                            LocalTime time = LocalTime.parse(split[1]);
                            for(DayOfWeek day : DayOfWeek.values()) {
                                restartDays.add(day);
                                restartTimes.add(time);
                            }
                        } else {
                            DayOfWeek day = DayOfWeek.valueOf(split[0].toUpperCase());
                            LocalTime time = LocalTime.parse(split[1]);
                            restartDays.add(day);
                            restartTimes.add(time);
                        }
                    } catch(IllegalArgumentException | java.time.format.DateTimeParseException e) {
                        McmeConnect.getLogger().warn("Invalid restart schedule entry: '" + line + "' - " + e.getMessage());
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
        restartTasks.forEach(Task::cancel);
        restartTasks.clear();
    }
    
    interface Callback{void call();}
    
    private void runLater(Callback callback, int delaySeconds) {
        Task restartTask = McmeConnect.getProxyPlugin().getTask( () ->  {
            if(restartScheduled) {
                callback.call();
            }
        });
        restartTask.schedule(delaySeconds, TimeUnit.SECONDS);
        restartTasks.add(restartTask);
    }

    public List<DayOfWeek> getRestartDays() {
        return restartDays;
    }

    public List<LocalTime> getRestartTimes() {
        return restartTimes;
    }
}

