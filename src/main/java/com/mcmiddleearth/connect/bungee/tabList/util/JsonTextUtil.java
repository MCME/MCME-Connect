package com.mcmiddleearth.connect.bungee.tabList.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.ChatColor;

import java.awt.*;
import java.util.Random;
import java.util.logging.Logger;

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
                        current.add("extra", extra);
                        current = new JsonObject();
                        extra.add(current);
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
                                if(status.obfuscated) current.addProperty("obfuscated",false);
                                if(status.bold) current.addProperty("bold",false);
                                if(status.strikethrough) current.addProperty("strikethrough",false);
                                if(status.underline) current.addProperty("underline",false);
                                if(status.italic) current.addProperty("italic",false);
                                current.addProperty("color", adjustColor(ChatColor.getByChar('f'),adjust));
                                break;
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
                                current.addProperty("color", adjustColor(ChatColor.getByChar(formattingCode),adjust));
                        }
                        oldColorSplit[j] = oldColorSplit[j].substring(1);
                    }
                    current.addProperty("text",oldColorSplit[j]);
                    firstPart = false;
                }
            }
        }
        return result;
    }

    public static String adjustColor(ChatColor color, ColorAdjustment adjust) {
        if(adjust.getMethod().equals(AdjustmentMethod.NONE)) {
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
        if(adjust.getMethod().equals(AdjustmentMethod.NONE)) {
            return color;
        } else {
            return adjustColor(Color.decode(color),adjust);
        }
    }

    public static String adjustColor(Color color, ColorAdjustment adjust) {
        switch (adjust.method) {
            case BRIGHTEN: return "#"+Integer.toHexString(color.brighter().getRGB()).substring(2);
            case DARKEN: return "#"+Integer.toHexString(color.darker().getRGB()).substring(2);
            case GRAYOUT:
                float[] hsb = Color.RGBtoHSB(color.getRed(),color.getGreen(),color.getBlue(),null);
                color = Color.getHSBColor(hsb[0],hsb[1]*0.7f,hsb[2]*0.8f);
                return "#"+Integer.toHexString(color.getRGB()).substring(2);
            case ADJUST:
                hsb = Color.RGBtoHSB(color.getRed(),color.getGreen(),color.getBlue(),null);
                color = Color.getHSBColor(hsb[0]*adjust.getHue(),
                                          Math.min(hsb[1]*adjust.getSaturation(),1),
                                          Math.min(hsb[2]*adjust.getBrightness(),1));
                return "#"+Integer.toHexString(color.getRGB()).substring(2);
            default: return "#"+Integer.toHexString(color.getRGB()).substring(2);
        }
    }

    private static class Format {
        public boolean obfuscated;
        public boolean bold;
        public boolean strikethrough;
        public boolean underline;
        public boolean italic;
    }

    public enum AdjustmentMethod {
        BRIGHTEN,
        DARKEN,
        GRAYOUT,
        ADJUST,
        NONE;
    }

    public static class ColorAdjustment {

        private AdjustmentMethod method;
        private float hue=1;
        private float saturation=1;
        private float brightness=1;

        public ColorAdjustment(AdjustmentMethod method) {
            this.method = method;
        }

        public float getHue() {
            return hue;
        }

        public void setHue(float hue) {
            this.hue = hue;
        }

        public float getSaturation() {
            return saturation;
        }

        public void setSaturation(float saturation) {
            this.saturation = saturation;
        }

        public float getBrightness() {
            return brightness;
        }

        public void setBrightness(float brightness) {
            this.brightness = brightness;
        }

        public AdjustmentMethod getMethod() {
            return method;
        }
    }

}
