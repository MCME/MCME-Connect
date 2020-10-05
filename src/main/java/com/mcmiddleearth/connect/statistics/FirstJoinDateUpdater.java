/*
 * Copyright (C) 2019 Eriol_Eandur
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

import com.github.mryurihi.tbnbt.stream.NBTInputStream;
import com.github.mryurihi.tbnbt.tag.NBTTag;
import com.github.mryurihi.tbnbt.tag.NBTTagCompound;
import com.github.mryurihi.tbnbt.tag.NBTTagLong;
import com.mcmiddleearth.connect.ConnectPlugin;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 *
 * @author Eriol_Eandur
 */
public class FirstJoinDateUpdater {

    private static final File playerDataFolder = new File(ConnectPlugin.getInstance().getDataFolder(),"playerdata");
    
    static public void update(Player player) {
        if(playerDataFolder.exists()) {
            File file = new File(playerDataFolder,player.getUniqueId().toString()+".dat");
            if(file.exists()) {
                try(NBTInputStream in = new NBTInputStream(new FileInputStream(file))) {
                    NBTTag tag = in.readTag();
                    NBTTagCompound bukkitTag = tag.getAsTagCompound().get("bukkit").getAsTagCompound();
                    NBTTagLong firstPlayedTag = bukkitTag.get("firstPlayed").getAsTagLong();
                    invokeCraftBukkit("entity.CraftPlayer", "setFirstPlayed", 
                                              new Class[]{long.class}, player, firstPlayedTag.getValue());
                } catch (IOException ex) {
                    Logger.getLogger(FirstJoinDateUpdater.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    //remove methods below after 1.13 update (included in PluginUtils 1.2
    public static Class<?> getCraftBukkitClass(String name) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        return Class.forName("org.bukkit.craftbukkit." + version + "." + name);
    }
    
    public static Object invokeCraftBukkit(String className, String methodName, Class[] argsClasses, 
                                           Object object, Object... args) {
        try {
            Class clazz = getCraftBukkitClass(className);
            return invoke(clazz,methodName, argsClasses, object, args);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(FirstJoinDateUpdater.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public static Object invoke(Class<?> clazz, String methodName, Class[] argsClasses, 
                                Object object, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        if(argsClasses==null) {
            argsClasses = new Class[args.length];
            for(int i=0; i<args.length; i++) {
                argsClasses[i] = args[i].getClass();
            }
        }
        Method method;
        try {
            method = clazz.getMethod(methodName, argsClasses);
        } catch (NoSuchMethodException ex) {
            method = clazz.getDeclaredMethod(methodName, argsClasses);
        }
        return method.invoke(object, args);
    }
    

    
}
