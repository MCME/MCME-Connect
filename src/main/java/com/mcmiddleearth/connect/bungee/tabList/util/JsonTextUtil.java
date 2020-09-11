package com.mcmiddleearth.connect.bungee.tabList.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.ChatColor;

import java.awt.*;

public class JsonTextUtil {

    public static JsonObject parseColoredText(String text, ColorAdjustment adjust) {
        JsonObject result = new JsonObject();
        JsonObject current = result;
        boolean firstPart = true;
        Format status = new Format();
        text = text.replaceAll("§","&");
        text = text.replaceAll("\\\\#","§");
        String[] newColorSplit = text.split("#");
        String color = "";
        for(int i = 0; i < newColorSplit.length; i++) {
            newColorSplit[i] = newColorSplit[i].replace('§','#');
            if(newColorSplit[i].length()>0) {
                if(i > 0) {
                    color = "#"+newColorSplit[i].substring(0,6);
                    newColorSplit[i] = newColorSplit[i].substring(6);
                }
                newColorSplit[i] = newColorSplit[i].replaceAll("\\\\&","§");
                String[] oldColorSplit = newColorSplit[i].split("&");
                for(int j = 0; j < oldColorSplit.length; j++) {
                    if(!firstPart) {
                        JsonArray extra = new JsonArray();
                        current.add("extra",extra);
                        current = new JsonObject();
                        extra.add(current);
                    } else {
                        firstPart = false;
                    }
                    if (j == 0) {
                        if(!color.equals("")) {
                            current.addProperty("color", adjustColor(color,adjust));
                            color = "";
                        }
                    } else {
                        char formattingCode = oldColorSplit[j].charAt(0);
                        switch(formattingCode) {
                            case 'k': status.obfuscated = true; current.addProperty("obfuscated",true); break;
                            case 'l': status.bold = true; current.addProperty("bold",true); break;
                            case 'm': status.strikethrough = true; current.addProperty("strikethrough",true); break;
                            case 'n': status.underline = true; current.addProperty("underline",true); break;
                            case 'o': status.italic = true; current.addProperty("italic",true); break;
                            case 'r':
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                if(status.obfuscated) current.addProperty("obfuscated",false);
                                if(status.bold) current.addProperty("bold",false);
                                if(status.strikethrough) current.addProperty("strikethrough",false);
                                if(status.underline) current.addProperty("underline",false);
                                if(status.italic) current.addProperty("italic",false);
                                current.addProperty("color", adjustColor(ChatColor.getByChar(formattingCode),adjust));
                            /*case '0': resetFormat(current, status,"black"); break;
                            case '1': resetFormat(current, status,"dark_blue"); break;
                            case '2': resetFormat(current, status,"dark_green"); break;
                            case '3': resetFormat(current, status,"dark_cyan"); break;
                            case '4': resetFormat(current, status,"dark_red"); break;
                            case '5': resetFormat(current, status,"purple"); break;
                            case '6': resetFormat(current, status,"gold"); break;
                            case '7': resetFormat(current, status,"gray"); break;
                            case '8': resetFormat(current, status,"dark_gray"); break;
                            case '9': resetFormat(current, status,"blue"); break;
                            case 'a': resetFormat(current, status,"green"); break;
                            case 'b': resetFormat(current, status,"aqua"); break;
                            case 'c': resetFormat(current, status,"red"); break;
                            case 'd': resetFormat(current, status,"light_purple"); break;
                            case 'e': resetFormat(current, status,"yellow"); break;*/
                        }
                        oldColorSplit[j] = oldColorSplit[j].substring(1);
                    }
                    if(oldColorSplit[j].length()>0) {
                        current.addProperty("text",oldColorSplit[j]);
                    }
                }
            }
        }
        return result;
    }

    public static String adjustColor(ChatColor color, ColorAdjustment adjust) {
        if(adjust.equals(ColorAdjustment.NONE)) {
            return color.getName();
        } else {
            return adjustColor(color.getColor(),adjust);
        }
    }

    /**
     * Adjusts a Hex RGB color
     * @param color 6 digit hex RGB color
     * @param adjust mode
     * @return hex RGB color string
     */
    public static String adjustColor(String color, ColorAdjustment adjust) {
        if(adjust.equals(ColorAdjustment.NONE)) {
            return color;
        } else {
            return adjustColor(Color.decode(color),adjust);
        }
    }

    public static String adjustColor(Color color, ColorAdjustment adjust) {
        switch (adjust) {
            case BRIGHTEN: return Integer.toHexString(color.brighter().getRGB()).substring(2);
            case DARKEN: return Integer.toHexString(color.darker().getRGB()).substring(2);
            case GRAYOUT:
                float[] hsb = Color.RGBtoHSB(color.getRed(),color.getGreen(),color.getBlue(),null);
                color = Color.getHSBColor(hsb[0],hsb[1]*0.5f,hsb[2]*0.8f);
                return Integer.toHexString(color.darker().getRGB()).substring(2);
            default: return Integer.toHexString(color.getRGB()).substring(2);
        }
    }

    /*private static void resetFormat(JsonObject current, Format status, String color) {
        if(status.obfuscated) current.addProperty("obfuscated",false);
        if(status.bold) current.addProperty("bold",false);
        if(status.strikethrough) current.addProperty("strikethrough",false);
        if(status.underline) current.addProperty("underline",false);
        if(status.italic) current.addProperty("italic",false);
    }*/

    private static class Format {
        public boolean obfuscated;
        public boolean bold;
        public boolean strikethrough;
        public boolean underline;
        public boolean italic;
    }

    public enum ColorAdjustment {
        BRIGHTEN,
        DARKEN,
        GRAYOUT,
        NONE;
    }

}
