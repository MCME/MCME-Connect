package com.mcmiddleearth.connect.log;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {

    private static PrintWriter writer;
    private static final File logFile = new File("logger.txt");
    private static final String permission = "logger.admin";

    private static Log instance;

    private static final Map<String,Integer> components = new HashMap<>();

    protected void enable() {
        instance = this;
        try {
//Logger.getGlobal().info("creating new log file!");
            writer = new PrintWriter(logFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disable() {
        writer.close();
    }

    /**
     * /log on/off
     * enables/disables chat messages to player who issued command
     *
     * /log <component> <level> [-bungee/spigot]
     * Sets log level (off,severe, error, warning, info, verbose, frequent)
     * Additional arguments can limit to bungee or spigot server of player who issued command
     * @param sender
     * @param args
     */
    protected void handleCommand(String sender, String[] args) {
        if(args.length>0) {
            if(args[0].equals("clear")) {
                clearLog();
            } else if(args.length == 1 || args[1].equalsIgnoreCase("-bungee")) {
                setLogLevel(args[0], LogLevel.INFO);
            } else if (args[1].equalsIgnoreCase("false")) {
                setLogLevel(args[0], LogLevel.OFF);
            } else {
                setLogLevel(args[0], LogLevel.valueOf(args[1].toUpperCase()));
            };
        } else {
            Logger.getLogger(Log.class.getSimpleName()).info("Log components:");
            for(Map.Entry<String,Integer> entry: components.entrySet()) {
                Logger.getLogger(Log.class.getSimpleName()).info("- "+entry.getKey()+" "+entry.getValue());
            }
        }
    }

    public static void frequent(String component, String message) {
        log(component, LogLevel.FREQUENT, message);
    }

    public static void verbose(String component, String message) {
        log(component, LogLevel.VERBOSE, message);
    }

    public static void info(String component, String message) {
        log(component, LogLevel.INFO, message);
    }

    public static void warn(String component, String message) {
        log(component, LogLevel.WARNING, message);
    }

    public static void error(String component, String message) {
        log(component, LogLevel.ERROR, message);
    }

    public static void severe(String component, String message) {
        log(component, LogLevel.SEVERE, message);
    }

    public static void log(String component, LogLevel level, String message) {
        boolean enabled = false;
//Logger.getGlobal().info("log");
        if(!components.containsKey(component)) {
            setLogLevel(component, LogLevel.INFO);
            enabled = true;
        }
        for(String search: components.keySet()) {
            if (enabled || (component.toLowerCase().startsWith(search.toLowerCase()) && level.value <= components.get(search))) {
                writer.format("[%s - %s] %s (%s)%n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("H:mm:ss,SSS")), component, message, Thread.currentThread().getName());
                writer.flush();
                instance.sendToDeveloper(component, level, message);
                break;
                //Logger.getGlobal().info("logging message!");
            }
        }
    }

    public void sendToDeveloper(String component, LogLevel level, String message) {}

    public static void setLogLevel(String component, LogLevel level) {
        if(component.equalsIgnoreCase("all")) {
            components.replaceAll((k, v) -> level.getValue());
        } else {
            components.put(component, level.getValue());
        }
    }

    public void clearLog() {
        disable();
        enable();
    }

    protected String getPermission() {
        return permission;
    }

    public enum LogLevel {
        INFO(4),
        WARNING(3),
        ERROR(2),
        SEVERE(1),
        OFF(0),
        VERBOSE(5),
        FREQUENT(6);

        int value;

        LogLevel(int value) {
            this.value = value;
        }

        int getValue() {
            return value;
        }

    }
}
