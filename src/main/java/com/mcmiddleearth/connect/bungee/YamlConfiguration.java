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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Eriol_Eandur
 */
public class YamlConfiguration {
    
    private Map<String,Object> map = new HashMap<>();

    public YamlConfiguration(Map<String,Object> map) {
        this.map = map;
    }

    public YamlConfiguration() {}

    public void load(File file) {
        Yaml yaml = new Yaml();
        try {
            map = yaml.load(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(YamlConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Map<String,Object> getSection(String key) {
        Object value = getValue(key);
        return (value!=null?(Map<String,Object>) value:null);
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = getValue(key);
        return (value!=null?(Boolean)value:defaultValue);
    }
    
    public int getInt(String key, int defaultValue) {
        Object value = getValue(key);
        return (value!=null?(Integer)value:defaultValue);
    }
    
    public double getDouble(String key, double defaultValue) {
        Object value = getValue(key);
        return (value!=null?(Double)value:defaultValue);
    }
    
    public String getString(String key, String defaultValue) {
        Object value = getValue(key);
        return (value!=null?(String)value:defaultValue);
    }
    
    public List<String> getStringList(String key) {
        return (List<String>)getValue(key);
    }

    public List<Object> getList(String key) {return (List<Object>)getValue(key); }

    public Object getValue(String key) {
//Logger.getGlobal().info("Key: "+key);
//Logger.getGlobal().info("Keysplit: "+key.split("\\.").length);
        return getValue(map, key.split("\\."));
    }
    
    private Object getValue(Map<String,Object> submap, String[] subkeys) {
//Logger.getGlobal().info("length "+subkeys.length);
        if(subkeys.length>1) {
            if(submap.containsKey(subkeys[0])) {
                return getValue((Map<String,Object>)submap.get(subkeys[0]),
                                Arrays.copyOfRange(subkeys, 1, subkeys.length));
            } else {
                return null;
            }
        } else {
            return submap.get(subkeys[0]);
        }
    }

    public Set<String> getKeys() {
        return map.keySet();
    }
}
