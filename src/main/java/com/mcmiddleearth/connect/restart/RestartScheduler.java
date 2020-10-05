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
package com.mcmiddleearth.connect.restart;

import com.mcmiddleearth.connect.ConnectPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Eriol_Eandur
 */
public class RestartScheduler {

    private final List<DayOfWeek> restartDays = new ArrayList<>();
    private final List<LocalTime> restartTimes = new ArrayList<>();

    private final List<BukkitTask> restartTasks = new ArrayList<>();

    private boolean restartScheduled = false;

    private BukkitTask task;

    private static File restartScheduleFile = new File(ConnectPlugin.getInstance().getDataFolder(), "restartSchedule.yml");

    public RestartScheduler() {
        //loadConfig();
        task = new BukkitRunnable() {

            @Override
            public void run() {
                loadConfig();
                LocalDateTime now = LocalDateTime.now();
                DayOfWeek day = now.getDayOfWeek();
                if (!restartScheduled) {
                    for (int i = 0; i < restartDays.size(); i++) {
                        if (day.equals(restartDays.get(i))) {
                            LocalDateTime restart = restartTimes.get(i).atDate(LocalDate.now());
                            if (now.isBefore(restart.minusMinutes(9))
                                    && now.isAfter(restart.minusMinutes(10))) {
                                restartScheduled = true;
                                Bukkit.broadcastMessage(ChatColor.BOLD + "Server will restart in 10 minutes.");
                                runLater(() -> Bukkit.broadcastMessage(ChatColor.BOLD + "Server will restart in 5 minutes."), 300);
                                runLater(() -> Bukkit.broadcastMessage(ChatColor.BOLD + "Server will restart in 1 minute."), 540);
                                runLater(() -> Bukkit.broadcastMessage(ChatColor.BOLD + "Server is restarting..."), 600);
                                runLater(() -> RestartHandler.restartServer(), 602);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(ConnectPlugin.getInstance(), 1200, 1200);
    }

    public final void loadConfig() {
        ConnectPlugin.getInstance().reloadConfig();
        List<String> restarts = ConnectPlugin.getInstance().getConfig()
                .getStringList("scheduledRestarts");
        restartDays.clear();
        restartTimes.clear();

        if (restarts != null) {
            for (String line : restarts) {
                String[] split = line.split(" ");
                if (split.length > 1) {
                    if (split[0].equalsIgnoreCase("all")) {
                        LocalTime time = LocalTime.parse(split[1]);
                        for (DayOfWeek day : DayOfWeek.values()) {
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

    interface Callback {
        void call();
    }

    private void runLater(Callback callback, int delaySeconds) {
        restartTasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                if (restartScheduled) {
                    callback.call();
                }
            }
        }.runTaskLater(ConnectPlugin.getInstance(), delaySeconds * 20));
    }

    public List<DayOfWeek> getRestartDays() {
        return restartDays;
    }

    public List<LocalTime> getRestartTimes() {
        return restartTimes;
    }
}

