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
package com.mcmiddleearth.connect.statistics;

import com.mcmiddleearth.connect.ConnectPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.Statistic;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Eriol_Eandur
 */
public class StatisticListener implements Listener {
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if(ConnectPlugin.getStatisticStorage() == null) return;
                ConnectPlugin.getStatisticStorage().loadStaticstic(event.getPlayer(),player -> {
                    if(ConnectPlugin.getStatisticStorage() != null) {
                        ConnectPlugin.getStatisticStorage().saveStatistic(player);
                    }
                });
            }
        }.runTaskLater(ConnectPlugin.getInstance(), 80);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onStatisticIncrease(PlayerStatisticIncrementEvent event) {
        if(ConnectPlugin.getStatisticStorage() == null) return;
        Statistic stat = event.getStatistic();
        switch(stat.getType()) {
            case BLOCK:
            case ITEM:
                Material mat = event.getMaterial();
                ConnectPlugin.getStatisticStorage()
                             .saveMaterialStats(event.getPlayer(),stat,
                                                mat,event.getNewValue());
                break;
            case ENTITY:
                EntityType entity = event.getEntityType();
                ConnectPlugin.getStatisticStorage()
                             .saveEntityStats(event.getPlayer(),stat,
                                                entity,event.getNewValue());
        }
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent event) {
        if(ConnectPlugin.getStatisticStorage() != null) {
            ConnectPlugin.getStatisticStorage().saveStatistic(event.getPlayer());
        }
    }
    
}
